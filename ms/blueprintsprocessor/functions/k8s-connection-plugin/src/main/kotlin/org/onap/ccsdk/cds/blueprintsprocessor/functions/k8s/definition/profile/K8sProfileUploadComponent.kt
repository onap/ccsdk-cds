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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.definition.profile

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.io.FileUtils
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sConnectionPluginConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.functions.k8s.K8sPluginApi
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.returnNullIfMissing
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.ArchiveType
import org.onap.ccsdk.cds.controllerblueprints.core.utils.BlueprintArchiveUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component("component-k8s-profile-upload")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class K8sProfileUploadComponent(
    private var bluePrintPropertiesService: BlueprintPropertiesService,
    private val resourceResolutionService: ResourceResolutionService
) :

    AbstractComponentFunction() {

    companion object {

        const val INPUT_K8S_PROFILE_NAME = "k8s-rb-profile-name"
        const val INPUT_K8S_DEFINITION_NAME = "k8s-rb-definition-name"
        const val INPUT_K8S_DEFINITION_VERSION = "k8s-rb-definition-version"
        const val INPUT_K8S_PROFILE_NAMESPACE = "k8s-rb-profile-namespace"
        const val INPUT_K8S_PROFILE_SOURCE = "k8s-rb-profile-source"
        const val INPUT_RESOURCE_ASSIGNMENT_MAP = "resource-assignment-map"
        const val INPUT_ARTIFACT_PREFIX_NAMES = "artifact-prefix-names"

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
            INPUT_K8S_PROFILE_SOURCE,
            INPUT_ARTIFACT_PREFIX_NAMES
        )
        var outputPrefixStatuses = mutableMapOf<String, String>()
        var inputParamsMap = mutableMapOf<String, JsonNode?>()

        inputParameterNames.forEach {
            inputParamsMap[it] = getOptionalOperationInput(it)?.returnNullIfMissing()
        }

        log.info("Getting the template prefixes")
        val prefixList: ArrayList<String> = getTemplatePrefixList(inputParamsMap[INPUT_ARTIFACT_PREFIX_NAMES])

        log.info("Iterating over prefixes in resource assignment map.")
        for (prefix in prefixList) {
            // Prefilling prefix sucess status
            outputPrefixStatuses.put(prefix, OUTPUT_SKIPPED)
            // Resource assignment map is organized by prefixes, in each iteraton we work only
            // on one section of resource assignment map
            val prefixNode: JsonNode = operationInputs[INPUT_RESOURCE_ASSIGNMENT_MAP]?.get(prefix) ?: continue
            val assignmentMapPrefix = JacksonUtils.jsonNode(prefixNode.toPrettyString()) as ObjectNode

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
            val profileName: String? = prefixInputParamsMap[INPUT_K8S_PROFILE_NAME]?.returnNullIfMissing()?.asText()
            val definitionName: String? = prefixInputParamsMap[INPUT_K8S_DEFINITION_NAME]?.returnNullIfMissing()?.asText()
            val definitionVersion: String? = prefixInputParamsMap[INPUT_K8S_DEFINITION_VERSION]?.returnNullIfMissing()?.asText()

            val k8sProfileUploadConfiguration = K8sConnectionPluginConfiguration(bluePrintPropertiesService)

            // Creating API connector
            var api = K8sPluginApi(k8sProfileUploadConfiguration)

            if ((profileName == null) || (definitionName == null) || (definitionVersion == null)) {
                log.warn("Prefix $prefix does not have required data for us to continue.")
            } else if (!api.hasDefinition(definitionName, definitionVersion)) {
                log.warn("K8s RB Definition ($definitionName/$definitionVersion) not found ")
            } else if (profileName == "") {
                log.warn("K8s rb profile name is empty! Either define profile name to use or choose default")
            } else if (api.hasProfile(definitionName, definitionVersion, profileName)) {
                log.info("Profile Already Existing - skipping upload")
            } else {
                log.info("Uploading K8s Profile..")
                outputPrefixStatuses.put(prefix, OUTPUT_ERROR)
                val profileNamespace: String? = prefixInputParamsMap[INPUT_K8S_PROFILE_NAMESPACE]?.returnNullIfMissing()?.asText()
                var profileSource: String? = prefixInputParamsMap[INPUT_K8S_PROFILE_SOURCE]?.returnNullIfMissing()?.asText()
                if (profileNamespace == null)
                    throw BlueprintProcessorException("Profile $profileName namespace is missing")
                if (profileSource == null) {
                    profileSource = profileName
                    log.info("Profile name used instead of profile source")
                }
                val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
                val artifact: ArtifactDefinition = bluePrintContext.nodeTemplateArtifact(nodeTemplateName, profileSource)
                if (artifact.type != BlueprintConstants.MODEL_TYPE_ARTIFACT_K8S_PROFILE)
                    throw BlueprintProcessorException(
                        "Unexpected profile artifact type for profile source " +
                            "$profileSource. Expecting: $artifact.type"
                    )
                var profile = K8sProfile()
                profile.profileName = profileName
                profile.rbName = definitionName
                profile.rbVersion = definitionVersion
                profile.namespace = profileNamespace
                val profileFilePath: Path = prepareProfileFile(profileName, profileSource, artifact.file)
                api.createProfile(definitionName, definitionVersion, profile)
                api.uploadProfileContent(definitionName, definitionVersion, profile, profileFilePath)

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
        bluePrintRuntimeService.getBlueprintError().addError(runtimeException.message!!)
    }

    private fun getTemplatePrefixList(node: JsonNode?): ArrayList<String> {
        var result = ArrayList<String>()
        when (node) {
            is ArrayNode -> {
                val arrayNode = node.toList()
                for (prefixNode in arrayNode)
                    result.add(prefixNode.asText())
            }
            is ObjectNode -> {
                result.add(node.asText())
            }
        }
        return result
    }

    private suspend fun prepareProfileFile(k8sRbProfileName: String, ks8ProfileSource: String, ks8ProfileLocation: String): Path {
        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val bluePrintBasePath: String = bluePrintContext.rootPath
        val profileSourceFileFolderPath: Path = Paths.get(
            bluePrintBasePath.plus(File.separator).plus(ks8ProfileLocation)
        )

        if (profileSourceFileFolderPath.toFile().exists() && !profileSourceFileFolderPath.toFile().isDirectory)
            return profileSourceFileFolderPath
        else if (profileSourceFileFolderPath.toFile().exists()) {
            log.info("Profile building started from source $ks8ProfileSource")
            val properties: MutableMap<String, Any> = mutableMapOf()
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_STORE_RESULT] = false
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_KEY] = ""
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_ID] = ""
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOURCE_TYPE] = ""
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_OCCURRENCE] = 1
            properties[ResourceResolutionConstants.RESOURCE_RESOLUTION_INPUT_RESOLUTION_SUMMARY] = false
            val resolutionResult: Pair<String, MutableList<ResourceAssignment>> = resourceResolutionService.resolveResources(
                bluePrintRuntimeService,
                nodeTemplateName,
                ks8ProfileSource,
                properties
            )
            val tempMainPath: File = createTempDir("k8s-profile-", "")
            val tempProfilePath: File = createTempDir("content-", "", tempMainPath)

            val resolvedJsonContent = resolutionResult.second
                .associateBy({ it.name }, { it.property?.value })
                .asJsonNode()

            try {
                val manifestFiles: ArrayList<File>? = readManifestFiles(
                    profileSourceFileFolderPath.toFile(),
                    tempProfilePath
                )
                if (manifestFiles != null) {
                    templateLocation(
                        profileSourceFileFolderPath.toFile(), resolvedJsonContent,
                        tempProfilePath, manifestFiles
                    )
                } else
                    throw BlueprintProcessorException("Manifest file is missing")
                // Preparation of the final profile content
                val finalProfileFilePath = Paths.get(
                    tempMainPath.toString().plus(File.separator).plus(
                        "$k8sRbProfileName.tar.gz"
                    )
                )
                if (!BlueprintArchiveUtils.compress(
                        tempProfilePath, finalProfileFilePath.toFile(),
                        ArchiveType.TarGz
                    )
                ) {
                    throw BlueprintProcessorException("Profile compression has failed")
                }
                FileUtils.deleteDirectory(tempProfilePath)

                return finalProfileFilePath
            } catch (t: Throwable) {
                FileUtils.deleteDirectory(tempMainPath)
                throw t
            }
        } else
            throw BlueprintProcessorException("Profile source $ks8ProfileLocation is missing in CBA folder")
    }

    private fun readManifestFiles(profileSource: File, destinationFolder: File): ArrayList<File>? {
        val directoryListing: Array<File>? = profileSource.listFiles()
        var result: ArrayList<File>? = null
        if (directoryListing != null) {
            for (child in directoryListing) {
                if (!child.isDirectory && child.name.toLowerCase() == "manifest.yaml") {
                    child.bufferedReader().use { inr ->
                        val manifestYaml = Yaml()
                        val manifestObject: Map<String, Any> = manifestYaml.load(inr)
                        val typeObject: MutableMap<String, Any>? = manifestObject["type"] as MutableMap<String, Any>?
                        if (typeObject != null) {
                            result = ArrayList<File>()
                            val valuesObject = typeObject["values"]
                            if (valuesObject != null) {
                                result!!.add(File(destinationFolder.toString().plus(File.separator).plus(valuesObject)))
                                result!!.add(File(destinationFolder.toString().plus(File.separator).plus(child.name)))
                            }
                            (typeObject["configresource"] as ArrayList<*>?)?.forEach { item ->
                                val fileInfo: Map<String, Any> = item as Map<String, Any>
                                val filePath = fileInfo["filepath"]
                                val chartPath = fileInfo["chartpath"]
                                if (filePath == null || chartPath == null)
                                    log.error("One configresource in manifest was skipped because of the wrong format")
                                else {
                                    result!!.add(File(destinationFolder.toString().plus(File.separator).plus(filePath)))
                                }
                            }
                        }
                    }
                    break
                }
            }
        }
        return result
    }

    private fun templateLocation(
        location: File,
        params: JsonNode,
        destinationFolder: File,
        manifestFiles: ArrayList<File>
    ) {
        val directoryListing: Array<File>? = location.listFiles()
        if (directoryListing != null) {
            for (child in directoryListing) {
                var newDestinationFolder = destinationFolder.toPath()
                if (child.isDirectory)
                    newDestinationFolder = Paths.get(destinationFolder.toString().plus(File.separator).plus(child.name))

                templateLocation(child, params, newDestinationFolder.toFile(), manifestFiles)
            }
        } else if (!location.isDirectory) {
            if (location.extension.toLowerCase() == "vtl") {
                templateFile(location, params, destinationFolder, manifestFiles)
            } else {
                val finalFilePath = Paths.get(
                    destinationFolder.path.plus(File.separator)
                        .plus(location.name)
                ).toFile()
                if (isFileInTheManifestFiles(finalFilePath, manifestFiles)) {
                    if (!destinationFolder.exists())
                        Files.createDirectories(destinationFolder.toPath())
                    FileUtils.copyFile(location, finalFilePath)
                }
            }
        }
    }

    private fun isFileInTheManifestFiles(file: File, manifestFiles: ArrayList<File>): Boolean {
        manifestFiles.forEach { fileFromManifest ->
            if (fileFromManifest.toString().toLowerCase() == file.toString().toLowerCase())
                return true
        }
        return false
    }

    private fun templateFile(
        templatedFile: File,
        params: JsonNode,
        destinationFolder: File,
        manifestFiles: ArrayList<File>
    ) {
        val finalFile = File(
            destinationFolder.path.plus(File.separator)
                .plus(templatedFile.nameWithoutExtension)
        )
        if (!isFileInTheManifestFiles(finalFile, manifestFiles))
            return
        val fileContent = templatedFile.bufferedReader().readText()
        val finalFileContent = BlueprintVelocityTemplateService.generateContent(
            fileContent,
            params, true
        )
        if (!destinationFolder.exists())
            Files.createDirectories(destinationFolder.toPath())
        finalFile.bufferedWriter().use { out -> out.write(finalFileContent) }
    }
}
