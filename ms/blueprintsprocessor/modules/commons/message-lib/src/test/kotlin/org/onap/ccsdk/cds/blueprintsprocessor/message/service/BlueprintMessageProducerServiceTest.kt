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
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.RecordMetadata
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.util.concurrent.Future
import kotlin.test.Test
import kotlin.test.assertTrue


@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(classes = [BluePrintMessageLibConfiguration::class,
    BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class])
@TestPropertySource(properties =
["blueprintsprocessor.messageproducer.sample.type=kafka-basic-auth",
    "blueprintsprocessor.messageproducer.sample.bootstrapServers=127.0.0.1:9092",
    "blueprintsprocessor.messageproducer.sample.topic=default-topic",
    "blueprintsprocessor.messageproducer.sample.clientId=default-client-id"
])
open class BlueprintMessageProducerServiceTest {

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Test
    fun testKafkaBasicAuthProducertService() {
        runBlocking {
            val blueprintMessageProducerService = bluePrintMessageLibPropertyService
                    .blueprintMessageProducerService("sample") as KafkaBasicAuthMessageProducerService

            val mockKafkaTemplate = mockk<KafkaProducer<String, ByteArray>>()

            val responseMock = mockk<Future<RecordMetadata>>()
            every { responseMock.get() } returns mockk()

            every { mockKafkaTemplate.send(any(), any()) } returns responseMock

            val spyBluePrintMessageProducerService = spyk(blueprintMessageProducerService, recordPrivateCalls = true)

            every { spyBluePrintMessageProducerService.messageTemplate(any()) } returns mockKafkaTemplate

            val response = spyBluePrintMessageProducerService.sendMessage("Testing message")
            assertTrue(response, "failed to get command response")
        }
    }

}



