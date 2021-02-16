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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.instance.healthcheck.K8sRbInstanceHealthCheck
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT

class K8sPluginInstanceApi(
    private val k8sConfiguration: K8sConnectionPluginConfiguration
) {
    private val log = LoggerFactory.getLogger(K8sPluginInstanceApi::class.java)!!

    fun getInstanceList(): List<K8sRbInstance>? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val objectMapper = jacksonObjectMapper()
                val parsedObject: ArrayList<K8sRbInstance>? = objectMapper.readValue(result.body)
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
        val instances: List<K8sRbInstance>? = this.getInstanceList()
        instances?.forEach {
            if (it.request?.rbName == rbDefinitionName && it.request?.rbVersion == rbDefinitionVersion &&
                it.request?.profileName == rbProfileName
            )
                return it
        }
        return null
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

    fun getInstanceHealthCheckList(instanceId: String): List<K8sRbInstanceHealthCheck>? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/healthcheck",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val objectMapper = jacksonObjectMapper()
                val parsedObject: ArrayList<K8sRbInstanceHealthCheck>? = objectMapper.readValue(result.body)
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

    fun getInstanceHealthCheck(instanceId: String, healthCheckId: String): K8sRbInstanceHealthCheck? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/healthcheck/$healthCheckId",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceHealthCheck? = JacksonUtils.readValue(
                    result.body,
                    K8sRbInstanceHealthCheck::class.java
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

    fun startInstanceHealthCheck(instanceId: String): K8sRbInstanceHealthCheck? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/healthcheck",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sRbInstanceHealthCheck? = JacksonUtils.readValue(
                    result.body,
                    K8sRbInstanceHealthCheck::class.java
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

    fun createConfigurationValues(configValues: K8sConfigValueRequest, instanceId: String): K8sConfigValueResponse? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/config",
                JacksonUtils.getJson(configValues)
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun editConfigurationValues(configValues: K8sConfigValueRequest, instanceId: String, configName: String): K8sConfigValueResponse? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                PUT.name,
                "/config/$configName",
                JacksonUtils.getJson(configValues)
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun getConfigurationValues(instanceId: String, configName: String): K8sConfigValueResponse? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                GET.name,
                "/config/$configName",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun deleteConfigurationValues(instanceId: String, configName: String) {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                DELETE.name,
                "/config/$configName",
                ""
            )
            log.debug(result.toString())
            if (result.status !in 200..299) {
                throw BlueprintProcessorException(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun rollbackConfigurationValues(instanceId: String): K8sConfigValueResponse? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/rollback",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun createConfigurationValues(instanceId: String): K8sConfigValueResponse? {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                POST.name,
                "/tagit",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sConfigValueResponse? = JacksonUtils.readValue(
                    result.body, K8sConfigValueResponse::class.java
                )
                parsedObject
            } else
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun deleteInstanceHealthCheck(instanceId: String, healthCheckId: String) {
        val rbInstanceService = K8sRbInstanceRestClient(k8sConfiguration, instanceId)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbInstanceService.exchangeResource(
                DELETE.name,
                "/healthcheck/$healthCheckId",
                ""
            )
            log.debug(result.toString())
            if (result.status !in 200..299)
                throw BlueprintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb instance")
            throw BlueprintProcessorException("${e.message}")
        }
    }
}
