/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2021 Orange.
 * Modifications Copyright © 2022 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s

import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

abstract class K8sAbstractRestClientService(
    private val k8sConfiguration: K8sConnectionPluginConfiguration,
    clientName: String
) : BasicAuthRestClientService(BasicAuthRestClientProperties()) {

    init {
        val service: BluePrintRestLibPropertyService = BluePrintDependencyService.instance(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)
        service.interceptExternalBlueprintWebClientService(this, clientName)
    }

    protected val baseUrl: String = k8sConfiguration.getProperties().url
    private var restClientProperties: BasicAuthRestClientProperties? = null

    override fun getRestClientProperties(): BasicAuthRestClientProperties {
        return getBasicAuthRestClientProperties()
    }

    private fun getBasicAuthRestClientProperties(): BasicAuthRestClientProperties {
        return if (restClientProperties != null)
            restClientProperties!!
        else {
            val basicAuthRestClientProperties = BasicAuthRestClientProperties()
            basicAuthRestClientProperties.username = k8sConfiguration.getProperties().username
            basicAuthRestClientProperties.password = k8sConfiguration.getProperties().password
            basicAuthRestClientProperties.url = apiUrl()
            basicAuthRestClientProperties.additionalHeaders = getHeaders()
            restClientProperties = basicAuthRestClientProperties
            return basicAuthRestClientProperties
        }
    }

    private fun getHeaders(): HashMap<String, String> {
        val mapOfHeaders = hashMapOf<String, String>()
        mapOfHeaders["Accept"] = "application/json"
        mapOfHeaders["Content-Type"] = "application/json"
        mapOfHeaders["cache-control"] = " no-cache"
        return mapOfHeaders
    }

    abstract fun apiUrl(): String
}
