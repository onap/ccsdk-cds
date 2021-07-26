/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
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

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.swagger.annotations.ApiModelProperty
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import java.util.Date
import java.util.UUID

/**
 * BlueprintProcessorData
 * @author Brinda Santh
 * DATE : 8/15/2018
 */

open class ExecutionServiceInput {

    @get:ApiModelProperty(required = false, hidden = true)
    var correlationUUID: String = UUID.randomUUID().toString()

    @get:ApiModelProperty(required = true, value = "Headers providing request context.")
    lateinit var commonHeader: CommonHeader

    @get:ApiModelProperty(required = true, value = "Provide information about the action to execute.")
    lateinit var actionIdentifiers: ActionIdentifiers

    @get:ApiModelProperty(
        required = true,
        value = "Contain the information to be passed as input to the action." +
            "The payload is constituted of two section: the workflow input which is the higher level block (xxx-request)" +
            " and the input for resource resolution located within the xxx-request block, contained within xxx-properties"
    )
    lateinit var payload: ObjectNode

    @get:ApiModelProperty(hidden = true)
    @get:JsonIgnore
    var stepData: StepData? = null
}

open class ExecutionServiceOutput {

    @get:ApiModelProperty(required = false, hidden = true)
    var correlationUUID: String? = null

    @get:ApiModelProperty(required = true, value = "Headers providing request context.")
    lateinit var commonHeader: CommonHeader

    @get:ApiModelProperty(required = true, value = "Provide information about the action to execute.")
    lateinit var actionIdentifiers: ActionIdentifiers

    @get:ApiModelProperty(required = true, value = "Status of the request.")
    lateinit var status: Status

    @get:ApiModelProperty(
        required = true,
        value = "Contain the information to be passed as input to the action." +
            "The payload is constituted of two section: the workflow input which is the higher level block (xxx-request)" +
            " and the input for resource resolution located within the xxx-request block, contained within xxx-properties"
    )
    lateinit var payload: ObjectNode

    @get:ApiModelProperty(hidden = true)
    @get:JsonIgnore
    var stepData: StepData? = null
}

const val ACTION_MODE_ASYNC = "async"
const val ACTION_MODE_SYNC = "sync"

open class ActionIdentifiers {

    @get:ApiModelProperty(required = false, value = "Name of the CBA.")
    lateinit var blueprintName: String

    @get:ApiModelProperty(required = false, value = "Version of the CBA.")
    lateinit var blueprintVersion: String

    @get:ApiModelProperty(required = true, value = "Name of the workflow to execute.")
    lateinit var actionName: String

    @get:ApiModelProperty(
        required = true,
        value = "Async processing is only supported for gRPC client.",
        allowableValues = "sync, async"
    )
    lateinit var mode: String
}

open class CommonHeader {

    @get:ApiModelProperty(required = true, value = "Date of the execution", example = "2012-04-23T18:25:43.511Z")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp: Date = Date()

    @get:ApiModelProperty(required = true, value = "Identify the system/person triggering the request.")
    lateinit var originatorId: String

    @get:ApiModelProperty(required = true, value = "Uniquely identify a request.")
    lateinit var requestId: String

    @get:ApiModelProperty(required = true, value = "Allow for fine-grain request identifier")
    lateinit var subRequestId: String

    @get:ApiModelProperty(required = false, hidden = true)
    var flags: Flags? = null
}

open class Flags {

    @get:ApiModelProperty(value = "Whether or not to force the action.")
    var isForce: Boolean = false

    @get:ApiModelProperty(value = "3600")
    var ttl: Int = 3600
}

open class Status {

    @get:ApiModelProperty(required = true, value = "HTTP status code equivalent.")
    var code: Int = 200

    @get:ApiModelProperty(required = true, value = "Type of the event being emitted by CDS.")
    var eventType: String = EventType.EVENT_COMPONENT_EXECUTED.name

    @get:ApiModelProperty(
        required = true,
        value = "Time when the execution ended.",
        example = "2012-04-23T18:25:43.511Z"
    )
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp: Date = Date()

    @get:ApiModelProperty(required = false, value = "Error message when system failed")
    var errorMessage: String? = null

    @get:ApiModelProperty(required = true, value = "Message providing request status")
    var message: String = BluePrintConstants.STATUS_SUCCESS
}

open class StepData {

    lateinit var name: String
    var properties: MutableMap<String, JsonNode> = mutableMapOf()
}
