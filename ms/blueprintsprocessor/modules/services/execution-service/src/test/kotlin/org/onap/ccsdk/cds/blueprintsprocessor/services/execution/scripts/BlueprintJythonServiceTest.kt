/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [BlueprintJythonService::class, PythonExecutorProperty::class])
@TestPropertySource(properties =
["blueprints.processor.functions.python.executor.modulePaths=./../../../../../components/scripts/python/ccsdk_blueprints",
    "blueprints.processor.functions.python.executor.executionPath=./../../../../../components/scripts/python/ccsdk_blueprints"])
class BlueprintJythonServiceTest {

    @Autowired
    private lateinit var blueprintJythonService: BlueprintJythonService

    @Test
    fun testGetAbstractPythonPlugin() {
        val bluePrintContext = BluePrintMetadataUtils.getBluePrintContext(
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration")

        val dependencies: MutableMap<String, Any> = hashMapOf()

        val content = JacksonUtils.getContent("./../../../../." +
                "./components/model-catalog/blueprint-model/test-blueprint/baseconfiguration/Scripts/python/SamplePythonComponentNode.py")

        val abstractComponentFunction = blueprintJythonService.jythonInstance<AbstractComponentFunction>(bluePrintContext, "SamplePythonComponentNode", content, dependencies)

        assertNotNull(abstractComponentFunction, "failed to get python component")

        abstractComponentFunction.process(ExecutionServiceInput())

    }
}