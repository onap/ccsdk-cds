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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
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

                print(resourceAssignment.dependencies)
                // Get all matching resources assignments to process
                val groupResourceAssignments =
                        resourceAssignments.filter {
                            it.dictionarySource == dSource
                        }.toMutableList()

                val inputKeyMapping: MutableMap<String, String> =
                        resourceAssignment.dependencies?.map { it to it }?.toMap()
                                as MutableMap<String, String>

                // Get the values from runtime store
                val resolvedKeyValues = resolveInputKeyMappingVariables(inputKeyMapping)

                // Generate the payload using already resolved value
                val generatedPayload = generatePayload(resolvedKeyValues, groupResourceAssignments)

                // Get the Rest Client service, selector will be included in application.properties
                val restClientService = BluePrintDependencyService.restClientService(
                        "naming-ms")

                // Get the Rest Response
                val response = restClientService.exchangeResource(HttpMethod.POST.name,
                        "/web/service/v1/genNetworkElementName/cds", generatedPayload)
                val responseStatusCode = response.status
                val responseBody = response.body
                if (responseStatusCode in 200..299 && !responseBody.isBlank()) {
                    populateResource(groupResourceAssignments, responseBody)
                } else {
                    val errMsg =
                            "Failed to dictionary name ($dName), dictionary source($($dName) " +
                                    "response_code: ($responseStatusCode)"
                    log.warn(errMsg)
                    throw BluePrintProcessorException(errMsg)
                }
                // Parse the error Body and assign the property value
            }
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BluePrintProcessorException(
                    "Failed in template key ($resourceAssignment) assignments with: ${e.message}",
                    e
            )
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException,
                                   executionRequest: ResourceAssignment) {
        raRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }

    private fun generatePayload(input: Map<String, Any>,
                                groupResourceAssignments: MutableList<ResourceAssignment>): String {

        var payload = """{ "elements" : ["""
        groupResourceAssignments.forEach {
            val metadata = resourceDictionaries[it.dictionaryName]?.property?.metadata
            val namingType = metadata?.get("naming-type")

            payload = payload.plus("""{ 
                    "${it.name}" : "$""")
            payload = payload.plus("""{${it.name}}",
                    "naming-type" : "$namingType",""")

            for ((k, v) in input) {
                payload = payload.plus(""""$k" : $v,""")
            }
            payload = payload.dropLast(1).plus("},")
        }
        payload = payload.dropLast(1).plus("] }")
        return payload

    }

    private fun populateResource(resourceAssignments: MutableList<ResourceAssignment>,
                                 restResponse: String) {
        /** Parse all the resource assignment fields and set the corresponding value */
        resourceAssignments.forEach { resourceAssignment ->
            // Set the List of Complex Values
            val metadata =
                    resourceDictionaries[resourceAssignment.dictionaryName]?.property?.metadata
            val responseKey = metadata?.get("naming-type")?.toLowerCase().plus("-name")

            val parsedResourceAssignmentValue = checkNotNull(
                    JacksonUtils.jsonNode(restResponse).path(responseKey).textValue()) {
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
