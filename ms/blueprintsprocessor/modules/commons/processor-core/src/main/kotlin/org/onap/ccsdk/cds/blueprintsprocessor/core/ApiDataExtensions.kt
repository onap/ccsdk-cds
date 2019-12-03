/*
 *  Copyright © 2019 IBM.
 *  Modifications Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.core

import com.fasterxml.jackson.databind.JsonNode
import com.google.protobuf.Struct
import com.google.protobuf.util.JsonFormat
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonString
import org.onap.ccsdk.cds.controllerblueprints.core.asType
import org.onap.ccsdk.cds.controllerblueprints.core.jsonAsJsonType
import kotlin.reflect.KClass

fun <T : Any> ExecutionServiceInput.payloadAsType(clazzType: KClass<T>): T {
    val actionName = this.actionIdentifiers.actionName
    val requestJsonNode = this.payload.get("$actionName-request")
    return requestJsonNode.asType(clazzType.java)
}

/** Convert Proto Struct to Json string */
fun Struct.asJson(): String {
    return JsonFormat.printer().print(this)
}

/** Convert Proto Struct to Json node */
fun Struct.asJsonType(): JsonNode {
    return this.asJson().jsonAsJsonType()
}

/** Convert Json node to Proto Struct */
fun JsonNode.asProtoStruct(): Struct {
    return this.asJsonString(false).asProtoStruct()
}

/** Convert Json string to Proto Struct */
fun String.asProtoStruct(): Struct {
    val struct = Struct.newBuilder()
    JsonFormat.parser().merge(this, struct)
    return struct.build()
}
