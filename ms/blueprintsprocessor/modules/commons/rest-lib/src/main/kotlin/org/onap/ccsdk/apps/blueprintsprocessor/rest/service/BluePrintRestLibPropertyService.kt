/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

import org.onap.ccsdk.apps.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.apps.blueprintsprocessor.rest.*
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.springframework.stereotype.Service

@Service
open class BluePrintRestLibPropertyService(private var bluePrintProperties: BluePrintProperties) {

    @Throws(BluePrintProcessorException::class)
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

    @Throws(BluePrintProcessorException::class)
    fun blueprintWebClientService(selector: String): BlueprintWebClientService {
        val prefix = "blueprintsprocessor.restclient.$selector"
        val beanProperties = restClientProperties(prefix)
        when (beanProperties) {
            is BasicAuthRestClientProperties -> {
                return BasicAuthRestClientService(beanProperties)
            }
            is SSLBasicAuthRestClientProperties -> {
                return SSLBasicAuthRestClientService(beanProperties)
            }
            is DME2RestClientProperties -> {
                return DME2ProxyRestClientService(beanProperties)
            }
            else -> {
                throw BluePrintProcessorException("couldn't get rest service for selector($selector)")
            }
        }

    }

    fun basicAuthRestClientProperties(prefix: String): BasicAuthRestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, BasicAuthRestClientProperties::class.java)
    }

    fun sslBasicAuthRestClientProperties(prefix: String): SSLBasicAuthRestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, SSLBasicAuthRestClientProperties::class.java)
    }

    fun dme2ProxyClientProperties(prefix: String): DME2RestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, DME2RestClientProperties::class.java)
    }

    fun policyManagerRestClientProperties(prefix: String): PolicyManagerRestClientProperties {
        return bluePrintProperties.propertyBeanType(prefix, PolicyManagerRestClientProperties::class.java)
    }
}


