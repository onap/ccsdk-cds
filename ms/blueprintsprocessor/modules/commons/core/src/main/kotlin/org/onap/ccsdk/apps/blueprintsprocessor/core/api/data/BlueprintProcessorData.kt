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

package org.onap.ccsdk.apps.blueprintsprocessor.core.api.data

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.swagger.annotations.ApiModelProperty
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import java.util.*

/**
 * BlueprintProcessorData
 * @author Brinda Santh
 * DATE : 8/15/2018
 */

open class ResourceResolutionInput {
    @get:ApiModelProperty(required = true)
    lateinit var commonHeader: CommonHeader
    @get:ApiModelProperty(required = true)
    lateinit var actionIdentifiers: ActionIdentifiers
    @get:ApiModelProperty(required = true)
    lateinit var resourceAssignments: MutableList<ResourceAssignment>
    @get:ApiModelProperty(required = true)
    lateinit var payload: ObjectNode
}

open class ResourceResolutionOutput {
    @get:ApiModelProperty(required = true)
    lateinit var commonHeader: CommonHeader
    @get:ApiModelProperty(required = true)
    lateinit var actionIdentifiers: ActionIdentifiers
    @get:ApiModelProperty(required = true)
    lateinit var status: Status
    @get:ApiModelProperty(required = true)
    lateinit var resourceAssignments: MutableList<ResourceAssignment>
}

open class ExecutionServiceInput {
    @get:ApiModelProperty(required = true)
    lateinit var commonHeader: CommonHeader
    @get:ApiModelProperty(required = true)
    lateinit var actionIdentifiers: ActionIdentifiers
    @get:ApiModelProperty(required = true)
    lateinit var payload: ObjectNode
    var metadata: MutableMap<String, JsonNode> = hashMapOf()
}

open class ExecutionServiceOutput {
    @get:ApiModelProperty(required = true)
    lateinit var commonHeader: CommonHeader
    @get:ApiModelProperty(required = true)
    lateinit var actionIdentifiers: ActionIdentifiers
    @get:ApiModelProperty(required = true)
    var status: Status = Status()
    @get:ApiModelProperty(required = true)
    lateinit var payload: ObjectNode
    var metadata: MutableMap<String, JsonNode> = hashMapOf()
}

open class ActionIdentifiers {
    @get:ApiModelProperty(required = false)
    lateinit var blueprintName: String
    @get:ApiModelProperty(required = false)
    lateinit var blueprintVersion: String
    @get:ApiModelProperty(required = true)
    lateinit var actionName: String
    @get:ApiModelProperty(required = true, allowableValues = "sync, async")
    lateinit var mode: String
}

open class CommonHeader {
    @get:ApiModelProperty(required = true, example = "2012-04-23T18:25:43.511Z")
    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp: Date = Date()
    @get:ApiModelProperty(required = true)
    lateinit var originatorId: String
    @get:ApiModelProperty(required = true)
    lateinit var requestId: String
    @get:ApiModelProperty(required = true)
    lateinit var subRequestId: String
    @get:ApiModelProperty(required = false)
    var flags: Flags? = null
}

open class Flags {
    var isForce: Boolean = false
    @get:ApiModelProperty(value = "3600")
    var ttl: Int = 3600
}

open class Status {
    @get:ApiModelProperty(required = true)
    var code: Int = 200
    @get:ApiModelProperty(required = false)
    var errorMessage: String? = null
    @get:ApiModelProperty(required = true)
    var message: String = "success"
}





