/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2020 Orange.
 * Modifications Copyright © 2020 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance

import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sAbstractRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService

open class K8sRbInstanceRestClient(
    k8sConfiguration: K8sConnectionPluginConfiguration,
    private val instanceId: String = ""
) : K8sAbstractRestClientService(k8sConfiguration, "k8s-plugin-instance") {

    companion object {
        public const val CLIENT_NAME = "k8s-plugin-instance"

        fun getK8sRbInstanceRestClient(
            k8sConfiguration: K8sConnectionPluginConfiguration,
            instanceId: String = ""
        ): BlueprintWebClientService {
            val rbInstanceService = K8sRbInstanceRestClient(
                k8sConfiguration,
                instanceId
            )
            return getInterceptedWebclientService(rbInstanceService, CLIENT_NAME)
        }
    }

    override fun apiUrl(): String {
        return if (instanceId != "")
            "$baseUrl/v1/instance/$instanceId"
        else
            "$baseUrl/v1/instance"
    }
}
