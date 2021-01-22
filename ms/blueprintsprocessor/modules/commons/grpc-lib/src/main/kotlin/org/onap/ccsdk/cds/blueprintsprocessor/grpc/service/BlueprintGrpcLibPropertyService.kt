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

package org.onap.ccsdk.cds.blueprintsprocessor.grpc.service

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.BasicAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GRPCLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.GrpcServerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TLSAuthGrpcServerProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.grpc.TokenAuthGrpcServerProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(GRPCLibConstants.SERVICE_BLUEPRINT_GRPC_LIB_PROPERTY)
open class BlueprintGrpcLibPropertyService(private var bluePrintPropertiesService: BlueprintPropertiesService) {

    fun blueprintGrpcServerService(jsonNode: JsonNode): BlueprintGrpcServerService {
        val grpcServerProperties = grpcServerProperties(jsonNode)
        return blueprintGrpcServerService(grpcServerProperties)
    }

    fun blueprintGrpcServerService(selector: String): BlueprintGrpcServerService {
        val prefix = "${GRPCLibConstants.PROPERTY_GRPC_SERVER_PREFIX}$selector"
        val grpcServerProperties = grpcServerProperties(prefix)
        return blueprintGrpcServerService(grpcServerProperties)
    }

    /** GRPC Server Lib Property Service */
    fun grpcServerProperties(jsonNode: JsonNode): GrpcServerProperties {
        return when (val type = jsonNode.get("type").textValue()) {
            GRPCLibConstants.TYPE_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode, TokenAuthGrpcServerProperties::class.java)!!
            }
            GRPCLibConstants.TYPE_TLS_AUTH -> {
                JacksonUtils.readValue(jsonNode, TLSAuthGrpcServerProperties::class.java)!!
            }
            else -> {
                throw BlueprintProcessorException("Grpc type($type) not supported")
            }
        }
    }

    fun grpcServerProperties(prefix: String): GrpcServerProperties {
        val type = bluePrintPropertiesService.propertyBeanType(
            "$prefix.type", String::class.java
        )
        return when (type) {
            GRPCLibConstants.TYPE_TOKEN_AUTH -> {
                tokenAuthGrpcServerProperties(prefix)
            }
            GRPCLibConstants.TYPE_TLS_AUTH -> {
                tlsAuthGrpcServerProperties(prefix)
            }
            else -> {
                throw BlueprintProcessorException("Grpc type($type) not supported")
            }
        }
    }

    private fun tokenAuthGrpcServerProperties(prefix: String): TokenAuthGrpcServerProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, TokenAuthGrpcServerProperties::class.java)
    }

    private fun tlsAuthGrpcServerProperties(prefix: String): TLSAuthGrpcServerProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, TLSAuthGrpcServerProperties::class.java)
    }

    private fun blueprintGrpcServerService(grpcServerProperties: GrpcServerProperties):
        BlueprintGrpcServerService {
            when (grpcServerProperties) {
                is TLSAuthGrpcServerProperties -> {
                    return TLSAuthGrpcServerService(grpcServerProperties)
                }
                else -> {
                    throw BlueprintProcessorException("couldn't get grpc client service for properties $grpcServerProperties")
                }
            }
        }

    /** GRPC Client Lib Property Service */

    fun blueprintGrpcClientService(jsonNode: JsonNode): BlueprintGrpcClientService {
        val restClientProperties = grpcClientProperties(jsonNode)
        return blueprintGrpcClientService(restClientProperties)
    }

    fun blueprintGrpcClientService(selector: String): BlueprintGrpcClientService {
        val prefix = "${GRPCLibConstants.PROPERTY_GRPC_CLIENT_PREFIX}$selector"
        val restClientProperties = grpcClientProperties(prefix)
        return blueprintGrpcClientService(restClientProperties)
    }

    fun grpcClientProperties(jsonNode: JsonNode): GrpcClientProperties {
        val type = jsonNode.get("type").returnNullIfMissing()?.textValue()
            ?: BlueprintProcessorException("missing type property")
        return when (type) {
            GRPCLibConstants.TYPE_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode, TokenAuthGrpcClientProperties::class.java)!!
            }
            GRPCLibConstants.TYPE_TLS_AUTH -> {
                JacksonUtils.readValue(jsonNode, TLSAuthGrpcClientProperties::class.java)!!
            }
            GRPCLibConstants.TYPE_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, BasicAuthGrpcClientProperties::class.java)!!
            }
            else -> {
                throw BlueprintProcessorException("Grpc type($type) not supported")
            }
        }
    }

    fun grpcClientProperties(prefix: String): GrpcClientProperties {
        val type = bluePrintPropertiesService.propertyBeanType(
            "$prefix.type", String::class.java
        )
        return when (type) {
            GRPCLibConstants.TYPE_TOKEN_AUTH -> {
                tokenAuthGrpcClientProperties(prefix)
            }
            GRPCLibConstants.TYPE_TLS_AUTH -> {
                tlsAuthGrpcClientProperties(prefix)
            }
            GRPCLibConstants.TYPE_BASIC_AUTH -> {
                basicAuthGrpcClientProperties(prefix)
            }
            else -> {
                throw BlueprintProcessorException("Grpc type($type) not supported")
            }
        }
    }

    fun blueprintGrpcClientService(grpcClientProperties: GrpcClientProperties):
        BlueprintGrpcClientService {
            return when (grpcClientProperties) {
                is TokenAuthGrpcClientProperties -> {
                    TokenAuthGrpcClientService(grpcClientProperties)
                }
                is TLSAuthGrpcClientProperties -> {
                    TLSAuthGrpcClientService(grpcClientProperties)
                }
                is BasicAuthGrpcClientProperties -> {
                    BasicAuthGrpcClientService(grpcClientProperties)
                }
                else -> {
                    throw BlueprintProcessorException("couldn't get grpc service for type(${grpcClientProperties.type})")
                }
            }
        }

    private fun tokenAuthGrpcClientProperties(prefix: String): TokenAuthGrpcClientProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, TokenAuthGrpcClientProperties::class.java)
    }

    private fun tlsAuthGrpcClientProperties(prefix: String): TLSAuthGrpcClientProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, TLSAuthGrpcClientProperties::class.java)
    }

    private fun basicAuthGrpcClientProperties(prefix: String): BasicAuthGrpcClientProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, BasicAuthGrpcClientProperties::class.java)
    }
}
