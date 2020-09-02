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
import java.util.ArrayList
import java.nio.file.Paths
import java.io.File
import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.slf4j.LoggerFactory
import java.nio.file.Path
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

        const val OUTPUT_STATUSES = "statuses"
        const val OUTPUT_SKIPPED = "skipped"
        const val OUTPUT_UPLOADED = "uploaded"
        const val OUTPUT_ERROR = "error"
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
        var outputPrefixStatuses = mutableMapOf<String, String>()
        var inputParamsMap = mutableMapOf<String, JsonNode?>()

        inputParameterNames.forEach {
            inputParamsMap[it] = getOptionalOperationInput(it)?.returnNullIfMissing()
        }

        log.info("Getting the template prefixes")
        val prefixList: ArrayList<String> = getTemplatePrefixList(executionRequest)

        log.info("Iterating over prefixes in resource assignment map.")
        for (prefix in prefixList) {
            // Prefilling prefix sucess status
            outputPrefixStatuses.put(prefix, OUTPUT_SKIPPED)

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
            val profileName = prefixInputParamsMap[INPUT_K8S_PROFILE_NAME]?.returnNullIfMissing()?.textValue()
            val definitionName = prefixInputParamsMap[INPUT_K8S_DEFINITION_NAME]?.returnNullIfMissing()?.textValue()
            val definitionVersion = prefixInputParamsMap[INPUT_K8S_DEFINITION_VERSION]?.returnNullIfMissing()?.textValue()

            val k8sProfileUploadConfiguration = K8sProfileUploadConfiguration(bluePrintPropertiesService)

            // Creating API connector
            var api = K8sPluginApi(
                k8sProfileUploadConfiguration.getProperties().username,
                k8sProfileUploadConfiguration.getProperties().password,
                k8sProfileUploadConfiguration.getProperties().url,
                definitionName,
                definitionVersion
            )

            if ((profileName == null) || (definitionName == null) || (definitionVersion == null)) {
                log.warn("Prefix $prefix does not have required data for us to continue.")
            } else if (!api.hasDefinition()) {
                log.warn("K8s RB Definition ($definitionName/$definitionVersion) not found ")
            } else if (profileName == "") {
                log.warn("K8s rb profile name is empty! Either define profile name to use or choose default")
            } else if (api.hasProfile(profileName)) {
                log.info("Profile Already Existing - skipping upload")
            } else {
                log.info("Uploading K8s Profile..")
                outputPrefixStatuses.put(prefix, OUTPUT_ERROR)

                var profile = K8sProfile()
                profile.profileName = profileName
                profile.rbName = definitionName
                profile.rbVersion = definitionVersion
                profile.namespace = prefixInputParamsMap[INPUT_K8S_PROFILE_NAMESPACE]?.textValue()
                api.createProfile(profile)
                val profileFilePath: Path = prepareProfileFile(profileName)
                api.uploadProfileContent(profile, profileFilePath)

                log.info("K8s Profile Upload Completed")
                outputPrefixStatuses.put(prefix, OUTPUT_UPLOADED)
            }
        }
        bluePrintRuntimeService.setNodeTemplateAttributeValue(
            nodeTemplateName,
            OUTPUT_STATUSES,
            outputPrefixStatuses.asJsonNode()
        )
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
        return Paths.get(
            bluePrintBasePath.plus(File.separator)
                .plus("Templates")
                .plus(File.separator)
                .plus("k8s-profiles")
                .plus(File.separator)
                .plus("$k8sRbProfileName.tar.gz")
        )
    }
}
