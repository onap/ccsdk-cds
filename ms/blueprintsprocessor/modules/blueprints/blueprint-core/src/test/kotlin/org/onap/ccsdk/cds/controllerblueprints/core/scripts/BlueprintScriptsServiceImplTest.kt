/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.core.scripts

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintDefinitions
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvm.util.classpathFromClassloader
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty
import kotlin.test.assertNotNull

class BlueprintScriptsServiceImplTest {

    private fun viewClassPathInfo() {

        println(" *********** classpathFromClass  *********** ")
        classpathFromClass(
            BlueprintScriptsServiceImplTest::class.java.classLoader,
            BlueprintScriptsServiceImplTest::class
        )!!
            .forEach(::println)

        println(" *********** classpathFromClassloader  *********** ")
        classpathFromClassloader(BlueprintScriptsServiceImplTest::class.java.classLoader)!!
            .forEach(::println)

        println(" *********** classpathFromClasspathProperty  *********** ")
        classpathFromClasspathProperty()!!
            .forEach(::println)
    }

    @Test
    fun testCachedService() {
        runBlocking {

            val bluePrintScriptsService = BlueprintScriptsServiceImpl()

            val basePath = normalizedPathName("src/test/resources/compile")

            /** Load the Definitions */
            val bluePrintDefinitions = bluePrintScriptsService
                .scriptInstance<BlueprintDefinitions>(
                    basePath,
                    "cba.scripts.ActivateBlueprintDefinitions", true
                )
            assertNotNull(bluePrintDefinitions, "failed to get blueprint definitions")

            val serviceTemplate = bluePrintDefinitions.serviceTemplate()
            assertNotNull(serviceTemplate, "failed to get service template")

            val customDataType = bluePrintDefinitions.otherDefinition<DataType>("datatype-custom-datatype")
            assertNotNull(customDataType, "failed to get custom definitions")

            val instance = bluePrintScriptsService
                .scriptInstance<BlueprintFunctionNode<String, String>>(
                    basePath,
                    "cba.scripts.SampleBlueprintFunctionNode", false
                )
            assertNotNull(instance, "failed to get compiled instance")

            val cachedInstance = bluePrintScriptsService
                .scriptInstance<BlueprintFunctionNode<String, String>>(
                    basePath,
                    "cba.scripts.SampleBlueprintFunctionNode", false
                )
            assertNotNull(cachedInstance, "failed to get cached compile instance")
        }
    }
}
