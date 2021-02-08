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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.configuration.template.K8sTemplate
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.profile.upload.K8sProfile
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import java.nio.file.Path

class K8sPluginApi(
    val username: String,
    val password: String,
    val baseUrl: String,
    val definition: String?,
    val definitionVersion: String?
) {

    private val service: K8sUploadFileRestClientService // BasicAuthRestClientService
    private val log = LoggerFactory.getLogger(K8sPluginApi::class.java)!!
    val objectMapper = ObjectMapper()

    init {
        var mapOfHeaders = hashMapOf<String, String>()
        mapOfHeaders.put("Accept", "application/json")
        mapOfHeaders.put("Content-Type", "application/json")
        mapOfHeaders.put("cache-control", " no-cache")
        mapOfHeaders.put("Accept", "application/json")
        var basicAuthRestClientProperties: BasicAuthRestClientProperties = BasicAuthRestClientProperties()
        basicAuthRestClientProperties.username = username
        basicAuthRestClientProperties.password = password
        basicAuthRestClientProperties.url = "$baseUrl/v1/rb/definition/$definition/$definitionVersion"
        basicAuthRestClientProperties.additionalHeaders = mapOfHeaders

        this.service = K8sUploadFileRestClientService(basicAuthRestClientProperties)
    }

    fun hasDefinition(): Boolean {
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(HttpMethod.GET.name, "", "")
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb definition")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun hasProfile(profileName: String): Boolean {
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                HttpMethod.GET.name,
                "/profile/$profileName",
                ""
            )
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb profile")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun createProfile(profile: K8sProfile) {
        val profileJsonString: String = objectMapper.writeValueAsString(profile)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                HttpMethod.POST.name,
                "/profile",
                profileJsonString
            )
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to create k8s rb profile ${profile.profileName}")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun uploadProfileContent(profile: K8sProfile, filePath: Path) {
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.uploadBinaryFile(
                "/profile/${profile.profileName}/content",
                filePath
            )
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to upload k8s rb profile ${profile.profileName}")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun createTemplate(template: K8sTemplate): Boolean {
        val templateJsonString: String = objectMapper.writeValueAsString(template)
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service?.exchangeResource(
                    HttpMethod.POST.name,
                    "/config-template",
                    templateJsonString
            )!!
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception during create template")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun uploadTemplate(template: K8sTemplate, filePath: Path) {
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.uploadBinaryFile(
                    "/config-template/${template.templateName}/content",
                    filePath
            )
            if (result.status !in 200..299) {
                throw Exception(result.body)
            }
        } catch (e: Exception) {
            log.error("Caught exception trying to upload k8s rb template ${template.templateName}")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun getTemplate(templateName: String): K8sTemplate {
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                    HttpMethod.GET.name,
                    "/config-template/${templateName}",
                    ""
            )
            log.debug(result.toString())
            return objectMapper.readValue(result.body)
        } catch (e: Exception) {
            log.error("Caught exception during get template")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun deleteTemplate(templateName: String) {
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                    HttpMethod.DELETE.name,
                    "/config-template/${templateName}",
                    ""
            )
            log.debug(result.toString())
        } catch (e: Exception) {
            log.error("Caught exception during get template")
            throw BlueprintProcessorException("${e.message}")
        }
    }

}
