/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.nats.service

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.nats.BlueprintNatsLibConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.nats.NatsLibConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertTrue

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BlueprintNatsLibConfiguration::class,
        BlueprintPropertyConfiguration::class, BlueprintPropertiesService::class
    ]
)
@TestPropertySource(
    properties =
        [
            "blueprintsprocessor.nats.cds-controller.type=token-auth",
            "blueprintsprocessor.nats.cds-controller.host=nats://localhost:4222",
            "blueprintsprocessor.nats.cds-controller.token=tokenAuth"
        ]
)
class BlueprintNatsLibPropertyServiceTest {

    @Autowired
    lateinit var bluePrintNatsLibPropertyService: BlueprintNatsLibPropertyService

    @Test
    fun testNatsProperties() {
        assertTrue(::bluePrintNatsLibPropertyService.isInitialized)
        bluePrintNatsLibPropertyService.bluePrintNatsService(NatsLibConstants.DEFULT_NATS_SELECTOR)
    }
}
