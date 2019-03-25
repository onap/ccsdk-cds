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

package org.onap.ccsdk.cds.controllerblueprints.scripts

import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.util.classpathFromClasspathProperty

@KotlinScript(
        fileExtension = "cba.kts",
        compilationConfiguration = BluePrintScripCompilationConfiguration::class,
        displayName = "Controller Blueprint Archive Kotlin Scripts"
)
abstract class BluePrintKotlinScript

object BluePrintScripCompilationConfiguration : ScriptCompilationConfiguration(
        {
            jvm {
                //classpathFromClassloader(BluePrintScripCompilationConfiguration::class.java.classLoader)
                classpathFromClasspathProperty()
            }
            ide{
                acceptedLocations(ScriptAcceptedLocation.Everywhere)
            }

        }
)

open class BluePrintSourceCode : SourceCode {
    lateinit var blueprintKotlinSources: MutableList<String>
    lateinit var moduleName: String
    lateinit var targetJarFile: File
    var regenerate: Boolean = false

    override val text: String
        get() = ""

    override val locationId: String? = null

    override val name: String?
        get() = moduleName
}
