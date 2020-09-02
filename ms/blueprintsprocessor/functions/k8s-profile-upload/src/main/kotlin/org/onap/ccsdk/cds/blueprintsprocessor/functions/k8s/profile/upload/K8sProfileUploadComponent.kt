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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.profile.upload

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.http.HttpMethod
import com.fasterxml.jackson.annotation.JsonProperty
import java.util.ArrayList
import java.nio.file.Paths
import java.io.File
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.RestLoggerService
import org.apache.commons.io.IOUtils
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.message.BasicHeader
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.ClientProtocolException
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import java.nio.charset.Charset
import java.util.Base64
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing

@Component("component-k8s-profile-upload")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class K8sProfileUploadComponent(private var bluePrintPropertiesService: BluePrintPropertiesService) :

    AbstractComponentFunction() {

    companion object {
        const val INPUT_K8S_PROFILE_NAME = "k8s-rb-profile-name"
        const val INPUT_K8S_DEFINITION_NAME = "k8s-rb-definition-name"
        const val INPUT_K8S_DEFINITION_VERSION = "k8s-rb-definition-version"
        const val INPUT_K8S_PROFILE_NAMESPACE = "k8s-rb-profile-namespace"
        const val INPUT_K8S_PROFILE_SOURCE = "k8s-rb-profile-source"
        const val INPUT_RESOURCE_ASSIGNMENT_MAP = "resource-assignment-map"

        const val OUTPUT_RESOURCE_ASSIGNMENT_MAP = "resource-assignment-map"
        const val OUTPUT_STATUS = "status"
    }

    private val log = LoggerFactory.getLogger(K8sProfileUploadComponent::class.java)!!

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("Triggering K8s Profile Upload component logic.")

        val inputParameterNames = arrayOf(
            INPUT_K8S_PROFILE_NAME,
            INPUT_K8S_DEFINITION_NAME,
            INPUT_K8S_DEFINITION_VERSION,
            INPUT_K8S_PROFILE_NAMESPACE,
            INPUT_K8S_PROFILE_SOURCE
        )

        var inputParamsMap = mutableMapOf<String, JsonNode?>()

        inputParameterNames.forEach {
            inputParamsMap[it] = getOptionalOperationInput(it)?.returnNullIfMissing()
        }

        log.info("Getting the template prefixes")
        val prefixList: ArrayList<String> = getTemplatePrefixList(executionRequest)

        log.info("Iterating over prefixes in resource assignment map.")
        for (prefix in prefixList) {
            // Resource assignment map is organized by prefixes, in each iteraton we work only
            // on one section of resource assignment map
            val assignmentMapPrefix = operationInputs[INPUT_RESOURCE_ASSIGNMENT_MAP]?.get(prefix)

            // We are copying the map because for each prefix it might be completed with a different data
            var prefixInputParamsMap = inputParamsMap.toMutableMap()
            prefixInputParamsMap.forEach { (inputParamName, value) ->
                if (value == null) {
                    val mapValue = assignmentMapPrefix?.get(inputParamName)
                    log.debug("$inputParamName value was $value so we fetch $mapValue")
                    prefixInputParamsMap[inputParamName] = mapValue
                }
            }

            // For clarity we pull out the required fields
            val profileName = prefixInputParamsMap[INPUT_K8S_PROFILE_NAME]?.returnNullIfMissing()?.toString()
            val definitionName = prefixInputParamsMap[INPUT_K8S_DEFINITION_NAME]?.returnNullIfMissing()?.textValue()
            val definitionVersion = prefixInputParamsMap[INPUT_K8S_DEFINITION_VERSION]?.returnNullIfMissing()?.textValue()

            val k8sProfileUploadConfiguration = K8sProfileUploadConfiguration(bluePrintPropertiesService)

            // Creating API connector
            var api = K8sApi(
                k8sProfileUploadConfiguration.getProperties().username,
                k8sProfileUploadConfiguration.getProperties().password,
                k8sProfileUploadConfiguration.getProperties().url,
                definitionName,
                definitionVersion
            )

            if ((profileName == null) || (definitionName == null) || (definitionVersion == null)) {
                log.error("Prefix $prefix does not have required data for us to continue.")
            } else if (!api.hasDefinition()) {
                log.error("K8s RB Definition ($definitionName/$definitionVersion) not found ")
            } else if (profileName == "") {
                log.error("K8s rb profile name is empty! Either define profile name to use or choose default")
            } else if (api.hasProfile(profileName)) {
                log.info("Profile Already Existing - skipping upload")
            } else {
                log.info("Uploading K8s Profile..")

                var profile = K8sProfile()
                profile.profileName = profileName
                profile.rbName = definitionName
                profile.rbVersion = definitionVersion
                profile.namespace = prefixInputParamsMap[INPUT_K8S_PROFILE_NAMESPACE].toString()
                api.createProfile(profile)
                val profileFilePath: Path = prepareProfileFile(profileName)
                api.uploadProfileContent(profile, profileFilePath)

                log.info("K8s Profile Upload Completed")
            }
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }

    fun getTemplatePrefixList(executionRequest: ExecutionServiceInput): ArrayList<String> {
        val result = ArrayList<String>()
        for (prefix in executionRequest.payload.get("resource-assignment-request").get("template-prefix").elements())
            result.add(prefix.asText())
        return result
    }

    fun prepareProfileFile(k8sRbProfileName: String): Path {
        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val bluePrintBasePath: String = bluePrintContext.rootPath
        var profileFilePath: Path = Paths.get(bluePrintBasePath.plus(File.separator).plus("Templates").plus(File.separator).plus("k8s-profiles").plus(File.separator).plus("$k8sRbProfileName.tar.gz"))

        return profileFilePath
    }

    inner class K8sApi(
        val username: String,
        val password: String,
        val baseUrl: String,
        val definition: String?,
        val definitionVersion: String?
    ) {
        private val service: UploadFileRestClientService // BasicAuthRestClientService

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

            this.service = UploadFileRestClientService(basicAuthRestClientProperties)
        }

        fun hasDefinition(): Boolean {
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(HttpMethod.GET.name, "", "")
                log.info(result.toString())
                if (result.status >= 200 && result.status < 300)
                    return true
                else
                    return false
            } catch (e: Exception) {
                log.error("Caught exception trying to get k8s rb definition")
                throw BluePrintProcessorException("${e.message}")
            }
        }

        fun hasProfile(profileName: String): Boolean {
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                    HttpMethod.GET.name,
                    "/profile/$profileName",
                    ""
                )
                if (result.status >= 200 && result.status < 300)
                    return true
                else {
                    log.error(result.toString())
                    return false
                }
            } catch (e: Exception) {
                log.error("Caught exception trying to get k8s rb profile")
                throw BluePrintProcessorException("${e.message}")
            }
        }

        fun createProfile(profile: K8sProfile) {
            val objectMapper = ObjectMapper()
            val profileJsonString: String = objectMapper.writeValueAsString(profile)
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                    HttpMethod.POST.name,
                    "/profile",
                    profileJsonString
                )
                if (result.status < 200 || result.status >= 300) {
                    throw Exception(result.body)
                }
            } catch (e: Exception) {
                log.error("Caught exception trying to create k8s rb profile ${profile.profileName}")
                throw BluePrintProcessorException("${e.message}")
            }
        }

        fun uploadProfileContent(profile: K8sProfile, filePath: Path) {
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.uploadBinaryFile(
                    "/profile/${profile.profileName}/content",
                    filePath
                )
                if (result.status < 200 || result.status >= 300) {
                    throw Exception(result.body)
                }
            } catch (e: Exception) {
                log.error("Caught exception trying to upload k8s rb profile ${profile.profileName}")
                throw BluePrintProcessorException("${e.message}")
            }
        }
    }
}

