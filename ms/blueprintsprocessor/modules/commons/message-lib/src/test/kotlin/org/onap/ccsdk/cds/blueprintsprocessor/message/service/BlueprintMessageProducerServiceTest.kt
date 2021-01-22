/*
 *  Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.message.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.security.scram.ScramLoginModule
import org.apache.kafka.common.serialization.ByteArraySerializer
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BlueprintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.MessageLibConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.Future
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(
    classes = [
        BlueprintMessageLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.messageproducer.sample.type=kafka-scram-ssl-auth",
            "blueprintsprocessor.messageproducer.sample.bootstrapServers=127.0.0.1:9092",
            "blueprintsprocessor.messageproducer.sample.topic=default-topic",
            "blueprintsprocessor.messageproducer.sample.clientId=default-client-id",
            "blueprintsprocessor.messageproducer.sample.truststore=/path/to/truststore.jks",
            "blueprintsprocessor.messageproducer.sample.truststorePassword=secretpassword",
            "blueprintsprocessor.messageproducer.sample.keystore=/path/to/keystore.jks",
            "blueprintsprocessor.messageproducer.sample.keystorePassword=secretpassword",
            "blueprintsprocessor.messageproducer.sample.scramUsername=sample-user",
            "blueprintsprocessor.messageproducer.sample.scramPassword=secretpassword"
        ]
)
open class BlueprintMessageProducerServiceTest {

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BlueprintMessageLibPropertyService

    @Test
    fun testKafkaScramSslAuthProducerService() {
        runBlocking {
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                .blueprintMessageProducerService("sample") as KafkaMessageProducerService

            val mockKafkaTemplate = mockk<KafkaProducer<String, ByteArray>>()

            val responseMock = mockk<Future<RecordMetadata>>()
            every { responseMock.get() } returns mockk()

            every { mockKafkaTemplate.send(any(), any()) } returns responseMock

            val spyBlueprintMessageProducerService = spyk(blueprintMessageProducerService, recordPrivateCalls = true)

            every { spyBlueprintMessageProducerService.messageTemplate(any()) } returns mockKafkaTemplate

            val response = spyBlueprintMessageProducerService.sendMessage("mykey", "Testing message")
            assertTrue(response, "failed to get command response")
        }
    }

    @Test
    fun testKafkaScramSslAuthConfig() {
        val expectedConfig = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "127.0.0.1:9092",
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to ByteArraySerializer::class.java,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.MAX_BLOCK_MS_CONFIG to 250,
            ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG to 60 * 60 * 1000,
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to true,
            ConsumerConfig.CLIENT_ID_CONFIG to "default-client-id",
            CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SASL_SSL.toString(),
            SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to "/path/to/truststore.jks",
            SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to "secretpassword",
            SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to "/path/to/keystore.jks",
            SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "JKS",
            SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to "secretpassword",
            SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to SslConfigs.DEFAULT_SSL_ENDPOINT_IDENTIFICATION_ALGORITHM,
            SaslConfigs.SASL_MECHANISM to "SCRAM-SHA-512",
            SaslConfigs.SASL_JAAS_CONFIG to "${ScramLoginModule::class.java.canonicalName} required " +
                "username=\"sample-user\" " +
                "password=\"secretpassword\";"
        )

        val messageProducerProperties = bluePrintMessageLibPropertyService
            .messageProducerProperties("${MessageLibConstants.PROPERTY_MESSAGE_PRODUCER_PREFIX}sample")

        val configProps = messageProducerProperties.getConfig()

        assertEquals(
            messageProducerProperties.topic,
            "default-topic",
            "Topic doesn't match the expected value"
        )
        assertEquals(
            messageProducerProperties.type,
            "kafka-scram-ssl-auth",
            "Authentication type doesn't match the expected value"
        )

        expectedConfig.forEach {
            assertTrue(
                configProps.containsKey(it.key),
                "Missing expected kafka config key : ${it.key}"
            )
            assertEquals(
                configProps[it.key],
                it.value,
                "Unexpected value for ${it.key} got ${configProps[it.key]} instead of ${it.value}"
            )
        }
    }
}
