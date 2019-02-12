/*
 *  Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.restconf.executor

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.apps.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.BlueprintJythonService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.PythonExecutorProperty
import org.onap.ccsdk.apps.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull


@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [RestconfExecutorConfiguration::class, ComponentRestconfExecutor::class,
    BlueprintJythonService::class, PythonExecutorProperty::class, BluePrintRestLibPropertyService::class,
    BlueprintPropertyConfiguration::class,BluePrintProperties::class])
@TestPropertySource(properties =
["server.port=9111",
    "blueprintsprocessor.restconfEnabled=true",
    "blueprintsprocessor.restclient.odlPrimary.type=basic-auth",
    "blueprintsprocessor.restclient.odlPrimary.url=http://127.0.0.1:9111",
    "blueprintsprocessor.restclient.odlPrimary.userId=sampleuser",
    "blueprintsprocessor.restclient.odlPrimary.token=sampletoken"])
class ComponentRestconfExecutorTest {

    @Autowired
    lateinit var componentRestconfExecutor: ComponentRestconfExecutor

    @Test
    fun `test Restconf Component Instance`() {

        assertNotNull(componentRestconfExecutor, "failed to get ComponentRestconfExecutor instance")
    }


}