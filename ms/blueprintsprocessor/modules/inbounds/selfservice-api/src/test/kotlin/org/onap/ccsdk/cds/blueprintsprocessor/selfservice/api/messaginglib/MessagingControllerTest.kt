/*
 * Copyright © 2019 Bell Canada
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
package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.messaginglib

import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang.builder.ToStringBuilder
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.MessagingController
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.client.MultipartBodyBuilder
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.annotation.PartitionOffset
import org.springframework.kafka.annotation.TopicPartition
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.web.reactive.function.BodyInserters
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@EnableAutoConfiguration
@ContextConfiguration(classes = [MessagingControllerTest::class, SecurityProperties::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
@DirtiesContext
@EmbeddedKafka(ports = [9092])
@WebFluxTest
class MessagingControllerTest {

    private val log = LoggerFactory.getLogger(MessagingControllerTest::class.java)!!

    @Autowired
    lateinit var controller: MessagingController

    @Value("\${blueprintsprocessor.messageclient.self-service-api.consumerTopic}")
    lateinit var topicUsedForConsumer: String

    @Autowired
    lateinit var kt: KafkaTemplate<String, ExecutionServiceInput>

    @Autowired
    lateinit var webTestClient: WebTestClient

    var receivedEvent: String? = null

    @Before
    fun setup() {
        deleteDir("target", "blueprints")
        uploadBluePrint()
    }

    @After
    fun clean() {
        deleteDir("target", "blueprints")
    }

    @Test
    fun testReceive() {
        val samplePayload = "{\n" +
                "    \"resource-assignment-request\": {\n" +
                "      \"artifact-name\": [\"hostname\"],\n" +
                "      \"store-result\": true,\n" +
                "      \"resource-assignment-properties\" : {\n" +
                "        \"hostname\": \"demo123\"\n" +
                "      }\n" +
                "    }\n" +
                "  }"

        kt.defaultTopic = topicUsedForConsumer

        val input = ExecutionServiceInput().apply {
            commonHeader = CommonHeader().apply {
                originatorId = "1"
                requestId = "1234"
                subRequestId = "1234-1234"
            }

            actionIdentifiers = ActionIdentifiers().apply {
                blueprintName = "golden"
                blueprintVersion = "1.0.0"
                actionName = "resource-assignment"
                mode = "sync"
            }

            stepData = StepData().apply {
                name = "resource-assignment"
            }

            payload = JacksonUtils.jsonNode(samplePayload) as ObjectNode
        }

        kt.sendDefault(input)
        log.info("test-sender sent message='{}'", ToStringBuilder.reflectionToString(input))

        Thread.sleep(1000)
    }

    @KafkaListener(topicPartitions = [TopicPartition(topic = "\${blueprintsprocessor.messageclient.self-service-api.topic}", partitionOffsets = [PartitionOffset(partition = "0", initialOffset = "0")])])
    fun receivedEventFromBluePrintProducer(event: ExecutionServiceInput) {
        assertNotNull(event)
    }

    private fun uploadBluePrint() {
        runBlocking {
            val body = MultipartBodyBuilder().apply {
                part("file", object : ByteArrayResource(Files.readAllBytes(loadCbaArchive().toPath())) {
                    override fun getFilename(): String {
                        return "test-cba.zip"
                    }
                })
            }.build()

            webTestClient
                    .post()
                    .uri("/api/v1/execution-service/upload")
                    .body(BodyInserters.fromMultipartData(body))
                    .exchange()
                    .expectStatus().isOk
                    .returnResult<String>()
                    .responseBody
                    .awaitSingle()
        }
    }

    private fun loadCbaArchive():File {
        return Paths.get("./src/test/resources/cba-for-kafka-integration.zip").toFile()
    }

    @Configuration
    @EnableKafka
    open class ConsumerConfiguration {

        @Value("\${blueprintsprocessor.messageclient.self-service-api.bootstrapServers}")
        lateinit var bootstrapServers: String

        @Value("\${blueprintsprocessor.messageclient.self-service-api.groupId}")
        lateinit var groupId:String

        @Bean
        open fun consumerFactory2(): ConsumerFactory<String, ExecutionServiceInput>? {
            val configProperties = hashMapOf<String, Any>()
            configProperties[CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
            configProperties[ConsumerConfig.GROUP_ID_CONFIG] = groupId
            configProperties[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
            configProperties[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java.name
            configProperties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
            configProperties[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 1000

            return DefaultKafkaConsumerFactory(configProperties, StringDeserializer(),
                    JsonDeserializer(ExecutionServiceInput::class.java))
        }

        @Bean
        open fun listenerFactory(): ConcurrentKafkaListenerContainerFactory<String, ExecutionServiceInput> {
            val factory = ConcurrentKafkaListenerContainerFactory<String, ExecutionServiceInput>()
            factory.consumerFactory = consumerFactory2()
            return factory
        }
    }
}


