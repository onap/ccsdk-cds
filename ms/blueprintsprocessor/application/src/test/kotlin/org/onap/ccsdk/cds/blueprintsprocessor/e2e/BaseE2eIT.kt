/*
 * Copyright © 2026 Deutsche Telekom AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.e2e

import com.fasterxml.jackson.databind.JsonNode
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.google.protobuf.util.JsonFormat
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils
import io.grpc.stub.StreamObserver
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.After
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.WorkingFoldersInitializer
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import java.io.ByteArrayOutputStream
import java.net.ServerSocket
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Duration
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.streams.toList
import kotlin.test.fail

/**
 * Boots the complete blueprints processor against real infrastructure: a PostgreSQL container as the
 * primary database, a MariaDB container as the `processor-db` resource source, a WireMock server as
 * the `rest` resource source, an [EmbeddedNetconfServer] as the device and an embedded Kafka broker
 * for the self-service consumer. The infrastructure is started once per JVM and every subclass gets the same configuration,
 * so all ITs share one cached Spring context.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        WorkingFoldersInitializer::class,
        TestSecuritySettings.ServerContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
abstract class BaseE2eIT {

    @LocalServerPort
    private var httpPort: Int = 0

    protected val http: WebTestClient by lazy {
        WebTestClient.bindToServer()
            .baseUrl("http://localhost:$httpPort")
            .defaultHeader(HttpHeaders.AUTHORIZATION, TestSecuritySettings.clientAuthToken())
            // the first config-deploy compiles the CBA's Kotlin script
            .responseTimeout(Duration.ofMinutes(2))
            .build()
    }

    @After
    fun resetSouthbound() {
        wireMock.resetAll()
        netconf.reset()
    }

    protected fun publish(version: String) {
        val body = MultipartBodyBuilder().apply {
            part(
                "file",
                object : ByteArrayResource(cba(version)) {
                    override fun getFilename() = "$BLUEPRINT_NAME.zip"
                }
            )
        }.build()
        http.post().uri("/api/v1/blueprint-model/publish")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(body))
            .exchange()
            .expectStatus().isOk
    }

    protected fun publishOnce(version: String = DEFAULT_VERSION) {
        synchronized(published) {
            if (published.add(version)) {
                publish(version)
            }
        }
    }

    protected fun process(request: JsonNode): ProcessResult {
        val result = http.post().uri("/api/v1/execution-service/process")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request.toString())
            .exchange()
            .expectBody(String::class.java)
            .returnResult()
        return ProcessResult(result.status.value(), JacksonUtils.jsonNode(result.responseBody!!))
    }

    protected fun resourceAssignmentRequest(
        hostname: String,
        requestId: String = uniqueId("req"),
        resolutionKey: String = uniqueId("key"),
        version: String = DEFAULT_VERSION
    ): JsonNode = executionRequest(
        "resource-assignment", requestId, version,
        mapOf(
            "resolution-key" to resolutionKey,
            "resource-assignment-properties" to mapOf("hostname" to hostname)
        )
    )

    protected fun configDeployRequest(
        resolutionKey: String,
        requestId: String = uniqueId("req"),
        version: String = DEFAULT_VERSION
    ): JsonNode = executionRequest(
        "config-deploy", requestId, version,
        mapOf(
            "resolution-key" to resolutionKey,
            "netconf-host" to "127.0.0.1",
            "netconf-port" to netconf.port,
            "netconf-username" to netconf.username,
            "netconf-password" to netconf.password
        )
    )

    protected fun executionRequest(
        actionName: String,
        requestId: String,
        version: String,
        requestPayload: Map<String, Any>
    ): JsonNode = JacksonUtils.objectMapper.valueToTree(
        mapOf(
            "commonHeader" to mapOf(
                "originatorId" to "e2e-it",
                "requestId" to requestId,
                "subRequestId" to "$requestId-sub"
            ),
            "actionIdentifiers" to mapOf(
                "blueprintName" to BLUEPRINT_NAME,
                "blueprintVersion" to version,
                "actionName" to actionName,
                "mode" to "sync"
            ),
            "payload" to mapOf("$actionName-request" to requestPayload)
        )
    )

    protected fun processOverGrpc(request: JsonNode): ExecutionServiceOutput {
        val input = ExecutionServiceInput.newBuilder()
            .also { JsonFormat.parser().merge(request.toString(), it) }
            .build()
        val headers = Metadata().apply {
            put(Metadata.Key.of(HttpHeaders.AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER), TestSecuritySettings.clientAuthToken())
        }
        val channel = ManagedChannelBuilder.forAddress("localhost", grpcPort)
            .usePlaintext()
            .intercept(MetadataUtils.newAttachHeadersInterceptor(headers))
            .build()
        try {
            val result = CompletableFuture<ExecutionServiceOutput>()
            val requests = BluePrintProcessingServiceGrpc.newStub(channel).process(
                object : StreamObserver<ExecutionServiceOutput> {
                    override fun onNext(output: ExecutionServiceOutput) {
                        if (output.status.eventType in FINAL_EVENTS) {
                            result.complete(output)
                        }
                    }

                    override fun onError(error: Throwable) {
                        result.completeExceptionally(error)
                    }

                    override fun onCompleted() = Unit
                }
            )
            requests.onNext(input)
            return result.get(2, TimeUnit.MINUTES).also { requests.onCompleted() }
        } finally {
            channel.shutdownNow()
        }
    }

    protected fun sendOverKafka(request: JsonNode) {
        val config = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.brokersAsString,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java
        )
        KafkaProducer<String, String>(config).use {
            it.send(ProducerRecord(REQUEST_TOPIC, uniqueId("key"), request.toString())).get(30, TimeUnit.SECONDS)
        }
    }

    protected fun awaitKafkaResponse(requestId: String): JsonNode {
        val config = mapOf<String, Any>(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to kafka.brokersAsString,
            ConsumerConfig.GROUP_ID_CONFIG to uniqueId("e2e-it-reader"),
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java
        )
        KafkaConsumer<String, String>(config).use { consumer ->
            consumer.subscribe(listOf(RESPONSE_TOPIC))
            val deadline = System.nanoTime() + TimeUnit.MINUTES.toNanos(2)
            while (System.nanoTime() < deadline) {
                consumer.poll(Duration.ofSeconds(1))
                    .map { JacksonUtils.jsonNode(it.value()) }
                    .firstOrNull { it.at("/commonHeader/requestId").asText() == requestId }
                    ?.let { return it }
            }
        }
        fail("No response for request $requestId on $RESPONSE_TOPIC")
    }

    protected fun seedDevice(hostname: String, mgmtIp: String) {
        deviceDb.update("INSERT INTO E2E_DEVICE (hostname, mgmt_ip) VALUES (?, ?)", hostname, mgmtIp)
    }

    data class ProcessResult(val httpStatus: Int, val body: JsonNode) {

        val statusCode: Int get() = body.at("/status/code").asInt()
        val errorMessage: String get() = body.at("/status/errorMessage").asText()
    }

    companion object {

        const val BLUEPRINT_NAME = "e2e-it"
        const val DEFAULT_VERSION = "1.0.0"
        const val REST_USERNAME = "e2e-inventory"
        const val REST_PASSWORD = "e2e-inventory-secret"
        const val REQUEST_TOPIC = "e2e-it.request"
        const val RESPONSE_TOPIC = "e2e-it.response"

        private val FINAL_EVENTS = setOf(EventType.EVENT_COMPONENT_EXECUTED, EventType.EVENT_COMPONENT_FAILURE)

        private val postgres = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:13-alpine")).apply {
            withDatabaseName("sdnctl")
            withUsername("sdnctl")
            withPassword("sdnctl")
            start()
        }

        private val mariaDb = MariaDBContainer<Nothing>(DockerImageName.parse("mariadb:11.7.2")).apply {
            withDatabaseName("sdnctl")
            withUsername("sdnctl")
            withPassword("sdnctl")
            start()
        }

        val wireMock = WireMockServer(wireMockConfig().dynamicPort()).apply { start() }

        val netconf = EmbeddedNetconfServer("e2e-netconf", "e2e-netconf-secret")

        val grpcPort: Int = ServerSocket(0).use { it.localPort }

        val kafka = EmbeddedKafkaKraftBroker(1, 1, REQUEST_TOPIC, RESPONSE_TOPIC).apply { afterPropertiesSet() }

        val jdbc = JdbcTemplate(DriverManagerDataSource(postgres.jdbcUrl, postgres.username, postgres.password))

        private val deviceDb = JdbcTemplate(DriverManagerDataSource(mariaDb.jdbcUrl, mariaDb.username, mariaDb.password)).apply {
            execute("CREATE TABLE E2E_DEVICE (hostname VARCHAR(64) PRIMARY KEY, mgmt_ip VARCHAR(64) NOT NULL)")
        }

        private val published = mutableSetOf<String>()

        @JvmStatic
        @DynamicPropertySource
        fun infrastructureProperties(registry: DynamicPropertyRegistry) {
            registry.add("blueprintsprocessor.db.url") { postgres.jdbcUrl }
            registry.add("blueprintsprocessor.db.username") { postgres.username }
            registry.add("blueprintsprocessor.db.password") { postgres.password }
            registry.add("blueprintsprocessor.db.driverClassName") { "org.postgresql.Driver" }
            registry.add("blueprintsprocessor.db.hibernateDialect") { "org.hibernate.dialect.PostgreSQLDialect" }

            registry.add("blueprintsprocessor.db.processor-db.type") { "maria-db" }
            registry.add("blueprintsprocessor.db.processor-db.url") { mariaDb.jdbcUrl }
            registry.add("blueprintsprocessor.db.processor-db.username") { mariaDb.username }
            registry.add("blueprintsprocessor.db.processor-db.password") { mariaDb.password }

            registry.add("blueprintsprocessor.restclient.rest.type") { "basic-auth" }
            registry.add("blueprintsprocessor.restclient.rest.url") { wireMock.baseUrl() }
            registry.add("blueprintsprocessor.restclient.rest.username") { REST_USERNAME }
            registry.add("blueprintsprocessor.restclient.rest.password") { REST_PASSWORD }

            registry.add("blueprintsprocessor.grpcPort") { grpcPort }

            val consumer = "blueprintsprocessor.messageconsumer.self-service-api"
            registry.add("$consumer.kafkaEnable") { true }
            registry.add("$consumer.type") { "kafka-basic-auth" }
            registry.add("$consumer.bootstrapServers") { kafka.brokersAsString }
            registry.add("$consumer.topic") { REQUEST_TOPIC }
            registry.add("$consumer.groupId") { "e2e-it-processor" }
            registry.add("$consumer.clientId") { "e2e-it-processor" }
            // the consumer subscribes asynchronously after startup, possibly after a test has produced
            registry.add("$consumer.autoOffsetReset") { "earliest" }
            val producer = "blueprintsprocessor.messageproducer.self-service-api"
            registry.add("$producer.type") { "kafka-basic-auth" }
            registry.add("$producer.bootstrapServers") { kafka.brokersAsString }
            registry.add("$producer.topic") { RESPONSE_TOPIC }
            registry.add("$producer.clientId") { "e2e-it-processor" }
        }

        fun uniqueId(prefix: String) = "$prefix-${UUID.randomUUID()}"

        /** Zips the CBA from the test resources, stamped with [version] so tests can publish it side by side. */
        fun cba(version: String): ByteArray {
            val root = Paths.get(BaseE2eIT::class.java.getResource("/$BLUEPRINT_NAME")!!.toURI())
            val files = Files.walk(root).use { paths -> paths.filter { Files.isRegularFile(it) }.sorted().toList() }
            return ByteArrayOutputStream().use { bytes ->
                ZipOutputStream(bytes).use { zip ->
                    files.forEach { file ->
                        val name = root.relativize(file).toString().replace('\\', '/')
                        val content = String(Files.readAllBytes(file))
                            .replace("Template-Version: $DEFAULT_VERSION", "Template-Version: $version")
                            .replace("\"template_version\" : \"$DEFAULT_VERSION\"", "\"template_version\" : \"$version\"")
                        zip.putNextEntry(ZipEntry(name))
                        zip.write(content.toByteArray())
                        zip.closeEntry()
                    }
                }
                bytes.toByteArray()
            }
        }
    }
}
