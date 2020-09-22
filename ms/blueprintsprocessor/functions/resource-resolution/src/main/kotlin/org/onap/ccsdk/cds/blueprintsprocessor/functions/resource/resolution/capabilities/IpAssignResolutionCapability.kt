/*
 * Copyright © 2019 AT&T.
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
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.KeyIdentifier
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.http.HttpMethod

/**
 * @author saurav.paira
 */

open class IpAssignResolutionCapability : ResourceAssignmentProcessor() {

    val log = logger(IpAssignResolutionCapability::class)

    override fun getName(): String {
        return "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}ipassignment-capability"
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
                    ?: throw BluePrintProcessorException("couldn't get resource definition $dName source($dSource)")

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

                // Get the values from runtime store
                val resolvedKeyValues = resolveInputKeyMappingVariables(inputKeyMapping)
                log.info("\nResolved Input Key mappings: \n{}", resolvedKeyValues)

                resolvedKeyValues?.map { KeyIdentifier(it.key, it.value) }
                    ?.let { resourceAssignment.keyIdentifiers.addAll(it) }

                // Generate the payload using already resolved value
                val generatedPayload = generatePayload(resolvedKeyValues, groupResourceAssignments)
                log.info("\nIP Assign mS Request Payload: \n{}", generatedPayload.asJsonType().toPrettyString())

                resourceSourceProperties["resolved-payload"] = JacksonUtils.jsonNode(generatedPayload)

                // Get the Rest Client service, selector will be included in application.properties
                val restClientService = BluePrintDependencyService.restClientService(
                    "ipassign-ms"
                )

                // Get the Rest Response
                val response = restClientService.exchangeResource(
                    HttpMethod.POST.name,
                    "/web/service/v1/assign", generatedPayload
                )
                val responseStatusCode = response.status
                val responseBody = response.body
                log.info("\nIP Assign mS Response : \n{}", responseBody.asJsonType().toPrettyString())
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

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
        raRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }

    /** Generates aggregated request payload for Ip Assign mS. Parses the resourceassignments of
     * sourceCapability "ipassign-ms". It generates below sample payload
     * {
     "requests": [{
     "name": "fixed_ipv4_Address_01",
     "property": {
     "CloudRegionId": "abcd123",
     "IpServiceName": "MobilityPlan",
     }
     }, {
     "name": "fixed_ipv4_Address_02",
     "property": {
     "CloudRegionId": "abcd123",
     "IpServiceName": "MobilityPlan",
     }
     }
     ]
     } */
    private fun generatePayload(
        input: Map<String, Any>,
        groupResourceAssignments: MutableList<ResourceAssignment>
    ): String {
        data class IpRequest(val name: String = "", val property: Map<String, String> = mutableMapOf<String, String>())
        data class IpAssignRequest(val requests: MutableList<IpRequest> = mutableListOf())

        val ipAssignRequests = IpAssignRequest()
        groupResourceAssignments.forEach {
            val ipRequest = IpRequest(it.name, input.mapValues { it.value.toString().removeSurrounding("\"") })
            ipAssignRequests.requests.add(ipRequest)
        }
        return ipAssignRequests.asJsonType().toString()
    }

    private fun populateResource(
        resourceAssignments: MutableList<ResourceAssignment>,
        restResponse: String
    ) {
        /** Parse all the resource assignment fields and set the corresponding value */
        resourceAssignments.forEach { resourceAssignment ->
            // Set the List of Complex Values
            val parsedResourceAssignmentValue = checkNotNull(
                JacksonUtils.jsonNode(restResponse).path(resourceAssignment.name).textValue()
            ) {
                "Failed to find path ($resourceAssignment.name) in response ($restResponse)"
            }

            ResourceAssignmentUtils.setResourceDataValue(
                resourceAssignment,
                raRuntimeService,
                parsedResourceAssignmentValue
            )
        }
    }
}
