/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.blueprintsprocessor.nats.service

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.NatsConnectionProperties
import org.onap.ccsdk.cds.blueprintsprocessor.nats.NatsLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.nats.TLSAuthNatsConnectionProperties
import org.onap.ccsdk.cds.blueprintsprocessor.nats.TokenAuthNatsConnectionProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(NatsLibConstants.SERVICE_BLUEPRINT_NATS_LIB_PROPERTY)
open class BluePrintNatsLibPropertyService(private var bluePrintPropertiesService: BluePrintPropertiesService) {

    fun bluePrintNatsService(jsonNode: JsonNode): BluePrintNatsService {
        val natsConnectionProperties = natsConnectionProperties(jsonNode)
        return bluePrintNatsService(natsConnectionProperties)
    }

    fun bluePrintNatsService(selector: String): BluePrintNatsService {
        val prefix = "${NatsLibConstants.PROPERTY_NATS_PREFIX}$selector"
        val natsConnectionProperties = natsConnectionProperties(prefix)
        return bluePrintNatsService(natsConnectionProperties)
    }

    /** NATS Lib Property Service */
    fun natsConnectionProperties(jsonNode: JsonNode): NatsConnectionProperties {
        return when (val type = jsonNode.get("type").textValue()) {
            NatsLibConstants.TYPE_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode, TokenAuthNatsConnectionProperties::class.java)!!
            }
            NatsLibConstants.TYPE_TLS_AUTH -> {
                JacksonUtils.readValue(jsonNode, TLSAuthNatsConnectionProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("NATS type($type) not supported")
            }
        }
    }

    fun natsConnectionProperties(prefix: String): NatsConnectionProperties {
        val type = bluePrintPropertiesService.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            NatsLibConstants.TYPE_TOKEN_AUTH -> {
                tokenAuthNatsConnectionProperties(prefix)
            }
            NatsLibConstants.TYPE_TLS_AUTH -> {
                tlsAuthNatsConnectionProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("NATS type($type) not supported")
            }
        }
    }

    private fun tokenAuthNatsConnectionProperties(prefix: String): TokenAuthNatsConnectionProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, TokenAuthNatsConnectionProperties::class.java)
    }

    private fun tlsAuthNatsConnectionProperties(prefix: String): TLSAuthNatsConnectionProperties {
        return bluePrintPropertiesService.propertyBeanType(prefix, TLSAuthNatsConnectionProperties::class.java)
    }

    fun bluePrintNatsService(natsConnectionProperties: NatsConnectionProperties):
        BluePrintNatsService {
            return when (natsConnectionProperties) {
                is TokenAuthNatsConnectionProperties -> {
                    TokenAuthNatsService(natsConnectionProperties)
                }
                is TLSAuthNatsConnectionProperties -> {
                    TLSAuthNatsService(natsConnectionProperties)
                }
                else -> {
                    throw BluePrintProcessorException("couldn't get NATS service for properties $natsConnectionProperties")
                }
            }
        }
}
