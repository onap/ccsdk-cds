/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core.api.data

import com.fasterxml.jackson.databind.JsonNode

enum class RemoteScriptType {
    PYTHON, ANSIBLE, KOTLIN, SH
}

data class RemoteIdentifier(var blueprintName: String? = null,
                            var blueprintVersion: String? = null)


data class RemoteScriptExecutionInput(var requestId: String,
                                      var remoteIdentifier: RemoteIdentifier? = null,
                                      var remoteScriptType: RemoteScriptType,
                                      var command: String,
                                      var timeOut: Long = 30,
                                      var properties: MutableMap<String, JsonNode> = hashMapOf()
)


data class RemoteScriptExecutionOutput(var requestId: String, var response: String)

data class PrepareRemoteEnvInput(var requestId: String,
                                 var remoteScriptType: RemoteScriptType,
                                 var packages: MutableList<String>?,
                                 var timeOut: Long = 120,
                                 var properties: MutableMap<String, JsonNode> = hashMapOf()
)