/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright (c) 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonReactorUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import java.util.*

class ResourceAssignmentUtils {
    companion object {

        private val logger = LoggerFactory.getLogger(ResourceAssignmentUtils::class.toString())

        suspend fun resourceDefinitions(blueprintBasePath: String): MutableMap<String, ResourceDefinition> {
            val dictionaryFile = normalizedFile(blueprintBasePath, BluePrintConstants.TOSCA_DEFINITIONS_DIR,
                    ResourceResolutionConstants.FILE_NAME_RESOURCE_DEFINITION_TYPES)
            checkFileExists(dictionaryFile) { "resource definition file(${dictionaryFile.absolutePath}) is missing" }
            return JacksonReactorUtils.getMapFromFile(dictionaryFile, ResourceDefinition::class.java)
        }

        @Throws(BluePrintProcessorException::class)
        fun setResourceDataValue(resourceAssignment: ResourceAssignment,
                                 raRuntimeService: ResourceAssignmentRuntimeService, value: Any?) {
            // TODO("See if Validation is needed in future with respect to conversion and Types")
            return setResourceDataValue(resourceAssignment, raRuntimeService, value.asJsonType())
        }

        @Throws(BluePrintProcessorException::class)
        fun setResourceDataValue(resourceAssignment: ResourceAssignment,
                                 raRuntimeService: ResourceAssignmentRuntimeService, value: JsonNode) {
            val resourceProp = checkNotNull(resourceAssignment.property) {
                "Failed in setting resource value for resource mapping $resourceAssignment"
            }
            checkNotEmpty(resourceAssignment.name) {
                "Failed in setting resource value for resource mapping $resourceAssignment"
            }

            if (resourceAssignment.dictionaryName.isNullOrEmpty()) {
                resourceAssignment.dictionaryName = resourceAssignment.name
                logger.warn("Missing dictionary key, setting with template key (${resourceAssignment.name}) " +
                        "as dictionary key (${resourceAssignment.dictionaryName})")
            }

            try {
                if (resourceProp.type.isNotEmpty()) {
                    val valueToPrint = getValueToLog(resourceAssignment.dictionarySource!!, value)
                    logger.info("Setting Resource Value ($valueToPrint) for Resource Name " +
                            "(${resourceAssignment.name}), definition(${resourceAssignment.dictionaryName}) " +
                            "of type (${resourceProp.type})")
                    setResourceValue(resourceAssignment, raRuntimeService, value)
                    resourceAssignment.updatedDate = Date()
                    resourceAssignment.updatedBy = BluePrintConstants.USER_SYSTEM
                    resourceAssignment.status = BluePrintConstants.STATUS_SUCCESS
                }
            } catch (e: Exception) {
                throw BluePrintProcessorException("Failed in setting value for template key " +
                        "(${resourceAssignment.name}) and dictionary key (${resourceAssignment.dictionaryName}) of " +
                        "type (${resourceProp.type}) with error message (${e.message})", e)
            }
        }

        private fun setResourceValue(resourceAssignment: ResourceAssignment,
                                     raRuntimeService: ResourceAssignmentRuntimeService, value: JsonNode) {
            // TODO("See if Validation is needed wrt to type before storing")
            raRuntimeService.putResolutionStore(resourceAssignment.name, value)
            raRuntimeService.putDictionaryStore(resourceAssignment.dictionaryName!!, value)
            resourceAssignment.property!!.value = value
        }

        fun setFailedResourceDataValue(resourceAssignment: ResourceAssignment, message: String?) {
            if (isNotEmpty(resourceAssignment.name)) {
                resourceAssignment.updatedDate = Date()
                resourceAssignment.updatedBy = BluePrintConstants.USER_SYSTEM
                resourceAssignment.status = BluePrintConstants.STATUS_FAILURE
                resourceAssignment.message = message
            }
        }

        @Throws(BluePrintProcessorException::class)
        fun setInputValueIfProvided(resourceAssignment: ResourceAssignment,
                                     raRuntimeService: ResourceAssignmentRuntimeService, value: JsonNode) {
            logger.info("${resourceAssignment.dictionarySource} source template key (${resourceAssignment.name}) found from input and value is ($value)")
            try {
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)
            }
            catch (e: Exception) {
                throw BluePrintProcessorException("Failed to set input value in resource name " +
                        "(${resourceAssignment.name}) and dictionary key (${resourceAssignment.dictionaryName}) of " +
                        "type (${resourceAssignment.property!!.type}) with error message (${e.message})", e)
            }

        }