class K8sProfile {
    @get:JsonProperty("rb-name")
    var rbName: String? = null
    @get:JsonProperty("rb-version")
    var rbVersion: String? = null
    @get:JsonProperty("profile-name")
    var profileName: String? = null
    @get:JsonProperty("namespace")
    var namespace: String? = "default"

    override fun toString(): String {
        return "$rbName:$rbVersion:$profileName"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class UploadFileRestClientService(
    private val restClientProperties:
        BasicAuthRestClientProperties
) : BlueprintWebClientService {

    override fun defaultHeaders(): Map<String, String> {

        val encodedCredentials = setBasicAuth(
            restClientProperties.username,
            restClientProperties.password
        )
        return mapOf(
            HttpHeaders.CONTENT_TYPE to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.ACCEPT to MediaType.APPLICATION_JSON_VALUE,
            HttpHeaders.AUTHORIZATION to "Basic $encodedCredentials"
        )
    }

    override fun host(uri: String): String {
        return restClientProperties.url + uri
    }

    override fun convertToBasicHeaders(headers: Map<String, String>):
        Array<BasicHeader> {
            val customHeaders: MutableMap<String, String> = headers.toMutableMap()
            // inject additionalHeaders
            customHeaders.putAll(verifyAdditionalHeaders(restClientProperties))

            if (!headers.containsKey(HttpHeaders.AUTHORIZATION)) {
                val encodedCredentials = setBasicAuth(
                    restClientProperties.username,
                    restClientProperties.password
                )
                customHeaders[HttpHeaders.AUTHORIZATION] =
                    "Basic $encodedCredentials"
            }
            return super.convertToBasicHeaders(customHeaders)
        }

    private fun setBasicAuth(username: String, password: String): String {
        val credentialsString = "$username:$password"
        return Base64.getEncoder().encodeToString(
            credentialsString.toByteArray(Charset.defaultCharset())
        )
    }

    @Throws(IOException::class, ClientProtocolException::class)
    private fun performHttpCall(httpUriRequest: HttpUriRequest): BlueprintWebClientService.WebClientResponse<String> {
        val httpResponse = httpClient().execute(httpUriRequest)
        val statusCode = httpResponse.statusLine.statusCode
        httpResponse.entity.content.use {
            val body = IOUtils.toString(it, Charset.defaultCharset())
            return BlueprintWebClientService.WebClientResponse(statusCode, body)
        }
    }

    fun uploadBinaryFile(path: String, filePath: Path): BlueprintWebClientService.WebClientResponse<String> {
        val convertedHeaders: Array<BasicHeader> = convertToBasicHeaders(defaultHeaders())
        val httpPost = HttpPost(host(path))
        val entity = EntityBuilder.create().setBinary(Files.readAllBytes(filePath)).build()
        httpPost.setEntity(entity)
        RestLoggerService.httpInvoking(convertedHeaders)
        httpPost.setHeaders(convertedHeaders)
        return performHttpCall(httpPost)
    }
}
