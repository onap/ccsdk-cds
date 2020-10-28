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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import io.micrometer.core.instrument.MeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.BluePrintMessageLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.message.service.BluePrintMessageLibPropertyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BluePrintMessageLibConfiguration::class, SelfServiceApiTestConfiguration::class,
        BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class, ErrorCatalogTestConfiguration::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BluePrintProcessingKafkaConsumerTest {

    @MockBean
    lateinit var meterRegistry: MeterRegistry

    @Autowired
    lateinit var bluePrintMessageLibPropertyService: BluePrintMessageLibPropertyService

    @Test
    fun testExecutionInputMessageConsumer() {
        runBlocking {
            assertNotNull(
                bluePrintMessageLibPropertyService,
                "failed to initialise bluePrintMessageLibPropertyService"
            )

            val executionServiceHandle = mockk<ExecutionServiceHandler>()

            coEvery { executionServiceHandle.doProcess(any()) } returns mockk()

            val bluePrintProcessingKafkaConsumer = BluePrintProcessingKafkaConsumer(
                bluePrintMessageLibPropertyService,
                executionServiceHandle
            )

            launch {
                bluePrintProcessingKafkaConsumer.setupMessageListener()
            }
            delay(100)
            bluePrintProcessingKafkaConsumer.shutdownMessageListener()
        }
    }
}
