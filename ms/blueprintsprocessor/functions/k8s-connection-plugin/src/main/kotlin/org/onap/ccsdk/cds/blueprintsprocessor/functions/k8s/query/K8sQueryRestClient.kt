/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2022 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.query

import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sAbstractRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService

open class K8sQueryRestClient(
    k8sConfiguration: K8sConnectionPluginConfiguration
) : K8sAbstractRestClientService(k8sConfiguration, CLIENT_NAME) {

    companion object {
        public const val CLIENT_NAME = "k8s-plugin-query"

        fun getK8sQueryRestClient(
            k8sConfiguration: K8sConnectionPluginConfiguration
        ): BlueprintWebClientService {
            val rbQueryService = K8sQueryRestClient(
                k8sConfiguration
            )
            val service: BluePrintRestLibPropertyService =
                BluePrintDependencyService.instance(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)
            return service.interceptExternalBlueprintWebClientService(
                rbQueryService, CLIENT_NAME
            )
        }
    }

    override fun apiUrl(): String {
        return "$baseUrl/v1/query"
    }
}