        @Throws(BluePrintProcessorException::class)
        fun assertTemplateKeyValueNotNull(resourceAssignment: ResourceAssignment) {
            val resourceProp = checkNotNull(resourceAssignment.property) {
                "Failed to populate mandatory resource resource mapping $resourceAssignment"
            }
            if (resourceProp.required != null && resourceProp.required!!
                    && (resourceProp.value == null || resourceProp.value!!.returnNullIfMissing() == null)) {
                logger.error("failed to populate mandatory resource mapping ($resourceAssignment)")
                throw BluePrintProcessorException("failed to populate mandatory resource mapping ($resourceAssignment)")
            }
        }

        @Throws(BluePrintProcessorException::class)
        fun generateResourceDataForAssignments(assignments: List<ResourceAssignment>): String {
            val result: String
            try {
                val mapper = ObjectMapper()
                val root: ObjectNode = mapper.createObjectNode()

                var containsSecret = false
                assignments.forEach {
                    if (isNotEmpty(it.name) && it.property != null) {
                        val rName = it.name
                        val type = nullToEmpty(it.property?.type).toLowerCase()
                        val value = useDefaultValueIfNull(it, rName)
                        val valueToPrint: Any
                        if (it.dictionarySource in ResourceResolutionConstants.DATA_DICTIONARY_SECRET_SOURCE_TYPES) {
                            containsSecret = true
                            valueToPrint = "*************"
                        }
                        else{
                            valueToPrint = value
                        }

                        logger.trace("Generating Resource name ($rName), type ($type), value ($valueToPrint)")
                        root.set(rName, value)
                    }
                }
                result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                if (containsSecret) {
                    logger.info("Generated Resource Param Data (***********)")
                }
                else{
                    logger.info("Generated Resource Param Data ($result)")
                }
            } catch (e: Exception) {
                throw BluePrintProcessorException("Resource Assignment is failed with $e.message", e)
            }

            return result
        }

        @Throws(BluePrintProcessorException::class)
        fun generateResourceForAssignments(assignments: List<ResourceAssignment>): MutableMap<String, JsonNode> {
            val data: MutableMap<String, JsonNode> = hashMapOf()
            assignments.forEach {
                if (isNotEmpty(it.name) && it.property != null) {
                    val rName = it.name
                    val type = nullToEmpty(it.property?.type).toLowerCase()
                    val value = useDefaultValueIfNull(it, rName)
                    val valueToPrint = getValueToLog(it.dictionarySource!!, value)

                    logger.trace("Generating Resource name ($rName), type ($type), value ($valueToPrint)")
                    data[rName] = value
                }
            }
            return data
        }

        private fun useDefaultValueIfNull(resourceAssignment: ResourceAssignment, resourceAssignmentName: String): JsonNode {
            if (resourceAssignment.property?.value == null) {
                val defaultValue = "\${$resourceAssignmentName}"
                return TextNode(defaultValue)
            } else {
                return resourceAssignment.property!!.value!!
            }
        }

        fun transformToRARuntimeService(blueprintRuntimeService: BluePrintRuntimeService<*>,
                                        templateArtifactName: String): ResourceAssignmentRuntimeService {

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService(blueprintRuntimeService.id(),
                    blueprintRuntimeService.bluePrintContext())
            resourceAssignmentRuntimeService.createUniqueId(templateArtifactName)
            resourceAssignmentRuntimeService.setExecutionContext(blueprintRuntimeService.getExecutionContext() as MutableMap<String, JsonNode>)

            return resourceAssignmentRuntimeService
        }

        @Throws(BluePrintProcessorException::class)
        fun getPropertyType(raRuntimeService: ResourceAssignmentRuntimeService, dataTypeName: String,
                            propertyName: String): String {
            lateinit var type: String
            try {
                val dataTypeProps = checkNotNull(raRuntimeService.bluePrintContext().dataTypeByName(dataTypeName)?.properties)

                val propertyDefinition = checkNotNull(dataTypeProps[propertyName])
                type = checkNotEmpty(propertyDefinition.type) { "Couldn't get data type ($dataTypeName)" }
                logger.trace("Data type({})'s property ({}) is ({})", dataTypeName, propertyName, type)
            } catch (e: Exception) {
                logger.error("couldn't get data type($dataTypeName)'s property ($propertyName), error message $e")
                throw BluePrintProcessorException("${e.message}", e)
            }
            return type
        }

