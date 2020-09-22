/*
 * Copyright (C) 2019 Bell Canada.
 * Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core.utils

import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.protobuf.Struct
import com.google.protobuf.util.JsonFormat
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ACTION_MODE_SYNC
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.common.api.Flag
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.currentTimestamp
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput

// ACTION IDENTIFIER

fun org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers.toProto(): ActionIdentifiers {
    val actionIdentifier = ActionIdentifiers.newBuilder()
    actionIdentifier.actionName = this.actionName
    actionIdentifier.blueprintName = this.blueprintName
    actionIdentifier.blueprintVersion = this.blueprintVersion
    actionIdentifier.mode = this.mode
    return actionIdentifier.build()
}

fun ActionIdentifiers.toJava(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers {
    val actionIdentifier = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ActionIdentifiers()
    actionIdentifier.actionName = this.actionName
    actionIdentifier.blueprintName = this.blueprintName
    actionIdentifier.blueprintVersion = this.blueprintVersion
    actionIdentifier.mode = this.mode
    return actionIdentifier
}

// COMMON HEADER

fun org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader.toProto(): CommonHeader {
    val commonHeader = CommonHeader.newBuilder()
    commonHeader.originatorId = this.originatorId
    commonHeader.requestId = this.requestId
    commonHeader.subRequestId = this.subRequestId
    commonHeader.timestamp = this.timestamp.currentTimestamp()
    if (this.flags != null) {
        commonHeader.flag = this.flags!!.toProto()
    }
    return commonHeader.build()
}

fun CommonHeader.toJava(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader {
    val commonHeader = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.CommonHeader()
    commonHeader.originatorId = this.originatorId
    commonHeader.requestId = this.requestId
    commonHeader.subRequestId = this.subRequestId
    commonHeader.timestamp = if (!this.timestamp.isNullOrEmpty()) {
        this.timestamp!!.toControllerDate()
    } else {
        controllerDate()
    }
    commonHeader.flags = this.flag?.toJava()
    return commonHeader
}

// FLAG

fun org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Flags.toProto(): Flag {
    val flag = Flag.newBuilder()
    flag.isForce = this.isForce
    flag.ttl = this.ttl
    return flag.build()
}

fun Flag.toJava(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Flags {
    val flag = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Flags()
    flag.isForce = this.isForce
    flag.ttl = this.ttl
    return flag
}

// STATUS

fun org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.Status.toProto(): Status {
    val status = Status.newBuilder()
    status.code = this.code
    status.errorMessage = this.errorMessage ?: ""
    status.message = this.message
    status.timestamp = this.timestamp.toString()
    status.eventType = EventType.valueOf(this.eventType)
    return status.build()
}

// EXECUTION INPUT

fun ExecutionServiceInput.toJava(): org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput {
    val executionServiceInput = org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput()
    executionServiceInput.actionIdentifiers = this.actionIdentifiers.toJava()
    executionServiceInput.commonHeader = this.commonHeader.toJava()
    executionServiceInput.payload = JacksonUtils.jsonNode(JsonFormat.printer().print(this.payload)) as ObjectNode
    return executionServiceInput
}

// EXECUTION OUPUT

fun org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput.toProto(): ExecutionServiceOutput {
    val executionServiceOuput = ExecutionServiceOutput.newBuilder()
    executionServiceOuput.actionIdentifiers = this.actionIdentifiers.toProto()
    executionServiceOuput.commonHeader = this.commonHeader.toProto()
    executionServiceOuput.status = this.status.toProto()
    val struct = Struct.newBuilder()
    JsonFormat.parser().merge(JacksonUtils.getJson(this.payload), struct)
    executionServiceOuput.payload = struct.build()
    return executionServiceOuput.build()
}

/** Create proto common header with [requestId] [subRequestId] and [originator] */
fun createCommonHeaderProto(
    requestId: String,
    subRequestId: String,
    originator: String
) = CommonHeader.newBuilder()
    .setTimestamp(currentTimestamp())
    .setOriginatorId(originator)
    .setRequestId(requestId)
    .setSubRequestId(subRequestId).build()!!

/** Create proto action identifiers with [name] [version] and [action] */
fun createActionIdentifiersProto(
    name: String,
    version: String,
    action: String
) = ActionIdentifiers.newBuilder()
    .setBlueprintName(name)
    .setBlueprintVersion(version)
    .setActionName(action)
    .setMode(ACTION_MODE_SYNC)
    .build()!!

/** Create proto status with [message] and [code] */
fun createStatus(
    message: String,
    code: Int
) = Status.newBuilder()
    .setTimestamp(currentTimestamp())
    .setMessage(message)
    .setCode(code)
    .build()!!

/** Create ExecutionServiceInput using [commonHeader], [actionIdentifier] and response payload [jsonContent] */
fun createExecutionServiceInputProto(
    commonHeader: CommonHeader,
    actionIdentifier: ActionIdentifiers,
    jsonContent: String
): ExecutionServiceInput {

    val payloadBuilder = ExecutionServiceInput.newBuilder().payloadBuilder
    JsonFormat.parser().merge(jsonContent, payloadBuilder)

    return ExecutionServiceInput.newBuilder()
        .setCommonHeader(commonHeader)
        .setActionIdentifiers(actionIdentifier)
        .setPayload(payloadBuilder.build())
        .build()
}

/** Create ExecutionServiceOutput using [commonHeader], [actionIdentifier] [status]and
 * response payload [jsonContent]
 * */
fun createExecutionServiceOutputProto(
    commonHeader: CommonHeader,
    actionIdentifier: ActionIdentifiers,
    status: Status,
    jsonContent: String
): ExecutionServiceOutput {

    val payloadBuilder = ExecutionServiceOutput.newBuilder().payloadBuilder
    JsonFormat.parser().merge(jsonContent, payloadBuilder)

    return ExecutionServiceOutput.newBuilder()
        .setCommonHeader(commonHeader)
        .setActionIdentifiers(actionIdentifier)
        .setStatus(status)
        .setPayload(payloadBuilder.build())
        .build()
}
