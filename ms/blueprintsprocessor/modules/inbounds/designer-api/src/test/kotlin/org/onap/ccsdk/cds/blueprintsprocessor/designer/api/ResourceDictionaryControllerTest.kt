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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.db.BluePrintDBLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class,
        BluePrintPropertyConfiguration::class, BluePrintPropertiesService::class, BluePrintDBLibConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ResourceDictionaryControllerTest {

    @Autowired
    lateinit var resourceDictionaryController: ResourceDictionaryController

    @Test
    fun testResourceDictionaryControllerPresence() {
        assertNotNull(resourceDictionaryController, "failed to initialise ResourceDictionaryController")
    }
}
