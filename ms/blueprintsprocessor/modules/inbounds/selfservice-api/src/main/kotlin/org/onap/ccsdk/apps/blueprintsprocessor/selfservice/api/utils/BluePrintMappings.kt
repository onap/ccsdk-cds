/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.utils

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.base.Strings
import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.google.protobuf.util.JsonFormat
import org.onap.ccsdk.apps.controllerblueprints.common.api.ActionIdentifiers
import org.onap.ccsdk.apps.controllerblueprints.common.api.CommonHeader
import org.onap.ccsdk.apps.controllerblueprints.common.api.Flag
import org.onap.ccsdk.apps.controllerblueprints.common.api.Status
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput
import java.text.SimpleDateFormat
import java.util.*

private val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

// STRUCT

fun Struct.toJava(): ObjectNode {
    val objectNode = JsonNodeFactory.instance.objectNode()
    return getNode(objectNode)
}

fun Struct.getNode(objectNode: ObjectNode): ObjectNode {
    this.fieldsMap.forEach {
        when (it.value.kindCase.name) {
            "BOOL_VALUE" -> objectNode.put(it.key, it.value.boolValue)
            "KIND_NOT_SET" -> objectNode.put(it.key, it.value.toByteArray())
            "LIST_VALUE" -> {
                val arrayNode = JsonNodeFactory.instance.arrayNode()
                it.value.listValue.valuesList.forEach { arrayNode.addPOJO(it.getValue()) }
                objectNode.put(it.key, arrayNode)
            }
            "NULL_VALUE" -> objectNode.put(it.key, JsonNodeFactory.instance.nullNode())
            "NUMBER_VALUE" -> objectNode.put(it.key, it.value.numberValue)
            "STRING_VALUE" -> objectNode.put(it.key, it.value.stringValue)
            "STRUCT_VALUE" -> objectNode.put(it.key, it.value.structValue.getNode(JsonNodeFactory.instance.objectNode()))
        }
    }
    return objectNode
}

fun Value.getValue(): Any {
    return when (this.kindCase.name) {
        "BOOL_VALUE" -> this.boolValue
        "KIND_NOT_SET" -> this.toByteArray()
        "LIST_VALUE" -> listOf(this.listValue.valuesList.forEach { it.getValue() })
        "NULL_VALUE" -> JsonNodeFactory.instance.nullNode()
        "NUMBER_VALUE" -> this.numberValue
        "STRING_VALUE" -> this.stringValue
        "STRUCT_VALUE" -> this.structValue.getNode(JsonNodeFactory.instance.objectNode())
        else -> {
            "undefined"
        }
    }
}

// ACTION IDENTIFIER

fun org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ActionIdentifiers.toProto(): ActionIdentifiers {
    val actionIdentifier = ActionIdentifiers.newBuilder()
    actionIdentifier.actionName = this.actionName
    actionIdentifier.blueprintName = this.blueprintName
    actionIdentifier.blueprintVersion = this.blueprintVersion
    actionIdentifier.mode = this.mode
    return actionIdentifier.build()
}

fun ActionIdentifiers.toJava(): org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ActionIdentifiers {
    val actionIdentifier = org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ActionIdentifiers()
    actionIdentifier.actionName = this.actionName
    actionIdentifier.blueprintName = this.blueprintName
    actionIdentifier.blueprintVersion = this.blueprintVersion
    actionIdentifier.mode = this.mode
    return actionIdentifier
}

// COMMON HEADER

fun org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.CommonHeader.toProto(): CommonHeader {
    val commonHeader = CommonHeader.newBuilder()
    commonHeader.originatorId = this.originatorId
    commonHeader.requestId = this.requestId
    commonHeader.subRequestId = this.subRequestId
    commonHeader.timestamp = this.timestamp.toString()
    commonHeader.flag = this.flags?.toProto()
    return commonHeader.build()
}

fun CommonHeader.toJava(): org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.CommonHeader {
    val commonHeader = org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.CommonHeader()
    commonHeader.originatorId = this.originatorId
    commonHeader.requestId = this.requestId
    commonHeader.subRequestId = this.subRequestId
    commonHeader.timestamp = if (!Strings.isNullOrEmpty(this.timestamp)) {
        formatter.parse(this.timestamp)
    } else {
        Date()
    }
    commonHeader.flags = this.flag?.toJava()
    return commonHeader
}

// FLAG

fun org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Flags.toProto(): Flag {
    val flag = Flag.newBuilder()
    flag.isForce = this.isForce
    flag.ttl = this.ttl
    return flag.build()
}

fun Flag.toJava(): org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Flags {
    val flag = org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Flags()
    flag.isForce = this.isForce
    flag.ttl = this.ttl
    return flag
}

// STATUS

fun org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.Status.toProto(): Status {
    val status = Status.newBuilder()
    status.code = this.code
    status.errorMessage = this.errorMessage ?: ""
    status.message = this.message
    status.timestamp = this.timestamp.toString()
    status.eventType = this.eventType
    return status.build()
}

// EXECUTION INPUT

fun ExecutionServiceInput.toJava(): org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput {
    val executionServiceInput = org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput()
    executionServiceInput.actionIdentifiers = this.actionIdentifiers.toJava()
    executionServiceInput.commonHeader = this.commonHeader.toJava()
    executionServiceInput.payload = this.payload.toJava()
    return executionServiceInput
}

// EXECUTION OUPUT

fun org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceOutput.toProto(): ExecutionServiceOutput {
    val executionServiceOuput = ExecutionServiceOutput.newBuilder()
    executionServiceOuput.actionIdentifiers = this.actionIdentifiers.toProto()
    executionServiceOuput.commonHeader = this.commonHeader.toProto()
    executionServiceOuput.status = this.status.toProto()
    val struct = Struct.newBuilder()
    JsonFormat.parser().merge(JacksonUtils.getJson(this.payload), struct)
    executionServiceOuput.payload = struct.build()
    return executionServiceOuput.build()
}