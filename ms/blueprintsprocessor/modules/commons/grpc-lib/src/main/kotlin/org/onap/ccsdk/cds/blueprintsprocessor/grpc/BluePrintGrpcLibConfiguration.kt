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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BluePrintGrpcClientService
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.service.BluePrintGrpcLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
open class BluePrintGrpcLibConfiguration

/**
 * Exposed Dependency Service by this GRPC Lib Module
 */
fun BluePrintDependencyService.grpcLibPropertyService(): BluePrintGrpcLibPropertyService =
    instance(GRPCLibConstants.SERVICE_BLUEPRINT_GRPC_LIB_PROPERTY)

fun BluePrintDependencyService.grpcClientService(selector: String): BluePrintGrpcClientService {
    return grpcLibPropertyService().blueprintGrpcClientService(selector)
}

fun BluePrintDependencyService.grpcClientService(jsonNode: JsonNode): BluePrintGrpcClientService {
    return grpcLibPropertyService().blueprintGrpcClientService(jsonNode)
}

class GRPCLibConstants {
    companion object {

        const val SERVICE_BLUEPRINT_GRPC_LIB_PROPERTY = "blueprint-grpc-lib-property-service"
        const val PROPERTY_GRPC_CLIENT_PREFIX = "blueprintsprocessor.grpcclient."
        const val PROPERTY_GRPC_SERVER_PREFIX = "blueprintsprocessor.grpcserver."
        const val TYPE_TOKEN_AUTH = "token-auth"
        const val TYPE_BASIC_AUTH = "basic-auth"
        const val TYPE_TLS_AUTH = "tls-auth"
    }
}
