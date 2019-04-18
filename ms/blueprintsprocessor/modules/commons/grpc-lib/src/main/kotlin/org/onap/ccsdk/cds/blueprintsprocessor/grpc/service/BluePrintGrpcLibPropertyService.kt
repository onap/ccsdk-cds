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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.BasicAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GrpcClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(GRPCLibConstants.SERVICE_BLUEPRINT_GRPC_LIB_PROPERTY)
open class BluePrintGrpcLibPropertyService(private var bluePrintProperties: BluePrintProperties) {

    fun grpcClientProperties(jsonNode: JsonNode): GrpcClientProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            GRPCLibConstants.TYPE_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, BasicAuthGrpcClientProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("Grpc type($type) not supported")
            }
        }
    }

    fun grpcClientProperties(prefix: String): GrpcClientProperties {
        val type = bluePrintProperties.propertyBeanType(
                "$prefix.type", String::class.java)
        return when (type) {
            GRPCLibConstants.TYPE_BASIC_AUTH -> {
                basicAuthGrpcClientProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Grpc type($type) not supported")

            }
        }
    }

    private fun basicAuthGrpcClientProperties(prefix: String): BasicAuthGrpcClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, BasicAuthGrpcClientProperties::class.java)
    }
}