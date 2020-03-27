/*
 * Copyright © 2019 IBM, Bell Canada.
 * Modifications Copyright © 2019 IBM.
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts

import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.BeforeTest
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BlueprintJythonServiceImpl::class, PythonExecutorProperty::class])
@TestPropertySource(properties =
["blueprints.processor.functions.python.executor.modulePaths=./../../../../components/scripts/python/ccsdk_blueprints",
    "blueprints.processor.functions.python.executor.executionPath=./../../../../components/scripts/python/ccsdk_blueprints"])
class BlueprintJythonServiceTest {

    lateinit var blueprintContext: BluePrintContext
    @Autowired
    private lateinit var blueprintJythonService: BlueprintJythonServiceImpl

    @BeforeTest
    fun init() {
        blueprintContext = mockk<BluePrintContext>()
        every { blueprintContext.rootPath } returns normalizedPathName("target")
    }

    @Test
    fun testGetAbstractPythonPlugin() {
            val content = JacksonUtils.getClassPathFileContent("scripts/SamplePythonComponentNode.py")
            val dependencies: MutableMap<String, Any> = hashMapOf()

            val abstractPythonPlugin = blueprintJythonService
                    .jythonInstance<AbstractComponentFunction>(blueprintContext, "SamplePythonComponentNode",
                            content, dependencies)

            assertNotNull(abstractPythonPlugin, "failed to get python component")
    }

    @Test
    fun testGetAbstractJythonComponent() {
            val scriptInstance = "test-classes/scripts/SamplePythonComponentNode.py"

            val abstractJythonComponent = blueprintJythonService.jythonComponentInstance(blueprintContext, scriptInstance)

            assertNotNull(abstractJythonComponent, "failed to get Jython component")
    }
}
