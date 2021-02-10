/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2021 Orange.
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

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod.GET

class K8sPluginInstanceApi(
    private val k8sConfiguration: K8sConnectionPluginConfiguration
) {
    private val log = LoggerFactory.getLogger(K8sPluginInstanceApi::class.java)!!

    fun getAllInstances(): List<K8sRbInstance>? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: List<K8sRbInstance> = JacksonUtils.readValue(result.body)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Did not find any objects with tag"))
                null
            else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun getInstanceById(instanceId: String): K8sRbInstance? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val instance: JsonNode = JacksonUtils.jsonNode(result.body)
                val parsedObject: K8sRbInstance? = JacksonUtils.readValue(result.body, K8sRbInstance::class.java)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun getInstanceByRequestProperties(
        rbDefinitionName: String,
        rbDefinitionVersion: String,
        rbProfileName: String
    ): K8sRbInstance? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: List<K8sRbInstance> = JacksonUtils.readValue(result.body)
                var instance: K8sRbInstance? = null
                parsedObject.forEach {
                    if (it.request?.rbName == rbDefinitionName && it.request?.rbVersion == rbDefinitionVersion &&
                        it.request?.profileName == rbProfileName
                    )
                        instance = it
                }
                instance
            } else if (result.status == 500 && result.body.contains("Did not find any objects with tag"))
                null
            else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun getInstanceStatus(instanceId: String): K8sRbInstanceStatus? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/status",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceStatus? = JacksonUtils.readValue(
                    result.body, K8sRbInstanceStatus::class.java
                )
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }
}