        @Throws(BluePrintProcessorException::class)
        fun parseResponseNode(responseNode: JsonNode, resourceAssignment: ResourceAssignment,
                              raRuntimeService: ResourceAssignmentRuntimeService, outputKeyMapping: MutableMap<String, String>): JsonNode {
            try {
                if ((resourceAssignment.property?.type).isNullOrEmpty()) {
                    throw BluePrintProcessorException("Couldn't get data dictionary type for dictionary name (${resourceAssignment.name})")
                }
                val type = resourceAssignment.property!!.type

                val valueToPrint = getValueToLog(resourceAssignment.dictionarySource!!, responseNode)

                logger.info("For template key (${resourceAssignment.name}) setting value as ($valueToPrint)")

                return when (type) {
                    in BluePrintTypes.validPrimitiveTypes() -> {
                        parseResponseNodeForPrimitiveTypes(responseNode, outputKeyMapping)
                    }
                    in BluePrintTypes.validCollectionTypes() -> {
                        // Array Types
                        parseResponseNodeForCollection(responseNode, resourceAssignment, raRuntimeService, outputKeyMapping)
                    }
                    else -> {
                        // Complex Types
                        parseResponseNodeForComplexType(responseNode, resourceAssignment, raRuntimeService, outputKeyMapping)
                    }
                }
            } catch (e: Exception) {
                logger.error("Fail to parse response data, error message $e")
                throw BluePrintProcessorException("${e.message}", e)
            }
        }

        private fun parseResponseNodeForPrimitiveTypes(responseNode: JsonNode,
                                                       outputKeyMapping: MutableMap<String, String>): JsonNode {
            var result: JsonNode? = responseNode
            if (responseNode.isComplexType()) {
                val key = outputKeyMapping.keys.firstOrNull()
                var returnNode: JsonNode? = null
                if (responseNode is ArrayNode) {
                    val arrayNode = responseNode.toList()
                    val firstElement = if (key.isNullOrEmpty()) {
                        arrayNode.first()
                    }
                    else{
                        arrayNode.firstOrNull { element ->
                            element.isComplexType() && element.has(outputKeyMapping[key])
                        }
                    }

                    if (firstElement.isNull() || (firstElement!!.isComplexType() && !firstElement!!.has(outputKeyMapping[key]))) {
                        if (key.isNullOrEmpty()) {
                            throw BluePrintProcessorException("Fail to find mapping in the responseNode.")
                        }
                        else {
                            throw BluePrintProcessorException("Fail to find response with output key mapping ($key) in result.")
                        }
                    }
                    returnNode = firstElement
                }
                result = if (returnNode != null && returnNode!!.isComplexType()) {
                    returnNode[outputKeyMapping[key]]
                }
                else {
                    responseNode
                }
            }
            else {
                if (outputKeyMapping.isNotEmpty()) {
                    throw BluePrintProcessorException("Fail to find key-value in response node to map output-key-mapping.")
                }
            }
            return result!!
        }

        private fun parseResponseNodeForCollection(responseNode: JsonNode, resourceAssignment: ResourceAssignment,
                                                   raRuntimeService: ResourceAssignmentRuntimeService,
                                                   outputKeyMapping: MutableMap<String, String>): JsonNode {
            val dName = resourceAssignment.dictionaryName
            val dSource = resourceAssignment.dictionarySource!!
            var resultNode: JsonNode
            if ((resourceAssignment.property?.entrySchema?.type).isNullOrEmpty()) {
                throw BluePrintProcessorException("Couldn't get data type for dictionary type " +
                        "(${resourceAssignment.property!!.type}) and dictionary name ($dName)")
            }
            val entrySchemaType = resourceAssignment.property!!.entrySchema!!.type

            var arrayNode = JacksonUtils.objectMapper.createArrayNode()
            if (outputKeyMapping.isNotEmpty()) {
                when (responseNode) {
                    is ArrayNode -> {
                        val responseArrayNode = responseNode.toList()
                        for (responseSingleJsonNode in responseArrayNode) {
                            val arrayChildNode = parseSingleElementOfArrayResponseNode(entrySchemaType,
                                    outputKeyMapping, raRuntimeService, responseSingleJsonNode, dSource)
                            arrayNode.add(arrayChildNode)
                        }
                        resultNode = arrayNode
                    }
                    is ObjectNode -> {
                        val responseArrayNode = responseNode.rootFieldsToMap()
                        resultNode = parseObjectResponseNode(entrySchemaType, outputKeyMapping, responseArrayNode, dSource)
                    }
                    else -> {
                        throw BluePrintProcessorException("Key-value response expected to match the responseNode.")
                    }
                }
            }
            else {
                when (responseNode) {
                    is ArrayNode -> {
                        responseNode.forEach { elementNode ->
                            arrayNode.add(elementNode)
                        }
                        resultNode = arrayNode
                    }
                    is ObjectNode -> {
                        val responseArrayNode = responseNode.rootFieldsToMap()
                        for ((key, responseSingleJsonNode) in responseArrayNode) {
                            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
                            logKeyValueResolvedResource(key, responseSingleJsonNode, entrySchemaType, dSource)
                            JacksonUtils.populateJsonNodeValues(key, responseSingleJsonNode, entrySchemaType, arrayChildNode)
                            arrayNode.add(arrayChildNode)
                        }
                        resultNode = arrayNode
                    }
                    else -> {
                        resultNode = responseNode
                    }
                }
            }

            return resultNode
        }

