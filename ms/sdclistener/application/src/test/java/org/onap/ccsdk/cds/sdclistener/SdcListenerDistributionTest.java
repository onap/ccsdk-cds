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

package org.onap.ccsdk.cds.sdclistener;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.onap.sdc.utils.DistributionActionResultEnum.SUCCESS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.ZipFile;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementOutput;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintManagementServiceGrpc;
import org.onap.ccsdk.cds.controllerblueprints.management.api.BluePrintUploadInput;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerAuthClientInterceptor;
import org.onap.ccsdk.cds.sdclistener.client.SdcListenerClient;
import org.onap.ccsdk.cds.sdclistener.dto.SdcListenerDto;
import org.onap.ccsdk.cds.sdclistener.handler.BluePrintProcesssorHandler;
import org.onap.ccsdk.cds.sdclistener.service.ListenerServiceImpl;
import org.onap.ccsdk.cds.sdclistener.status.SdcListenerStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaKraftBroker;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SdcListenerDistributionTest.TestConfig.class)
@DirtiesContext
public class SdcListenerDistributionTest {

    private static final String NOTIFICATION_TOPIC = "SDC-DISTR-NOTIF-TOPIC-TEST";
    private static final String STATUS_TOPIC = "SDC-DISTR-STATUS-TOPIC-TEST";
    private static final String CONSUMER_GROUP = "cds-sdc-listener-test";
    private static final String CONSUMER_ID = "cds-sdc-listener-test-consumer";
    private static final String DISTRIBUTION_ID = "8c1f6c3e-2d4b-4b8e-9a61-3f0d2c5e7a19";
    private static final String CSAR_FILE = "service-ServicePnfTest-csar.csar";
    private static final Path CSAR_PATH = Paths.get("src/test/resources", CSAR_FILE);
    private static final String CBA_ENTRY =
            "Artifacts/org.openecomp.resource.pnf.PnfTest2_v1.0/Deployment/CONTROLLER_BLUEPRINT_ARCHIVE/vDNS.zip";
    private static final String ARTIFACT_URL = "/sdc/v1/catalog/services/ServicePnfTest/1.0/artifacts/" + CSAR_FILE;
    private static final Duration TIMEOUT = Duration.ofSeconds(90);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @ClassRule
    public static final TemporaryFolder ARCHIVE_DIR = new TemporaryFolder();

    private static final Queue<BluePrintUploadInput> UPLOADS = new ConcurrentLinkedQueue<>();
    private static EmbeddedKafkaBroker kafka;
    private static WireMockServer sdc;
    private static Server blueprintProcessor;

    @Autowired
    private SdcListenerDto listenerDto;

    @Configuration
    @Import({SdcListenerClient.class, SdcListenerNotificationCallback.class, SdcListenerDto.class,
            SdcListenerAuthClientInterceptor.class, ListenerServiceImpl.class, BluePrintProcesssorHandler.class,
            SdcListenerStatus.class})
    static class TestConfig {

        // IConfiguration defaults to SASL_PLAINTEXT and otherwise reads SASL_JAAS_CONFIG from the environment.
        @Bean
        SdcListenerConfiguration sdcListenerConfiguration() {
            return new SdcListenerConfiguration() {
                @Override
                public String getKafkaSecurityProtocolConfig() {
                    return "PLAINTEXT";
                }

                @Override
                public String getKafkaSaslJaasConfig() {
                    return "";
                }
            };
        }
    }

