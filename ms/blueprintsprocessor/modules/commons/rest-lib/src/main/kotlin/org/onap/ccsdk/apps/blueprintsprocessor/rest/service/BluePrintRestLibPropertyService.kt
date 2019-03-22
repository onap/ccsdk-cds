/*
 * Copyright © 2017-2019 AT&T, Bell Canada
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.rest.service

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.DME2RestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.PolicyManagerRestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.apps.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.TokenAuthRestClientProperties
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)
open class BluePrintRestLibPropertyService(private var bluePrintProperties: BluePrintProperties) {

    fun blueprintWebClientService(jsonNode: JsonNode): BlueprintWebClientService {
        val restClientProperties = restClientProperties(jsonNode)
        return blueprintWebClientService(restClientProperties)
    }

    fun blueprintWebClientService(selector: String): BlueprintWebClientService {
        val prefix = "blueprintsprocessor.restclient.$selector"
        val restClientProperties = restClientProperties(prefix)
        return blueprintWebClientService(restClientProperties)
    }

    fun restClientProperties(prefix: String): RestClientProperties {
        val type = bluePrintProperties.propertyBeanType("$prefix.type", String::class.java)
        return when (type) {
            RestLibConstants.TYPE_BASIC_AUTH -> {
                basicAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_SSL_BASIC_AUTH -> {
                sslBasicAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_DME2_PROXY -> {
                dme2ProxyClientProperties(prefix)
            }
            RestLibConstants.TYPE_POLICY_MANAGER -> {
                policyManagerRestClientProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Rest adaptor($type) is not supported")
            }
        }
    }

    private fun restClientProperties(jsonNode: JsonNode): RestClientProperties {
        val type = jsonNode.get("type").textValue()
        return when (type) {
            RestLibConstants.TYPE_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode, TokenAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, BasicAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, SSLBasicAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_DME2_PROXY -> {
                JacksonUtils.readValue(jsonNode, DME2RestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_POLICY_MANAGER -> {
                JacksonUtils.readValue(jsonNode, PolicyManagerRestClientProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("Rest adaptor($type) is not supported")
            }
        }
    }

    private fun blueprintWebClientService(restClientProperties: RestClientProperties): BlueprintWebClientService {
        when (restClientProperties) {
            is TokenAuthRestClientProperties -> {
                return TokenAuthRestClientService(restClientProperties)
            }
            is BasicAuthRestClientProperties -> {
                return BasicAuthRestClientService(restClientProperties)
            }
            is SSLBasicAuthRestClientProperties -> {
                return SSLBasicAuthRestClientService(restClientProperties)
            }
            is DME2RestClientProperties -> {
                return DME2ProxyRestClientService(restClientProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get rest service for")
            }
        }
    }

    private fun basicAuthRestClientProperties(prefix: String): BasicAuthRestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, BasicAuthRestClientProperties::class.java)
    }

    private fun sslBasicAuthRestClientProperties(prefix: String): SSLBasicAuthRestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, SSLBasicAuthRestClientProperties::class.java)
    }

    private fun dme2ProxyClientProperties(prefix: String): DME2RestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, DME2RestClientProperties::class.java)
    }

    private fun policyManagerRestClientProperties(prefix: String): PolicyManagerRestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, PolicyManagerRestClientProperties::class.java)
    }
}


