/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2020 Bell Canada.
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
import com.google.protobuf.ByteString
import java.util.Date

enum class StatusType {
    SUCCESS, FAILURE
}

/* TODO: Group fields into another struct containing originatorId, requestId, subRequestId, correlationId generally go together */
// timeOuts are in seconds

data class RemoteIdentifier(
    var blueprintName: String,
    var blueprintVersion: String,
    var blueprintUUID: String
)

data class RemoteScriptUploadBlueprintInput(
    val remoteIdentifier: RemoteIdentifier? = null,
    val requestId: String,
    val subRequestId: String,
    val originatorId: String,
    val correlationId: String? = null,
    val timeOut: Long = 30,
    val archiveType: String = "CBA_ZIP",
    val binData: ByteString
)

data class RemoteScriptUploadBlueprintOutput(
    val requestId: String,
    val subRequestId: String,
    val status: StatusType = StatusType.SUCCESS,
    val timestamp: Date = Date(),
    val payload: JsonNode
)

data class RemoteScriptExecutionInput(
    var originatorId: String,
    var requestId: String,
    var subRequestId: String,
    var correlationId: String? = null,
    var remoteIdentifier: RemoteIdentifier? = null,
    var command: String,
    var timeOut: Long = 30,
    var properties: MutableMap<String, JsonNode> = hashMapOf()
)

data class RemoteScriptExecutionOutput(
    var requestId: String,
    var response: List<String>,
    var status: StatusType = StatusType.SUCCESS,
    var timestamp: Date = Date(),
    var payload: JsonNode
)

data class PrepareRemoteEnvInput(
    var originatorId: String,
    var requestId: String,
    var subRequestId: String,
    var correlationId: String? = null,
    var remoteIdentifier: RemoteIdentifier? = null,
    var packages: JsonNode,
    var timeOut: Long = 120,
    var properties: MutableMap<String, JsonNode> = hashMapOf()
)
