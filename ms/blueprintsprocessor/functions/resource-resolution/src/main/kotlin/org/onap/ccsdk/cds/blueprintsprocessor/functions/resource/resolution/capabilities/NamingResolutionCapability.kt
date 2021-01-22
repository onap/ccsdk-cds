/*
 * Copyright © 2019 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.capabilities

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.restClientService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.KeyIdentifier
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.http.HttpMethod

/**
 * @author brindasanth
 */

open class NamingResolutionCapability : ResourceAssignmentProcessor() {

    val log = logger(NamingResolutionCapability::class)

    override fun getName(): String {
        return "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}naming-capability"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        try {
            if (!setFromInput(resourceAssignment) && isTemplateKeyValueNull(resourceAssignment)) {
                val dName = resourceAssignment.dictionaryName!!
                val dSource = resourceAssignment.dictionarySource!!
                val resourceDefinition = resourceDefinition(dName)

                /** Check Resource Assignment has the source definitions, If not get from Resource Definitions **/
                val resourceSource = resourceAssignment.dictionarySourceDefinition
                    ?: resourceDefinition?.sources?.get(dSource)
                    ?: throw BlueprintProcessorException("couldn't get resource definition $dName source($dSource)")

                val resourceSourceProperties =
                    checkNotNull(resourceSource.properties) { "failed to get source properties for $dName " }

                // Get all matching resources assignments to process
                val groupResourceAssignments =
                    resourceAssignments.filter {
                        it.dictionarySource == dSource
                    }.toMutableList()

                // inputKeyMapping is dynamic based on dependencies
                val inputKeyMapping: MutableMap<String, String> =
                    resourceAssignment.dependencies?.map { it to it }?.toMap()
                        as MutableMap<String, String>
                log.info("\nResolving Input Key mappings: \n{}", inputKeyMapping)

                // Get the values from runtime store
                val resolvedKeyValues = resolveInputKeyMappingVariables(inputKeyMapping)
                log.info("\nResolved Input Key mappings: \n{}", resolvedKeyValues)

                resolvedKeyValues?.map { KeyIdentifier(it.key, it.value) }
                    ?.let { resourceAssignment.keyIdentifiers.addAll(it) }

                // Generate the payload using already resolved value
                val generatedPayload = generatePayload(resolvedKeyValues, groupResourceAssignments)
                log.info("\nNaming mS Request Payload: \n{}", generatedPayload.asJsonType().toPrettyString())

                resourceSourceProperties["resolved-payload"] = JacksonUtils.jsonNode(generatedPayload)

                // Get the Rest Client service, selector will be included in application.properties
                val restClientService = BlueprintDependencyService.restClientService(
                    "naming-ms"
                )

                // Get the Rest Response
                val response = restClientService.exchangeResource(
                    HttpMethod.POST.name,
                    "/web/service/v1/genNetworkElementName/cds", generatedPayload
                )

                val responseStatusCode = response.status
                val responseBody = response.body
                log.info("\nNaming mS Response : \n{}", responseBody.asJsonType().toPrettyString())
                if (responseStatusCode in 200..299 && !responseBody.isBlank()) {
                    populateResource(groupResourceAssignments, responseBody)
                } else {
                    val errMsg =
                        "Failed to dictionary name ($dName), dictionary source($($dName) " +
                            "response_code: ($responseStatusCode)"
                    log.warn(errMsg)
                    throw BlueprintProcessorException(errMsg)
                }
                // Parse the error Body and assign the property value
            }
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BlueprintProcessorException(
                "Failed in template key ($resourceAssignment) assignments with: ${e.message}",
                e
            )
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
        raRuntimeService.getBlueprintError().addError(runtimeException.message!!)
    }

    /** Generates aggregated request payload for Naming mS. Parses the resourceassignments of
     * sourceCapability "naming-ms". "naming-type" should be provides as property metadata for
     * each resourceassigment of sourceCapability "naming-ms". It generates below sample payload
     * {
     "elements": [{
     "vf-module-name": "${vf-module-name}",
     "naming-type": "VF-MODULE",
     "naming-code": "dbc",
     "vf-module-label": "adsf",
     "policy-instance-name": "SDNC_Policy.Config_Json.xml",
     "vnf-name": "vnf-123",
     "vf-module-type": "base"
     }, {
     "vnfc-name": "${vnfc-name}",
     "naming-type": "VNFC",
     "naming-code": "dbc",
     "vf-module-label": "adsf",
     "policy-instance-name": "SDNC_Policy.Config_Json.xml",
     "vnf-name": "vnf-123",
     "vf-module-type": "base"
     }
     ]
     } */
    private fun generatePayload(
        input: Map<String, Any>,
        groupResourceAssignments: MutableList<ResourceAssignment>
    ): String {
        data class NameAssignRequest(val elements: MutableList<Map<String, String>> = mutableListOf())

        val nameAssignRequests = NameAssignRequest()
        groupResourceAssignments.forEach {
            val metadata = resourceDictionaries[it.dictionaryName]?.property?.metadata
            val namingType = metadata?.get("naming-type")
            val moduleName = namingType.plus("-name").toLowerCase()
            val moduleValue = "\${".plus(moduleName.plus("}"))

            val request: MutableMap<String, String> = input.mapValues {
                it.value.toString().removeSurrounding("\"")
            } as MutableMap<String, String>
            if (namingType != null) {
                request["naming-type"] = namingType
            }
            request[moduleName] = moduleValue
            nameAssignRequests.elements.add(request)
        }
        return nameAssignRequests.asJsonType().toString()
    }

    private fun populateResource(
        resourceAssignments: MutableList<ResourceAssignment>,
        restResponse: String
    ) {
        /** Parse all the resource assignment fields and set the corresponding value */
        resourceAssignments.forEach { resourceAssignment ->
            // Set the List of Complex Values
            val metadata =
                resourceDictionaries[resourceAssignment.dictionaryName]?.property?.metadata

            /** Naming ms returns the keys with "${naming-type}-name" */
            val responseKey = metadata?.get("naming-type")?.toLowerCase().plus("-name")

            val parsedResourceAssignmentValue = checkNotNull(
                JacksonUtils.jsonNode(restResponse).path(responseKey).textValue()
            ) {
                "Failed to find path ($responseKey) in response ($restResponse)"
            }

            ResourceAssignmentUtils.setResourceDataValue(
                resourceAssignment,
                raRuntimeService,
                parsedResourceAssignmentValue
            )
        }
    }
}
