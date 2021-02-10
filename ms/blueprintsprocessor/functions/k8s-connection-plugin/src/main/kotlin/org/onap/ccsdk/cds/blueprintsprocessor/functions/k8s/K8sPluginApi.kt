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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.K8sDefinitionRestClient
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.K8sUploadFileRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.profile.K8sProfile
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import java.nio.file.Path

class K8sPluginApi(
    private val k8sConfiguration: K8sConnectionPluginConfiguration
) {
    private val log = LoggerFactory.getLogger(K8sPluginApi::class.java)!!
    private val objectMapper = ObjectMapper()

    fun hasDefinition(definition: String, definitionVersion: String): Boolean {
        val rbDefinitionService = K8sDefinitionRestClient(
            k8sConfiguration,
            definition,
            definitionVersion
        )
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                GET.name,
                "",
                ""
            )
            log.debug(result.toString())
            return result.status in 200..299
        } catch (e: Exception) {
            log.error("Caught exception trying to get k8s rb definition")
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun hasProfile(definition: String, definitionVersion: String, profileName: String): Boolean {
        val rbDefinitionService = K8sDefinitionRestClient(
            k8sConfiguration,
            definition,
            definitionVersion
        )
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(
                GET.name,
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

    fun createProfile(definition: String, definitionVersion: String, profile: K8sProfile) {
        val rbDefinitionService = K8sDefinitionRestClient(
            k8sConfiguration,
            definition,
            definitionVersion
        )
        val profileJsonString: String = objectMapper.writeValueAsString(profile)
        try {
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
            throw BlueprintProcessorException("${e.message}")
        }
    }

    fun uploadProfileContent(definition: String, definitionVersion: String, profile: K8sProfile, filePath: Path) {
        val fileUploadService = K8sUploadFileRestClientService(
            k8sConfiguration,
            definition,
            definitionVersion
        )
        try {
            val result: BlueprintWebClientService.WebClientResponse<String> = fileUploadService.uploadBinaryFile(
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
}
