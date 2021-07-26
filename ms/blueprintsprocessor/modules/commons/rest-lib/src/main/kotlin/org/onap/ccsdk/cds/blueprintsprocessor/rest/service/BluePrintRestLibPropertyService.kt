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
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
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
open class BluePrintRestLibPropertyService(private var bluePrintPropertiesService: BluePrintPropertiesService) {

    private var preInterceptor: PreInterceptor? = null
    private var postInterceptor: PostInterceptor? = null

    fun setInterceptors(preInterceptor: PreInterceptor?, postInterceptor: PostInterceptor?) {
        this.preInterceptor = preInterceptor
        this.postInterceptor = postInterceptor
    }

    fun clearInterceptors() {
        this.preInterceptor = null
        this.postInterceptor = null
    }

    open fun blueprintWebClientService(jsonNode: JsonNode): BlueprintWebClientService {
        val service = preInterceptor?.getInstance(jsonNode)
            ?: blueprintWebClientService(restClientProperties(jsonNode))
        return postInterceptor?.getInstance(jsonNode, service) ?: service
    }

    open fun blueprintWebClientService(selector: String): BlueprintWebClientService {
        val service = preInterceptor?.getInstance(selector) ?: run {
            val prefix = "blueprintsprocessor.restclient.$selector"
            val restClientProperties = restClientProperties(prefix)
            blueprintWebClientService(restClientProperties)
        }
        return postInterceptor?.getInstance(selector, service) ?: service
    }

    fun restClientProperties(prefix: String): RestClientProperties {
        val type = bluePrintPropertiesService.propertyBeanType(
            "$prefix.type", String::class.java
        )
        return when (type) {
            RestLibConstants.TYPE_BASIC_AUTH -> {
                basicAuthRestClientProperties(prefix)
            }
            RestLibConstants.TYPE_TOKEN_AUTH -> {
                tokenRestClientProperties(prefix)
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

            RestLibConstants.TYPE_POLICY_MANAGER -> {
                policyManagerRestClientProperties(prefix)
            }
            else -> {
                throw BluePrintProcessorException(
                    "Rest adaptor($type) is" +
                        " not supported"
                )
            }
        }
    }

    fun restClientProperties(jsonNode: JsonNode): RestClientProperties {

        val type = jsonNode.get("type").textValue()
        return when (type) {
            RestLibConstants.TYPE_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode, TokenAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, BasicAuthRestClientProperties::class.java)!!
            }

            RestLibConstants.TYPE_POLICY_MANAGER -> {
                JacksonUtils.readValue(jsonNode, PolicyManagerRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_BASIC_AUTH -> {
                JacksonUtils.readValue(jsonNode, SSLBasicAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_TOKEN_AUTH -> {
                JacksonUtils.readValue(jsonNode, SSLTokenAuthRestClientProperties::class.java)!!
            }
            RestLibConstants.TYPE_SSL_NO_AUTH -> {
                JacksonUtils.readValue(jsonNode, SSLRestClientProperties::class.java)!!
            }
            else -> {
                throw BluePrintProcessorException(
                    "Rest adaptor($type) is not supported"
                )
            }
        }
    }

    private fun blueprintWebClientService(restClientProperties: RestClientProperties):
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
                else -> {
                    throw BluePrintProcessorException("couldn't get rest service for type:${restClientProperties.type}  uri: ${restClientProperties.url}")
                }
            }
        }

    private fun tokenRestClientProperties(prefix: String):
        TokenAuthRestClientProperties {
            return bluePrintPropertiesService.propertyBeanType(
                prefix, TokenAuthRestClientProperties::class.java
            )
        }

    private fun basicAuthRestClientProperties(prefix: String):
        BasicAuthRestClientProperties {
            return bluePrintPropertiesService.propertyBeanType(
                prefix, BasicAuthRestClientProperties::class.java
            )
        }

    private fun sslBasicAuthRestClientProperties(prefix: String):
        SSLRestClientProperties {

            val sslProps: SSLBasicAuthRestClientProperties =
                bluePrintPropertiesService.propertyBeanType(
                    prefix, SSLBasicAuthRestClientProperties::class.java
                )
            val basicProps: BasicAuthRestClientProperties =
                bluePrintPropertiesService.propertyBeanType(
                    prefix, BasicAuthRestClientProperties::class.java
                )
            sslProps.basicAuth = basicProps
            return sslProps
        }

    private fun sslTokenAuthRestClientProperties(prefix: String):
        SSLRestClientProperties {

            val sslProps: SSLTokenAuthRestClientProperties =
                bluePrintPropertiesService.propertyBeanType(
                    prefix,
                    SSLTokenAuthRestClientProperties::class.java
                )
            val basicProps: TokenAuthRestClientProperties =
                bluePrintPropertiesService.propertyBeanType(
                    prefix,
                    TokenAuthRestClientProperties::class.java
                )
            sslProps.tokenAuth = basicProps
            return sslProps
        }

    private fun sslNoAuthRestClientProperties(prefix: String):
        SSLRestClientProperties {
            return bluePrintPropertiesService.propertyBeanType(
                prefix, SSLRestClientProperties::class.java
            )
        }

    private fun policyManagerRestClientProperties(prefix: String):
        PolicyManagerRestClientProperties {
            return bluePrintPropertiesService.propertyBeanType(
                prefix, PolicyManagerRestClientProperties::class.java
            )
        }

    interface PreInterceptor {

        fun getInstance(jsonNode: JsonNode): BlueprintWebClientService?

        fun getInstance(selector: String): BlueprintWebClientService?
    }

    interface PostInterceptor {

        fun getInstance(jsonNode: JsonNode, service: BlueprintWebClientService): BlueprintWebClientService

        fun getInstance(selector: String, service: BlueprintWebClientService): BlueprintWebClientService
    }
}
