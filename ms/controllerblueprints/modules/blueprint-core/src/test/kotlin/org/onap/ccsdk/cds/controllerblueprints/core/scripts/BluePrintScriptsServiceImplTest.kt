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

package org.onap.ccsdk.cds.controllerblueprints.core.scripts


import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvm.util.classpathFromClassloader
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty
import kotlin.test.assertNotNull

class BluePrintScriptsServiceImplTest {

    private fun viewClassPathInfo() {

        println(" *********** classpathFromClass  *********** ")
        classpathFromClass(BluePrintScriptsServiceImplTest::class.java.classLoader,
                BluePrintScriptsServiceImplTest::class)!!
                .forEach(::println)

        println(" *********** classpathFromClassloader  *********** ")
        classpathFromClassloader(BluePrintScriptsServiceImplTest::class.java.classLoader)!!
                .forEach(::println)

        println(" *********** classpathFromClasspathProperty  *********** ")
        classpathFromClasspathProperty()!!
                .forEach(::println)
    }

    @Test
    fun testCachedService() {
        runBlocking {
            val blueprintContext = mockk<BluePrintContext>()

            val metadata: MutableMap<String, String> = hashMapOf()
            metadata[BluePrintConstants.METADATA_TEMPLATE_NAME] = "testing-bp"
            metadata[BluePrintConstants.METADATA_TEMPLATE_VERSION] = "1.0.0"

            every { blueprintContext.rootPath } returns normalizedPathName("src/test/resources/compile")
            every { blueprintContext.metadata } returns metadata
            every { blueprintContext.name() } returns "testing-bp"
            every { blueprintContext.version() } returns "1.0.0"

            val bluePrintScriptsService = BluePrintScriptsServiceImpl()

            val instance = bluePrintScriptsService.scriptInstance<BlueprintFunctionNode<String, String>>(blueprintContext,
                    "cba.scripts.SampleBlueprintFunctionNode", true)
            assertNotNull(instance, "failed to get compiled instance")

            val cachedInstance = bluePrintScriptsService.scriptInstance<BlueprintFunctionNode<String, String>>(blueprintContext,
                    "cba.scripts.SampleBlueprintFunctionNode", false)
            assertNotNull(cachedInstance, "failed to get cached compile instance")
        }
    }

}