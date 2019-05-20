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
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.concurrent.SettableListenableFuture
import kotlin.test.Test
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@DirtiesContext
@ContextConfiguration(classes = [BluePrintMessageLibConfiguration::class,
    BlueprintPropertyConfiguration::class, BluePrintProperties::class])
@TestPropertySource(properties =
["blueprintsprocessor.messageclient.sample.type=kafka-basic-auth",
    "blueprintsprocessor.messageclient.sample.bootstrapServers=127:0.0.1:9092",
    "blueprintsprocessor.messageclient.sample.topic=default-topic",
    "blueprintsprocessor.messageclient.sample.clientId=default-client-id"
])
open class BlueprintMessageProducerServiceTest {

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Test
    fun testKafkaBasicAuthClientService() {
        runBlocking {
            val bluePrintMessageClientService = bluePrintMessageLibPropertyService
                    .blueprintMessageClientService("sample") as KafkaBasicAuthMessageProducerService

            val mockKafkaTemplate = mockk<KafkaTemplate<String, Any>>()

            val future = SettableListenableFuture<SendResult<String, Any>>()
            //future.setException(BluePrintException("failed sending"))

            every { mockKafkaTemplate.send(any(), any()) } returns future

            val spyBluePrintMessageClientService = spyk(bluePrintMessageClientService, recordPrivateCalls = true)

            every { spyBluePrintMessageClientService.messageTemplate(any()) } returns mockKafkaTemplate

            val response = spyBluePrintMessageClientService.sendMessage("Testing message")
            assertNotNull(response, "failed to get command response")
        }
    }

}



