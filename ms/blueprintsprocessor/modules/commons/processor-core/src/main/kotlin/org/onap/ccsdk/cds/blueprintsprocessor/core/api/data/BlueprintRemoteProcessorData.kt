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
import java.util.*

enum class RemoteScriptType {
    PYTHON, ANSIBLE, KOTLIN, SH
}

enum class StatusType {
    SUCCESS, FAILURE
}

data class RemoteIdentifier(var blueprintName: String,
                            var blueprintVersion: String)


data class RemoteScriptExecutionInput(var requestId: String,
                                      var correlationId: String? = null,
                                      var remoteIdentifier: RemoteIdentifier? = null,
                                      var remoteScriptType: RemoteScriptType,
                                      var command: String,
                                      var timeOut: Long = 30,
                                      var properties: MutableMap<String, JsonNode> = hashMapOf()
)


data class RemoteScriptExecutionOutput(var requestId: String,
                                       var response: String,
                                       var status: StatusType = StatusType.SUCCESS,
                                       var timestamp: Date = Date())

data class PrepareRemoteEnvInput(var requestId: String,
                                 var correlationId: String? = null,
                                 var remoteIdentifier: RemoteIdentifier? = null,
                                 var remoteScriptType: RemoteScriptType,
                                 var packages: MutableList<String>?,
                                 var timeOut: Long = 120,
                                 var properties: MutableMap<String, JsonNode> = hashMapOf()
)