        private fun parseSingleElementOfArrayResponseNode(entrySchemaType: String, outputKeyMapping: MutableMap<String, String>,
                                                          raRuntimeService: ResourceAssignmentRuntimeService,
                                                          responseNode: JsonNode, dictionarySource: String): ObjectNode {
            val outputKeyMappingHasOnlyOneElement = checkIfOutputKeyMappingProvideOneElement(outputKeyMapping)
            when (entrySchemaType) {
                in BluePrintTypes.validPrimitiveTypes() -> {
                    if (outputKeyMappingHasOnlyOneElement) {
                        val outputKeyMap = outputKeyMapping.entries.first()
                        return parseSingleElementNodeWithOneOutputKeyMapping(responseNode, outputKeyMap.key, outputKeyMap.value, entrySchemaType, dictionarySource)
                    }
                    else {
                        throw BluePrintProcessorException("Expect one entry in output-key-mapping")
                    }
                }
                else -> {
                    return when {
                        checkOutputKeyMappingAllElementsInDataTypeProperties(entrySchemaType, outputKeyMapping, raRuntimeService) -> {
                            parseSingleElementNodeWithAllOutputKeyMapping(responseNode, outputKeyMapping, entrySchemaType, dictionarySource)
                        }
                        outputKeyMappingHasOnlyOneElement -> {
                            val outputKeyMap = outputKeyMapping.entries.first()
                            parseSingleElementNodeWithOneOutputKeyMapping(responseNode, outputKeyMap.key, outputKeyMap.value, entrySchemaType, dictionarySource)
                        }
                        else -> {
                            throw BluePrintProcessorException("Output-key-mapping do not map the Data Type $entrySchemaType")
                        }
                    }
                }
            }
        }

        private fun parseObjectResponseNode(entrySchemaType: String, outputKeyMapping: MutableMap<String, String>,
                                            responseArrayNode: MutableMap<String, JsonNode>, dictionarySource: String): ObjectNode {
            val outputKeyMappingHasOnlyOneElement = checkIfOutputKeyMappingProvideOneElement(outputKeyMapping)
            if (outputKeyMappingHasOnlyOneElement) {
                val outputKeyMap = outputKeyMapping.entries.first()
                return parseObjectResponseNodeWithOneOutputKeyMapping(responseArrayNode, outputKeyMap.key, outputKeyMap.value,
                        entrySchemaType, dictionarySource)
            }
            else {
                throw BluePrintProcessorException("Output-key-mapping do not map the Data Type $entrySchemaType")
            }
        }

        private fun parseSingleElementNodeWithOneOutputKeyMapping(responseSingleJsonNode: JsonNode, outputKeyMappingKey:
        String, outputKeyMappingValue: String, type: String, dictionarySource: String): ObjectNode {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()

            val responseKeyValue = if (responseSingleJsonNode.has(outputKeyMappingValue)) {
                responseSingleJsonNode.get(outputKeyMappingValue)
            }
            else {
                NullNode.getInstance()
            }

            logKeyValueResolvedResource(outputKeyMappingKey, responseKeyValue, type, dictionarySource)
            JacksonUtils.populateJsonNodeValues(outputKeyMappingKey, responseKeyValue, type, arrayChildNode)

            return arrayChildNode
        }

        private fun parseSingleElementNodeWithAllOutputKeyMapping(responseSingleJsonNode: JsonNode,
                                                                  outputKeyMapping: MutableMap<String, String>, type: String, dictionarySource: String): ObjectNode {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            outputKeyMapping.map {
                val responseKeyValue = if (responseSingleJsonNode.has(it.value)) {
                    responseSingleJsonNode.get(it.value)
                } else {
                    NullNode.getInstance()
                }

                logKeyValueResolvedResource(it.key, responseKeyValue, type, dictionarySource)
                JacksonUtils.populateJsonNodeValues(it.key, responseKeyValue, type, arrayChildNode)
            }
            return arrayChildNode
        }

