/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import org.onap.ccsdk.cds.controllerblueprints.core.scripts.BlueprintSourceCode

interface BlueprintScriptsService {

    suspend fun <T> scriptInstance(bluePrintSourceCode: BlueprintSourceCode, scriptClassName: String): T

    suspend fun <T> scriptInstance(
        blueprintBasePath: String,
        artifactName: String,
        artifactVersion: String,
        scriptClassName: String,
        reCompile: Boolean
    ): T

    suspend fun <T> scriptInstance(blueprintBasePath: String, scriptClassName: String, reCompile: Boolean): T

    suspend fun <T> scriptInstance(cacheKey: String, scriptClassName: String): T

    suspend fun <T> scriptInstance(scriptClassName: String): T
}
