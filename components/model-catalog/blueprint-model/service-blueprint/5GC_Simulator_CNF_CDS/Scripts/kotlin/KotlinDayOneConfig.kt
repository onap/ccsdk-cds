/*
* Copyright © 2019 TechMahindra
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.entity.ContentType
import org.apache.http.message.BasicHeader
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BasicAuthRestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BasicAuthRestClientService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.K8sPluginDefinitionApi
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.K8sDefinitionRestClient
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.RestLoggerService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractScriptComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.contentFromResolvedArtifactNB
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ArchiveType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BluePrintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.storedContentFromResolvedArtifactNB
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.yaml.snakeyaml.Yaml
import java.util.ArrayList
import java.io.IOException

import java.util.Base64
import java.nio.charset.Charset
import java.nio.file.Files
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

open class DayOneConfig : AbstractScriptComponentFunction() {

    private val log = LoggerFactory.getLogger(DayOneConfig::class.java)!!

    override fun getName(): String {
        return "DayOneConfig"
    }

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {
        log.info("DAY-1 Script excution Started")

        val prefix = "baseconfig"

        val baseK8sApiUrl = getDynamicProperties("api-access").get("url").asText()
        val k8sApiUsername = getDynamicProperties("api-access").get("username").asText()
        val k8sApiPassword = getDynamicProperties("api-access").get("password").asText()

        log.info("Multi-cloud params $baseK8sApiUrl")

        val aaiApiUrl = getDynamicProperties("aai-access").get("url").asText()
        val aaiApiUsername = getDynamicProperties("aai-access").get("username").asText()
        val aaiApiPassword = getDynamicProperties("aai-access").get("password").asText()



        log.info("AAI params $aaiApiUrl")




        val resolution_key = getDynamicProperties("resolution-key").asText()

        val sdnc_payload:String = contentFromResolvedArtifactNB("config-deploy-sdnc")
        //log.info("SDNC payload $sdnc_payload")
        val sdnc_payloadObject = JacksonUtils.jsonNode(sdnc_payload) as ObjectNode


        val aai_payload:String = contentFromResolvedArtifactNB("config-deploy-aai")
        //log.info("AAI payload $aai_payload")
        val aai_payloadObject = JacksonUtils.jsonNode(aai_payload) as ObjectNode






        try {

            for (item in sdnc_payloadObject.get("vf-modules")){

                var instanceID:String =""
                val modelTopology = item.get("vf-module-data").get("vf-module-topology")



                val moduleParameters = modelTopology.get("vf-module-parameters").get("param")

                val label: String? = getParamValueByName(moduleParameters, "vf-module-label")
                val modelInfo = modelTopology.get("onap-model-information")
                val vfModuleInvariantID = modelInfo.get("model-invariant-uuid").asText()
                log.info("VF MOdule Inavriant ID $vfModuleInvariantID")
                val vfModuleCustID=modelInfo.get("model-customization-uuid").asText()
                val vfModuleUUID=modelInfo.get("model-uuid").asText()
                val idInfo = modelTopology.get("vf-module-topology-identifier")
                val vfModuleID = idInfo.get("vf-module-id").asText()
                for (aai_item in aai_payloadObject.get("vf-modules"))
                {
                    if (aai_item.get("vf-module-id").asText() == vfModuleID && aai_item.get("heat-stack-id") != null)
                    {
                        instanceID=aai_item.get("heat-stack-id").asText()
                        break
                    }
                }



                val k8sRbProfileName: String = "profile_" + vfModuleID

                val k8sConfigTemplateName: String = "template_" + vfModuleCustID

                val api = K8sConfigTemplateApi(k8sApiUsername, k8sApiPassword, baseK8sApiUrl, vfModuleInvariantID, vfModuleCustID, k8sConfigTemplateName)

                // Check if definition exists
                if (!api.hasDefinition()) {
                    throw BluePrintProcessorException("K8S Definition ($vfModuleInvariantID/$vfModuleCustID)  not found ")
                }
                val bluePrintPropertiesService: BluePrintPropertiesService =this.functionDependencyInstanceAsType("bluePrintPropertiesService")
                val k8sConfiguration = K8sConnectionPluginConfiguration(bluePrintPropertiesService)
                val rbDefinitionService = K8sDefinitionRestClient(k8sConfiguration,vfModuleInvariantID, vfModuleCustID)


                val def: BlueprintWebClientService.WebClientResponse<String> = rbDefinitionService.exchangeResource(HttpMethod.GET.name,"","")
                log.info(def.body.toString())
                val rbdef = JacksonUtils.jsonNode(def.body.toString()) as ObjectNode
                val chartName = rbdef.get("chart-name").asText()

                log.info("Config Template name: $k8sConfigTemplateName")



                var configTemplate = K8sConfigTemplate()
                configTemplate.templateName = k8sConfigTemplateName
                configTemplate.description = " "
                configTemplate.ChartName = chartName
                log.info("Chart name: ${configTemplate.ChartName}")



                if (!api.hasConfigTemplate(configTemplate)) {


                    val configTemplateFile: Path = prepareConfigTemplateJson(k8sConfigTemplateName, vfModuleID, label)

                    log.info("Config Template Upload Started")
                    api.createConfigTemplate(configTemplate)
                    api.uploadConfigTemplateContent(configTemplate, configTemplateFile)
                    log.info("Config Template Upload Completed")
                }
            }
            log.info("DAY-1 Script excution completed")


        }
        catch (e: Exception) {
            log.info("Caught exception during config template preparation!!")
            throw BluePrintProcessorException("${e.message}")
        }
    }
    private fun getParamValueByName(params: JsonNode, paramName: String): String? {
        for (param in params) {
            if (param.get("name").asText() == paramName && param.get("value").asText() != "null") {
                return param.get("value").asText()

            }
        }
        return null
    }

    fun prepareConfigTemplateJson(configTemplateName: String, vfModuleID: String, label: String?): Path {
        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val bluePrintBasePath: String = bluePrintContext.rootPath

        var profileFilePath: Path = Paths.get(bluePrintBasePath.plus(File.separator).plus("Templates").plus(File.separator).plus("k8s-profiles").plus(File.separator).plus(label +"-config-template.tar.gz"))
        log.info("Reading K8s Config Template file: $profileFilePath")

        val profileFile = profileFilePath.toFile()

        if (!profileFile.exists())
            throw BluePrintProcessorException("K8s Config template file $profileFilePath does not exists")

        return profileFilePath
    }


    fun getResolvedParameter(payload: ObjectNode, keyName: String): String {
        for (node in payload.get("resource-accumulator-resolved-data").elements()) {
            if (node.get("param-name").asText().equals(keyName)) {
                return node.get("param-value").asText()
            }
        }
        return ""
    }
    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        log.info("Recover function called!")
        log.info("Execution request : $executionRequest")
        log.error("Exception", runtimeException)
        addError(runtimeException.message!!)
    }



    inner class K8sConfigTemplateApi(
            val username: String,
            val password: String,
            val baseUrl: String,
            val definition: String,
            val definitionVersion: String,
            val configTemplateName: String
    ) {
        private val service: UploadConfigTemplateRestClientService // BasicAuthRestClientService

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

            this.service = UploadConfigTemplateRestClientService(basicAuthRestClientProperties)
        }

        fun hasDefinition(): Boolean {
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(HttpMethod.GET.name, "", "")
                print(result)
                if (result.status >= 200 && result.status < 300)
                    return true
                else
                    return false
            } catch (e: Exception) {
                log.info("Caught exception trying to get k8s config trmplate  definition")
                throw BluePrintProcessorException("${e.message}")
            }
        }

        fun hasConfigTemplate(profile: K8sConfigTemplate): Boolean {
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(HttpMethod.GET.name, "/config-template/${profile.templateName}", "")
                print(result)
                if (result.status >= 200 && result.status < 300) {
                    log.info("ConfigTemplate already exists")
                    return true
                } else
                    return false
            } catch (e: Exception) {
                log.info("Caught exception trying to get k8s config trmplate  definition")
                throw BluePrintProcessorException("${e.message}")
            }
        }

        fun createConfigTemplate(profile: K8sConfigTemplate) {
            val objectMapper = ObjectMapper()
            val profileJsonString: String = objectMapper.writeValueAsString(profile)
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.exchangeResource(
                        HttpMethod.POST.name,
                        "/config-template",
                        profileJsonString
                )

                if (result.status >= 200 && result.status < 300) {
                    log.info("Config template json info uploaded correctly")
                } else if (result.status < 200 || result.status >= 300) {
                    log.info("Config template already exists")
                }
            } catch (e: Exception) {
                log.info("Caught exception trying to create k8s config template ${profile.templateName}  - updated")
                //    throw BluePrintProcessorException("${e.message}")
            }
        }

        fun uploadConfigTemplateContent(profile: K8sConfigTemplate, filePath: Path) {
            try {
                val result: BlueprintWebClientService.WebClientResponse<String> = service.uploadBinaryFile(
                        "/config-template/${profile.templateName}/content",
                        filePath
                )
                if (result.status < 200 || result.status >= 300) {
                    throw Exception(result.body)
                }
            } catch (e: Exception) {
                log.info("Caught exception trying to upload k8s config template ${profile.templateName}")
                throw BluePrintProcessorException("${e.message}")
            }
        }
    }
}

class UploadConfigTemplateRestClientService(
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

class K8sConfigTemplate {
    @get:JsonProperty("template-name")
    var templateName: String? = null
    @get:JsonProperty("description")
    var description: String? = null
    @get:JsonProperty("ChartName")
    var ChartName: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}

class K8sResources {

    var GVK: GVK? = null
    lateinit var Name: String

}

class GVK {

    var Group: String? = null
    var Version: String? = null
    var Kind: String? = null

}

fun main(args: Array<String>) {

    val kotlin = DayOneConfig()



}
