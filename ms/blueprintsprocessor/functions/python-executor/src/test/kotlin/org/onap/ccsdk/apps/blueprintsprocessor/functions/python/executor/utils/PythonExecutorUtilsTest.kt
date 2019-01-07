/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.utils

import org.junit.Test
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import kotlin.test.assertNotNull


class PythonExecutorUtilsTest {

    private val log = LoggerFactory.getLogger(PythonExecutorUtils::class.java)

    @Test
    fun testGetPythonComponent() {

        val pythonPath: MutableList<String> = arrayListOf()
        pythonPath.add("./../../../../components/scripts/python/ccsdk_blueprints")

        val properties: MutableMap<String, Any> = hashMapOf()
        properties["logger"] = log

        val content = JacksonUtils.getContent("./../../../../components/scripts/python/ccsdk_blueprints/sample_blueprint_component.py")

        val abstractComponentFunction = PythonExecutorUtils.getPythonComponent("./../../../../components/scripts/python/ccsdk_blueprints", pythonPath, content,
                "SampleBlueprintComponent", properties)

        assertNotNull(abstractComponentFunction, "failed to get python component")

        abstractComponentFunction.process(ExecutionServiceInput())

    }


}

