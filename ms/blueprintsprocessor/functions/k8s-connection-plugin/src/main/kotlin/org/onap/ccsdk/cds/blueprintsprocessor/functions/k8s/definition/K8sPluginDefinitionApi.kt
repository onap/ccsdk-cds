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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition

import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.profile.K8sProfile
import com.fasterxml.jackson.module.kotlin.readValue
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.K8sDefinitionRestClient.Companion.getK8sDefinitionRestClient
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.K8sUploadFileRestClientService.Companion.getK8sUploadFileRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.template.K8sTemplate
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpMethod.PUT
import java.nio.file.Path

class K8sPluginDefinitionApi(
    private val k8sConfiguration: K8sConnectionPluginConfiguration
) {
    private val log = LoggerFactory.getLogger(K8sPluginDefinitionApi::class.java)!!
    private val objectMapper = ObjectMapper()

    fun hasDefinition(definition: String, definitionVersion: String): Boolean {
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb definition")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun hasProfile(definition: String, definitionVersion: String, profileName: String): Boolean {
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                GET.name,
                "/profile/$profileName",
                ""
            )
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb profile")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getProfile(definition: String, definitionVersion: String, profileName: String): K8sProfile? {
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                GET.name,
                "/profile/$profileName",
                ""
            )
            log.debug(result.toString())
            return if (result.status in 200..299) {
                val parsedObject: K8sProfile? = JacksonUtils.readValue(result.body, K8sProfile::class.java)
                parsedObject
            } else if (result.status == 500 && result.body.contains("Error finding master table"))
                null
            else
                throw BluePrintProcessorException(result.body)
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb profile")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun createProfile(definition: String, definitionVersion: String, profile: K8sProfile) {
        val profileJsonString: String = objectMapper.writeValueAsString(profile)
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                POST.name,
                "/profile",
                profileJsonString
            )
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to create k8s rb profile ${profile.profileName}")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun updateProfile(profile: K8sProfile) {
        val profileJsonString: String = objectMapper.writeValueAsString(profile)
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                profile.rbName!!,
                profile.rbVersion!!
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                PUT.name,
                "/profile/${profile.profileName}",
                profileJsonString
            )
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to create k8s rb profile ${profile.profileName}")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun deleteProfile(definition: String, definitionVersion: String, profileName: String) {
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                DELETE.name,
                "/profile/$profileName",
                ""
            )
            log.debug(result.toString())
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception during get template")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun uploadProfileContent(definition: String, definitionVersion: String, profile: K8sProfile, filePath: Path) {
        try {
            val fileUploadService = getK8sUploadFileRestClientService(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = fileUploadService.uploadBinaryFile(
                "/profile/${profile.profileName}/content",
                filePath
            )
            log.debug(result.toString())
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
            log.debug("Profile uploaded properly")
        } catch (e: Exception) {
            log.error("Caught exception trying to upload k8s rb profile ${profile.profileName}")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun createTemplate(definition: String, definitionVersion: String, template: K8sTemplate) {
        val templateJsonString: String = objectMapper.writeValueAsString(template)
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                POST.name,
                "/config-template",
                templateJsonString
            )
            log.debug(result.toString())
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception during create template")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun uploadConfigTemplateContent(definition: String, definitionVersion: String, template: K8sTemplate, filePath: Path) {
        try {
            val fileUploadService = getK8sUploadFileRestClientService(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = fileUploadService.uploadBinaryFile(
                "/config-template/${template.templateName}/content",
                filePath
            )
            log.debug(result.toString())
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to upload k8s rb template ${template.templateName}")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun deleteTemplate(definition: String, definitionVersion: String, templateName: String) {
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                DELETE.name,
                "/config-template/$templateName",
                ""
            )
            log.debug(result.toString())
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception during get template")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun getTemplate(definition: String, definitionVersion: String, templateName: String): K8sTemplate {
        try {
            val rbDefinitionService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = getTemplateRequest(rbDefinitionService, templateName)
            log.debug(result.toString())
            return objectMapper.readValue(result.body)
        } catch (e: Exception) {
            log.error("Caught exception during get template")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    fun hasTemplate(definition: String, definitionVersion: String, templateName: String): Boolean {
        try {
            val interceptedService = getK8sDefinitionRestClient(
                k8sConfiguration,
                definition,
                definitionVersion
            )
            val result: BlueprintWebClientService.WebClientResponse<String> = getTemplateRequest(interceptedService, templateName)
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception during get template")
            throw BluePrintProcessorException("${e.message}")
        }
    }

    private fun getTemplateRequest(rbDefinitionService: BlueprintWebClientService, templateName: String): BlueprintWebClientService.WebClientResponse<String> {
        return rbDefinitionService.exchangeResource(
            GET.name,
            "/config-template/$templateName",
            ""
        )
    }
}