    @BeforeClass
    public static void startBackends() throws Exception {
        kafka = new EmbeddedKafkaKraftBroker(1, 1, NOTIFICATION_TOPIC, STATUS_TOPIC)
                .brokerProperties(Map.of("group.initial.rebalance.delay.ms", "0"));
        kafka.afterPropertiesSet();
        startConsumerGroupAtBeginningOfNotificationTopic();
        publishNotification();

        sdc = new WireMockServer(wireMockConfig().dynamicPort());
        sdc.start();
        sdc.stubFor(get(urlEqualTo("/sdc/v1/artifactTypes")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json").withBody("[\"TOSCA_CSAR\",\"TOSCA_TEMPLATE\"]")));
        sdc.stubFor(get(urlEqualTo("/sdc/v1/distributionKafkaData"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json")
                        .withBody(MAPPER.createObjectNode().put("kafkaBootStrapServer", kafka.getBrokersAsString())
                                .put("distrNotificationTopicName", NOTIFICATION_TOPIC)
                                .put("distrStatusTopicName", STATUS_TOPIC).toString())));
        sdc.stubFor(get(urlEqualTo(ARTIFACT_URL))
                .willReturn(aResponse().withHeader("Content-Type", "application/octet-stream")
                        .withHeader("Content-Disposition", "attachment; filename=\"" + CSAR_FILE + "\"")
                        .withBody(Files.readAllBytes(CSAR_PATH))));

        blueprintProcessor = ServerBuilder.forPort(0)
                .addService(new BluePrintManagementServiceGrpc.BluePrintManagementServiceImplBase() {
                    @Override
                    public void uploadBlueprint(BluePrintUploadInput request,
                            StreamObserver<BluePrintManagementOutput> responseObserver) {
                        UPLOADS.add(request);
                        responseObserver.onNext(BluePrintManagementOutput.newBuilder()
                                .setStatus(Status.newBuilder().setCode(200).setMessage("uploaded")).build());
                        responseObserver.onCompleted();
                    }
                }).build().start();
    }

    @AfterClass
    public static void stopBackends() {
        blueprintProcessor.shutdownNow();
        sdc.stop();
        kafka.destroy();
    }

    @DynamicPropertySource
    static void listenerProperties(DynamicPropertyRegistry registry) {
        registry.add("listenerservice.config.sdcAddress", () -> "localhost:" + sdc.port());
        registry.add("listenerservice.config.isUseHttpsWithSDC", () -> "false");
        registry.add("listenerservice.config.consumerGroup", () -> CONSUMER_GROUP);
        registry.add("listenerservice.config.consumerId", () -> CONSUMER_ID);
        registry.add("listenerservice.config.pollingInterval", () -> "15");
        registry.add("listenerservice.config.pollingTimeout", () -> "15");
        registry.add("listenerservice.config.archivePath", () -> ARCHIVE_DIR.getRoot().getAbsolutePath());
        registry.add("listenerservice.config.grpcAddress", () -> "localhost");
        registry.add("listenerservice.config.grpcPort", () -> blueprintProcessor.getPort());
    }

    @Test
    public void distributedCbaIsUploadedToBlueprintProcessorAndReportedToSdc() throws Exception {
        List<String> statuses = awaitStatusesUntilComponentDone();

        assertEquals(Arrays.asList("NOTIFIED", "DOWNLOAD_OK", "COMPONENT_DONE_OK"), statuses);
        sdc.verify(getRequestedFor(urlEqualTo(ARTIFACT_URL)));
        assertEquals(1, UPLOADS.size());
        assertArrayEquals(cbaFromCsar(), UPLOADS.peek().getFileChunk().getChunk().toByteArray());
        assertEquals(SUCCESS, listenerDto.getDistributionClient().stop().getDistributionActionResult());
    }

    // The client consumes with auto.offset.reset=latest. Committing offset 0 for its group up front lets the
    // notification be published before the client starts without racing the partition assignment.
    private static void startConsumerGroupAtBeginningOfNotificationTopic() throws Exception {
        try (Admin admin =
                Admin.create(Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBrokersAsString()))) {
            admin.alterConsumerGroupOffsets(CONSUMER_GROUP,
                    Map.of(new TopicPartition(NOTIFICATION_TOPIC, 0), new OffsetAndMetadata(0))).all().get();
        }
    }

    private static void publishNotification() throws Exception {
        Map<String, Object> props = Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBrokersAsString(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            producer.send(new ProducerRecord<>(NOTIFICATION_TOPIC, notification())).get();
        }
    }

    private static String notification() {
        ObjectNode artifact = MAPPER.createObjectNode().put("artifactName", CSAR_FILE).put("artifactType", "TOSCA_CSAR")
                .put("artifactURL", ARTIFACT_URL)
                .put("artifactChecksum", "ZGVhZmM4YTM3NDgwYjQxNDFmZjYzYzQ5N2E4NDFkNDE=")
                .put("artifactDescription", "TOSCA representation of the service").put("artifactTimeout", 0)
                .put("artifactUUID", "27a07164-8d63-4d7e-a8b3-8422e938b37c").put("artifactVersion", "1");
        ObjectNode notification =
                MAPPER.createObjectNode().put("distributionID", DISTRIBUTION_ID).put("serviceName", "ServicePnfTest")
                        .put("serviceVersion", "1.0").put("serviceUUID", "1b8b0466-f7f2-4616-8198-055228f8d1eb")
                        .put("serviceDescription", "sdc listener test service")
                        .put("serviceInvariantUUID", "8939eac6-07f9-4395-b7ab-fe5c1a3e7cfe")
                        .put("workloadContext", "Production");
        notification.putArray("serviceArtifacts").add(artifact);
        notification.putArray("resources");
        return notification.toString();
    }

    private static List<String> awaitStatusesUntilComponentDone() throws IOException {
        Map<String, Object> props = Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        List<String> statuses = new ArrayList<>();
        long deadline = System.nanoTime() + TIMEOUT.toNanos();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            TopicPartition statusPartition = new TopicPartition(STATUS_TOPIC, 0);
            consumer.assign(List.of(statusPartition));
            consumer.seekToBeginning(List.of(statusPartition));
            while (!isComponentDone(statuses) && System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    JsonNode status = MAPPER.readTree(record.value());
                    if (DISTRIBUTION_ID.equals(status.path("distributionID").asText())) {
                        statuses.add(status.path("status").asText());
                    }
                }
            }
        }
        return statuses;
    }

    private static boolean isComponentDone(List<String> statuses) {
        return !statuses.isEmpty() && statuses.get(statuses.size() - 1).startsWith("COMPONENT_DONE");
    }

    private static byte[] cbaFromCsar() throws IOException {
        try (ZipFile csar = new ZipFile(CSAR_PATH.toFile());
                InputStream cba = csar.getInputStream(csar.getEntry(CBA_ENTRY))) {
            return cba.readAllBytes();
        }
    }
}
