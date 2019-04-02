/*
 * Copyright © 2017-2019 AT&T, Bell Canada
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2019 Huawei.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.DME2RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.PolicyManagerRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLBasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.SSLTokenAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.TokenAuthRestClientProperties
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.stereotype.Service

@Service(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)
open class BluePrintRestLibPropertyService(private var bluePrintProperties:
                                           BluePrintProperties) {

    fun blueprintWebClientService(jsonNode: JsonNode):
            BlueprintWebClientService {
        val restClientProperties = restClientProperties(jsonNode)
        return blueprintWebClientService(restClientProperties)
    }

    fun blueprintWebClientService(selector: String): BlueprintWebClientService {
        val prefix = "blueprintsprocessor.restclient.$selector"
        val restClientProperties = restClientProperties(prefix)
        return blueprintWebClientService(restClientProperties)
    }

    fun restClientProperties(prefix: String): RestClientProperties {
        val type = bluePrintProperties.propertyBeanType(
                "$prefix.type", String::class.java)
        return when (type) {
            RestLibConstants.TYPE_BASIC_AUTH -> {
                basicAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_SSL_BASIC_AUTH -> {
                sslBasicAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_SSL_TOKEN_AUTH -> {
                sslTokenAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_SSL_NO_AUTH -> {
                sslNoAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_DME2_PROXY -> {
                dme2ProxyClientProperties(prefix)
            }
            RestLibConstants.TYPE_POLICY_MANAGER -> {
                policyManagerRestClientProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException("Rest adaptor($type) is" +
                        " not supported")
            }
        }
    }

    fun restClientProperties(jsonNode: JsonNode): RestClientProperties {

        val type = jsonNode.get("type").textValue()
        return when (type) {
            RestLibConstants.TYPE_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode,
                        TokenAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode,
                        BasicAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_DME2_PROXY -> {
                JacksonUtils.readValue(jsonNode,
                        DME2RestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_POLICY_MANAGER -> {
                JacksonUtils.readValue(jsonNode,
                        PolicyManagerRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode,
                        SSLBasicAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode,
                        SSLTokenAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_NO_AUTH -> {
                JacksonUtils.readValue(
                        jsonNode, SSLRestClientProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException("Rest adaptor($type) is" +
                        " not supported")
            }
        }
    }
    
    private fun blueprintWebClientService(
            restClientProperties: RestClientProperties):
            BlueprintWebClientService {

        when (restClientProperties) {
            is SSLRestClientProperties -> {
                return SSLRestClientService(restClientProperties)
            }
            is TokenAuthRestClientProperties -> {
                return TokenAuthRestClientService(restClientProperties)
            }
            is BasicAuthRestClientProperties -> {
                return BasicAuthRestClientService(restClientProperties)
            }
            is DME2RestClientProperties -> {
                return DME2ProxyRestClientService(restClientProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get rest " +
                        "service for")
            }
        }
    }

    private fun basicAuthRestClientProperties(prefix: String):
            BasicAuthRestClientProperties {
        return bluePrintProperties.propertyBeanType(
                prefix, BasicAuthRestClientProperties::class.java)
    }

    private fun sslBasicAuthRestClientProperties(prefix: String):
            SSLRestClientProperties {

        val sslProps: SSLBasicAuthRestClientProperties =
                bluePrintProperties.propertyBeanType(
                        prefix, SSLBasicAuthRestClientProperties::class.java)
        val basicProps : BasicAuthRestClientProperties =
                bluePrintProperties.propertyBeanType(
                        prefix, BasicAuthRestClientProperties::class.java)
        sslProps.basicAuth = basicProps
        return sslProps
    }

    private fun sslTokenAuthRestClientProperties(prefix: String):
            SSLRestClientProperties {

        val sslProps: SSLTokenAuthRestClientProperties =
                bluePrintProperties.propertyBeanType(prefix,
                        SSLTokenAuthRestClientProperties::class.java)
        val basicProps : TokenAuthRestClientProperties =
                bluePrintProperties.propertyBeanType(prefix,
                        TokenAuthRestClientProperties::class.java)
        sslProps.tokenAuth = basicProps
        return sslProps
    }

    private fun sslNoAuthRestClientProperties(prefix: String):
            SSLRestClientProperties {
        return bluePrintProperties.propertyBeanType(
                prefix, SSLRestClientProperties::class.java)
    }

    private fun dme2ProxyClientProperties(prefix: String):
            DME2RestClientProperties {
        return bluePrintProperties.propertyBeanType(
                prefix, DME2RestClientProperties::class.java)
    }

    private fun policyManagerRestClientProperties(prefix: String):
            PolicyManagerRestClientProperties {
        return bluePrintProperties.propertyBeanType(
                prefix, PolicyManagerRestClientProperties::class.java)
    }
}


