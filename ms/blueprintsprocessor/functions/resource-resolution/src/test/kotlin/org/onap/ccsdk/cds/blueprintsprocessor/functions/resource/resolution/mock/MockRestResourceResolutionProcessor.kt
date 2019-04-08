/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode
import org.apache.commons.collections.MapUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.RestResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor.ResourceAssignmentProcessor
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import java.util.HashMap

class MockRestResourceResolutionProcessor(private val blueprintRestLibPropertyService:
                                          BluePrintRestLibPropertyService): ResourceAssignmentProcessor() {

    private val logger = LoggerFactory.getLogger(MockRestResourceResolutionProcessor::class.java)

    override fun resolveInputKeyMappingVariables(inputKeyMapping: Map<String, String>): Map<String, Any> {
        val resolvedInputKeyMapping = HashMap<String, Any>()
        if (MapUtils.isNotEmpty(inputKeyMapping)) {
            resolvedInputKeyMapping["vnf_name"] = "vnf1"
            resolvedInputKeyMapping["vnf-id"] = "123456"
            resolvedInputKeyMapping["aai-port"] = "8080"
        }
        return resolvedInputKeyMapping
    }

    override fun getName(): String {
        return "${ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-rest"
    }

    override suspend fun processNB(executionRequest: ResourceAssignment) {
        //val inputKeyMapping = hashMapOf("vnf-id" to "vnf-id", "aai-port" to "port")
        try {
            // Check if It has Input
            val value = getFromInput(executionRequest)
            if (value == null || value is MissingNode) {
                val dName = executionRequest.dictionaryName
                val dSource = executionRequest.dictionarySource
                val resourceDefinition = resourceDictionaries[dName]

                val resourceSource = resourceDefinition!!.sources[dSource]

                val resourceSourceProperties = resourceSource!!.properties

                val sourceProperties =
                        JacksonUtils.getInstanceFromMap(resourceSourceProperties!!, RestResourceSource::class.java)

                val path = nullToEmpty(sourceProperties.path)
                val inputKeyMapping = sourceProperties.inputKeyMapping

                val resolvedInputKeyMapping = resolveInputKeyMappingVariables(inputKeyMapping!!).toMutableMap()

                // Resolving content Variables
                val payload = resolveFromInputKeyMapping(nullToEmpty(sourceProperties.payload), resolvedInputKeyMapping)
                val urlPath =
                        resolveFromInputKeyMapping(checkNotNull(sourceProperties.urlPath), resolvedInputKeyMapping)

                logger.info("$dSource dictionary information : ($urlPath), ($inputKeyMapping), (${sourceProperties.outputKeyMapping})")

                // Get the Rest Client Service
                val restClientService = blueprintWebClientService(executionRequest)

                val response = restClientService.exchangeResource("GET", urlPath, payload)
                if (response.isBlank()) {
                    logger.warn("Failed to get $dSource result for dictionary name ($dName) using urlPath ($urlPath)")
                } else {
                    populateResource(executionRequest, sourceProperties, response, path)
                }
            }
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(executionRequest, e.message)
            throw BluePrintProcessorException("Failed in template key ($executionRequest) assignments with: ${e.message}",
                    e)
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {
        raRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }

    private fun blueprintWebClientService(resourceAssignment: ResourceAssignment): BlueprintWebClientService {

        return blueprintRestLibPropertyService.blueprintWebClientService(resourceAssignment.dictionarySource!!)
    }

    @Throws(BluePrintProcessorException::class)
    private fun populateResource(resourceAssignment: ResourceAssignment, sourceProperties: RestResourceSource,
                                 restResponse: String, path: String) {
        val type = nullToEmpty(resourceAssignment.property?.type)
        lateinit var entrySchemaType: String

        val outputKeyMapping = sourceProperties.outputKeyMapping

        val responseNode = JacksonUtils.jsonNode(restResponse).at(path)

        when (type) {
            in BluePrintTypes.validPrimitiveTypes() -> {
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, responseNode)
            }
            in BluePrintTypes.validCollectionTypes() -> {
                // Array Types
                entrySchemaType = resourceAssignment.property!!.entrySchema!!.type
                val arrayNode = responseNode as ArrayNode

                if (entrySchemaType !in BluePrintTypes.validPrimitiveTypes()) {

                    val responseArrayNode = responseNode.toList()
                    for (responseSingleJsonNode in responseArrayNode) {

                        val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()

                        outputKeyMapping!!.map {
                            val responseKeyValue = responseSingleJsonNode.get(it.key)
                            val propertyTypeForDataType = ResourceAssignmentUtils
                                    .getPropertyType(raRuntimeService, entrySchemaType, it.key)

                            JacksonUtils.populateJsonNodeValues(it.value,
                                    responseKeyValue, propertyTypeForDataType, arrayChildNode)
                        }
                        arrayNode.add(arrayChildNode)
                    }
                }
                // Set the List of Complex Values
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, arrayNode)
            }
            else -> {
                // Complex Types
                entrySchemaType = resourceAssignment.property!!.type
                val objectNode = JacksonUtils.objectMapper.createObjectNode()
                outputKeyMapping!!.map {
                    val responseKeyValue = responseNode.get(it.key)
                    val propertyTypeForDataType = ResourceAssignmentUtils
                            .getPropertyType(raRuntimeService, entrySchemaType, it.key)
                    JacksonUtils.populateJsonNodeValues(it.value, responseKeyValue, propertyTypeForDataType, objectNode)
                }
                // Set the List of Complex Values
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, objectNode)
            }
        }
    }
}