        private fun parseObjectResponseNodeWithOneOutputKeyMapping(responseArrayNode: MutableMap<String, JsonNode>,
                                                                   outputKeyMappingKey: String, outputKeyMappingValue: String,
                                                                   type: String, dictionarySource: String): ObjectNode {
            val objectNode = JacksonUtils.objectMapper.createObjectNode()
            val responseSingleJsonNode = responseArrayNode.filterKeys { key ->
                key == outputKeyMappingValue }.entries.firstOrNull()

            if (responseSingleJsonNode == null) {
                logKeyValueResolvedResource(outputKeyMappingKey, NullNode.getInstance(), type, dictionarySource)
                JacksonUtils.populateJsonNodeValues(outputKeyMappingKey, NullNode.getInstance(), type, objectNode)
            }
            else
            {
                logKeyValueResolvedResource(outputKeyMappingKey, responseSingleJsonNode.value, type, dictionarySource)
                JacksonUtils.populateJsonNodeValues(outputKeyMappingKey, responseSingleJsonNode.value, type, objectNode)
            }
            return objectNode
        }

        private fun parseResponseNodeForComplexType(responseNode: JsonNode, resourceAssignment: ResourceAssignment,
                                                    raRuntimeService: ResourceAssignmentRuntimeService,
                                                    outputKeyMapping: MutableMap<String, String>): JsonNode {
            val entrySchemaType = resourceAssignment.property!!.type
            val dictionaryName = resourceAssignment.dictionaryName!!
            val dictionarySource = resourceAssignment.dictionarySource!!
            val outputKeyMappingHasOnlyOneElement = checkIfOutputKeyMappingProvideOneElement(outputKeyMapping)

            if (outputKeyMapping.isNotEmpty()) {
                return when {
                    checkOutputKeyMappingAllElementsInDataTypeProperties(entrySchemaType, outputKeyMapping, raRuntimeService) -> {
                        parseSingleElementNodeWithAllOutputKeyMapping(responseNode, outputKeyMapping, entrySchemaType, dictionarySource)
                    }
                    outputKeyMappingHasOnlyOneElement -> {
                        val outputKeyMap = outputKeyMapping.entries.first()
                        parseSingleElementNodeWithOneOutputKeyMapping(responseNode, outputKeyMap.key, outputKeyMap.value,
                                entrySchemaType, dictionarySource)
                    }
                    else -> {
                        throw BluePrintProcessorException("Output-key-mapping do not map the Data Type $entrySchemaType")
                    }
                }
            }
            else {
                val childNode = JacksonUtils.objectMapper.createObjectNode()
                JacksonUtils.populateJsonNodeValues(dictionaryName, responseNode, entrySchemaType, childNode)
                return childNode
            }
        }

        private fun checkOutputKeyMappingAllElementsInDataTypeProperties(dataTypeName: String, outputKeyMapping: MutableMap<String, String>,
                                                                         raRuntimeService: ResourceAssignmentRuntimeService): Boolean {
            val dataTypeProps = raRuntimeService.bluePrintContext().dataTypeByName(dataTypeName)?.properties
            val result = outputKeyMapping.filterKeys { !dataTypeProps!!.containsKey(it) }.keys.firstOrNull()
            return result == null
        }

        private fun logKeyValueResolvedResource(key: String, value: JsonNode, type: String, dictionarySource: String) {
            val valueToPrint = getValueToLog(dictionarySource, value)

            logger.info("For List Type Resource: key ($key), value ($valueToPrint), " +
                    "type  ({$type})")
        }

        private fun checkIfOutputKeyMappingProvideOneElement(outputKeyMapping: MutableMap<String, String>): Boolean {
            return (outputKeyMapping.size == 1)
        }

        private fun getValueToLog(dictionarySource: String, value: Any): Any {
            return if (dictionarySource in ResourceResolutionConstants.DATA_DICTIONARY_SECRET_SOURCE_TYPES) {
                "*************"
            }
            else{
                value
            }
        }

        fun checkIfInputWasProvided(resourceAssignment: ResourceAssignment, value: JsonNode) : Boolean{
            val defaultValueNode = resourceAssignment.property!!.defaultValue.asJsonType()
            return (value.returnNullIfMissing() != null && value != defaultValueNode)
        }
    }
}