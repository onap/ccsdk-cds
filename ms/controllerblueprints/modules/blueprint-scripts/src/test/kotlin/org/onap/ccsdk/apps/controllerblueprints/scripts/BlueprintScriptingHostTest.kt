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

package org.onap.ccsdk.apps.controllerblueprints.scripts


import org.apache.commons.io.FileUtils
import org.junit.Ignore
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import java.io.File
import kotlin.script.experimental.jvm.util.classpathFromClass
import kotlin.script.experimental.jvm.util.classpathFromClassloader
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class BlueprintScriptingHostTest {

    @Test
    @Ignore
    fun `test classpaths`() {

        println(" *********** classpathFromClass  *********** ")
        classpathFromClass(BlueprintScriptingHostTest::class.java.classLoader,
                BlueprintScriptingHostTest::class)!!
                .forEach(::println)

        println(" *********** classpathFromClassloader  *********** ")
        classpathFromClassloader(BlueprintScriptingHostTest::class.java.classLoader)!!
                .forEach(::println)

        println(" *********** classpathFromClasspathProperty  *********** ")
        classpathFromClasspathProperty()!!
                .forEach(::println)
    }

    @Test
    fun `test same script two folders`() {

        FileUtils.forceMkdir(File("target/scripts1/"))
        FileUtils.forceMkdir(File("target/scripts2/"))

        val scriptSource1 = BluePrintSourceCode()
        scriptSource1.moduleName = "blueprint-test-script"

        scriptSource1.targetJarFile = File("target/scripts1/blueprint-script-generated.jar")
        val sources1: MutableList<String> = arrayListOf()
        sources1.add("src/test/resources/scripts1")
        scriptSource1.blueprintKotlinSources = sources1

        val scriptClassName = "Simple_cba\$SampleComponentFunction"

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<BluePrintKotlinScript>()

        val scriptEvaluator = BluePrintScriptEvaluator<BlueprintFunctionNode<String, String>>(scriptClassName)

        val scriptSource2 = BluePrintSourceCode()
        scriptSource2.moduleName = "blueprint-test-script"

        scriptSource2.targetJarFile = File("target/scripts2/blueprint-script-generated.jar")
        val sources2: MutableList<String> = arrayListOf()
        sources2.add("src/test/resources/scripts2")
        scriptSource2.blueprintKotlinSources = sources2

        for (i in 1..2) {
            val evalResponse = BlueprintScriptingHost(scriptEvaluator).eval(scriptSource1, compilationConfiguration,
                    null)
        }

        for (i in 1..2) {
            val evalResponse = BlueprintScriptingHost(scriptEvaluator).eval(scriptSource2, compilationConfiguration,
                    null)
        }
    }
}