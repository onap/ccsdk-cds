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

package org.onap.ccsdk.apps.controllerblueprints.core.script

import org.junit.Ignore
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BluePrintScriptServiceTest {

    @Test
    fun `invoke script`() {
        val scriptContent = "11 + 11"
        val value = BluePrintScriptService()
                .load<Int>(scriptContent)
        assertEquals(22, value, "failed to execute command")
    }

    @Test
    @Ignore
    fun `invoke script component node`() {

        //println(classpathFromClasspathProperty()?.joinToString("\n"))

        val scriptFile = File("src/test/resources/scripts/SampleBlueprintFunctionNode.kts")

        val functionNode = BluePrintScriptService()
                .scriptClassNewInstance<BlueprintFunctionNode<String, String>>(scriptFile,
                        "SampleBlueprintFunctionNode")
        assertNotNull(functionNode, "failed to get instance from script")
    }
}