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
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonType
import org.onap.ccsdk.cds.controllerblueprints.core.checkFileExists
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.common.ApplicationConstants.LOG_REDACTED
import org.onap.ccsdk.cds.controllerblueprints.core.isComplexType
import org.onap.ccsdk.cds.controllerblueprints.core.isNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.isNullOrMissing
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.nullToEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.rootFieldsToMap
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonReactorUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.core.utils.PropertyDefinitionUtils.Companion.hasLogProtect
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.DictionaryMetadataEntry
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.KeyIdentifier
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResolutionSummary
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import java.util.Date

class ResourceAssignmentUtils {
    companion object {

        private val logger = LoggerFactory.getLogger(ResourceAssignmentUtils::class.toString())

        suspend fun resourceDefinitions(blueprintBasePath: String): MutableMap<String, ResourceDefinition> {
            val dictionaryFile = normalizedFile(
                blueprintBasePath, BlueprintConstants.TOSCA_DEFINITIONS_DIR,
                ResourceResolutionConstants.FILE_NAME_RESOURCE_DEFINITION_TYPES
            )
            checkFileExists(dictionaryFile) { "resource definition file(${dictionaryFile.absolutePath}) is missing" }
            return JacksonReactorUtils.getMapFromFile(dictionaryFile, ResourceDefinition::class.java)
        }

        @Throws(BlueprintProcessorException::class)
        fun setResourceDataValue(
            resourceAssignment: ResourceAssignment,
            raRuntimeService: ResourceAssignmentRuntimeService,
            value: Any?
        ) {
            // TODO("See if Validation is needed in future with respect to conversion and Types")
            return setResourceDataValue(resourceAssignment, raRuntimeService, value.asJsonType())
        }

        @Throws(BlueprintProcessorException::class)
        fun setResourceDataValue(
            resourceAssignment: ResourceAssignment,
            raRuntimeService: ResourceAssignmentRuntimeService,
            value: JsonNode
        ) {
            val resourceProp = checkNotNull(resourceAssignment.property) {
                "Failed in setting resource value for resource mapping $resourceAssignment"
            }
            checkNotEmpty(resourceAssignment.name) {
                "Failed in setting resource value for resource mapping $resourceAssignment"
            }

            if (resourceAssignment.dictionaryName.isNullOrEmpty()) {
                resourceAssignment.dictionaryName = resourceAssignment.name
                logger.warn(
                    "Missing dictionary key, setting with template key (${resourceAssignment.name}) " +
                        "as dictionary key (${resourceAssignment.dictionaryName})"
                )
            }

            try {
                if (resourceProp.type.isNotEmpty()) {
                    val metadata = resourceAssignment.property!!.metadata
                    val valueToPrint = getValueToLog(metadata, value)
                    logger.info(
                        "Setting Resource Value ($valueToPrint) for Resource Name " +
                            "(${resourceAssignment.name}), definition(${resourceAssignment.dictionaryName}) " +
                            "of type (${resourceProp.type})"
                    )
                    setResourceValue(resourceAssignment, raRuntimeService, value)
                    resourceAssignment.updatedDate = Date()
                    resourceAssignment.updatedBy = BlueprintConstants.USER_SYSTEM
                    resourceAssignment.status = BlueprintConstants.STATUS_SUCCESS
                }
            } catch (e: Exception) {
                throw BlueprintProcessorException(
                    "Failed in setting value for template key " +
                        "(${resourceAssignment.name}) and dictionary key (${resourceAssignment.dictionaryName}) of " +
                        "type (${resourceProp.type}) with error message (${e.message})",
                    e
                )
            }
        }

        private fun setResourceValue(
            resourceAssignment: ResourceAssignment,
            raRuntimeService: ResourceAssignmentRuntimeService,
            value: JsonNode
        ) {
            // TODO("See if Validation is needed wrt to type before storing")
            raRuntimeService.putResolutionStore(resourceAssignment.name, value)
            raRuntimeService.putDictionaryStore(resourceAssignment.dictionaryName!!, value)
            resourceAssignment.property!!.value = value

            val metadata = resourceAssignment.property?.metadata
            metadata?.get(ResourceResolutionConstants.METADATA_TRANSFORM_TEMPLATE)
                ?.let { if (it.contains("$")) it else null }
                ?.let { template ->
                    val resolutionStore = raRuntimeService.getResolutionStore()
                        .mapValues { e -> e.value.asText() } as MutableMap<String, Any>
                    val newValue: JsonNode
                    try {
                        newValue = BlueprintVelocityTemplateService
                            .generateContent(template, null, true, resolutionStore)
                            .also {
                                if (hasLogProtect(metadata))
                                    logger.info("Transformed value: $resourceAssignment.name")
                                else
                                    logger.info("Transformed value: $value -> $it")
                            }
                            .let { v -> v.asJsonType() }
                    } catch (e: Exception) {
                        throw BlueprintProcessorException(
                            "transform-template failed: $template", e
                        )
                    }
                    with(resourceAssignment) {
                        raRuntimeService.putResolutionStore(this.name, newValue)
                        raRuntimeService.putDictionaryStore(this.dictionaryName!!, newValue)
                        this.property!!.value = newValue
                    }
                }
        }

        fun setFailedResourceDataValue(resourceAssignment: ResourceAssignment, message: String?) {
            if (isNotEmpty(resourceAssignment.name)) {
                resourceAssignment.updatedDate = Date()
                resourceAssignment.updatedBy = BlueprintConstants.USER_SYSTEM
                resourceAssignment.status = BlueprintConstants.STATUS_FAILURE
                resourceAssignment.message = message
            }
        }

        @Throws(BlueprintProcessorException::class)
        fun assertTemplateKeyValueNotNull(resourceAssignment: ResourceAssignment) {
            val resourceProp = checkNotNull(resourceAssignment.property) {
                "Failed to populate mandatory resource resource mapping $resourceAssignment"
            }
            if (resourceProp.required != null && resourceProp.required!! && resourceProp.value.isNullOrMissing()) {
                logger.error("failed to populate mandatory resource mapping ($resourceAssignment)")
                throw BlueprintProcessorException("failed to populate mandatory resource mapping ($resourceAssignment)")
            }
        }

        @Throws(BlueprintProcessorException::class)
        fun generateResourceDataForAssignments(assignments: List<ResourceAssignment>): String {
            val result: String
            try {
                val mapper = ObjectMapper()
                mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                val root: ObjectNode = mapper.createObjectNode()

                var containsLogProtected = false
                assignments.forEach {
                    if (isNotEmpty(it.name) && it.property != null) {
                        val rName = it.name
                        val metadata = it.property!!.metadata
                        val type = nullToEmpty(it.property?.type).toLowerCase()
                        val value = useDefaultValueIfNull(it, rName)
                        val valueToPrint = getValueToLog(metadata, value)
                        containsLogProtected = hasLogProtect(metadata)
                        logger.trace("Generating Resource name ($rName), type ($type), value ($valueToPrint)")
                        root.set<JsonNode>(rName, value)
                    }
                }
                result = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(mapper.treeToValue(root, Object::class.java))

                if (!containsLogProtected) {
                    logger.info("Generated Resource Param Data ($result)")
                }
            } catch (e: Exception) {
                throw BlueprintProcessorException("Resource Assignment is failed with $e.message", e)
            }

            return result
        }

        @Throws(BlueprintProcessorException::class)
        fun generateResourceForAssignments(assignments: List<ResourceAssignment>): MutableMap<String, JsonNode> {
            val data: MutableMap<String, JsonNode> = hashMapOf()
            assignments.forEach {
                if (isNotEmpty(it.name) && it.property != null) {
                    val rName = it.name
                    val metadata = it.property!!.metadata
                    val type = nullToEmpty(it.property?.type).toLowerCase()
                    val value = useDefaultValueIfNull(it, rName)
                    val valueToPrint = getValueToLog(metadata, value)

                    logger.trace("Generating Resource name ($rName), type ($type), value ($valueToPrint)")
                    data[rName] = value
                }
            }
            return data
        }

        fun generateResolutionSummaryData(
            resourceAssignments: List<ResourceAssignment>,
            resourceDefinitions: Map<String, ResourceDefinition>
        ): String {
            val emptyTextNode = TextNode.valueOf("")
            val resolutionSummaryList = resourceAssignments.map {
                val definition = resourceDefinitions[it.name]
                val description = definition?.property?.description ?: ""
                val value = it.property?.value
                    ?.let { v -> if (v.isNullOrMissing()) emptyTextNode else v }
                    ?: emptyTextNode

                var payload: JsonNode = definition?.sources?.get(it.dictionarySource)
                    ?.properties?.get("resolved-payload")
                    ?.let { p -> if (p.isNullOrMissing()) emptyTextNode else p }
                    ?: emptyTextNode

                val metadata = definition?.property?.metadata
                    ?.map { e -> DictionaryMetadataEntry(e.key, e.value) }
                    ?.toMutableList() ?: mutableListOf()

                val keyIdentifiers: MutableList<KeyIdentifier> = it.keyIdentifiers.map { k ->
                    if (k.value.isNullOrMissing()) KeyIdentifier(k.name, emptyTextNode) else k
                }.toMutableList()

                ResolutionSummary(
                    it.name,
                    value,
                    it.property?.required ?: false,
                    it.property?.type ?: "",
                    keyIdentifiers,
                    description,
                    metadata,
                    it.dictionaryName ?: "",
                    it.dictionarySource ?: "",
                    payload,
                    it.status ?: "",
                    it.message ?: ""
                )
            }
            // Wrapper needed for integration with SDNC
            val data = mapOf("resolution-summary" to resolutionSummaryList)
            return JacksonUtils.getJson(data, includeNull = true)
        }

        private fun useDefaultValueIfNull(
            resourceAssignment: ResourceAssignment,
            resourceAssignmentName: String
        ): JsonNode {
            if (resourceAssignment.property?.value == null) {
                val defaultValue = "\${$resourceAssignmentName}"
                return TextNode(defaultValue)
            } else {
                return resourceAssignment.property!!.value!!
            }
        }

        fun transformToRARuntimeService(
            blueprintRuntimeService: BlueprintRuntimeService<*>,
            templateArtifactName: String
        ): ResourceAssignmentRuntimeService {

            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService(
                blueprintRuntimeService.id(),
                blueprintRuntimeService.bluePrintContext()
            )
            resourceAssignmentRuntimeService.createUniqueId(templateArtifactName)
            resourceAssignmentRuntimeService.setExecutionContext(blueprintRuntimeService.getExecutionContext() as MutableMap<String, JsonNode>)

            return resourceAssignmentRuntimeService
        }

        @Throws(BlueprintProcessorException::class)
        fun getPropertyType(
            raRuntimeService: ResourceAssignmentRuntimeService,
            dataTypeName: String,
            propertyName: String
        ): String {
            lateinit var type: String
            try {
                val dataTypeProps =
                    checkNotNull(raRuntimeService.bluePrintContext().dataTypeByName(dataTypeName)?.properties)

                val propertyDefinition = checkNotNull(dataTypeProps[propertyName])
                type = checkNotEmpty(propertyDefinition.type) { "Couldn't get data type ($dataTypeName)" }
                logger.trace("Data type({})'s property ({}) is ({})", dataTypeName, propertyName, type)
            } catch (e: Exception) {
                logger.error("couldn't get data type($dataTypeName)'s property ($propertyName), error message $e")
                throw BlueprintProcessorException("${e.message}", e)
            }
            return type
        }

        @Throws(BlueprintProcessorException::class)
        fun parseResponseNode(
            responseNode: JsonNode,
            resourceAssignment: ResourceAssignment,
            raRuntimeService: ResourceAssignmentRuntimeService,
            outputKeyMapping: MutableMap<String, String>
        ): JsonNode {
            val metadata = resourceAssignment.property!!.metadata
            try {
                if ((resourceAssignment.property?.type).isNullOrEmpty()) {
                    throw BlueprintProcessorException("Couldn't get data dictionary type for dictionary name (${resourceAssignment.name})")
                }
                val type = resourceAssignment.property!!.type
                val valueToPrint = getValueToLog(metadata, responseNode)

                logger.info("For template key (${resourceAssignment.name}) trying to get value from responseNode ($valueToPrint)")
                return when (type) {
                    in BlueprintTypes.validPrimitiveTypes() -> {
                        // Primitive Types
                        parseResponseNodeForPrimitiveTypes(responseNode, resourceAssignment, outputKeyMapping)
                    }
                    in BlueprintTypes.validCollectionTypes() -> {
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
                throw BlueprintProcessorException("${e.message}", e)
            }
        }

        private fun parseResponseNodeForPrimitiveTypes(
            responseNode: JsonNode,
            resourceAssignment: ResourceAssignment,
            outputKeyMapping: MutableMap<String, String>
        ): JsonNode {
            // Return responseNode if is not a Complex Type
            if (!responseNode.isComplexType()) {
                return responseNode
            }

            val outputKey = outputKeyMapping.keys.firstOrNull()
            var returnNode = if (responseNode is ArrayNode) {
                val arrayNode = responseNode.toList()
                if (outputKey.isNullOrEmpty()) {
                    arrayNode.first()
                } else {
                    arrayNode.firstOrNull { element ->
                        element.isComplexType() && element.has(outputKeyMapping[outputKey])
                    }
                }
            } else {
                responseNode
            }

            if (returnNode.isNullOrMissing() || returnNode!!.isComplexType() && !returnNode.has(outputKeyMapping[outputKey])) {
                throw BlueprintProcessorException("Fail to find output key mapping ($outputKey) in the responseNode.")
            }

            val returnValue = if (returnNode.isComplexType()) {
                returnNode[outputKeyMapping[outputKey]]
            } else {
                returnNode
            }

            outputKey?.let { KeyIdentifier(it, returnValue) }
                ?.let { resourceAssignment.keyIdentifiers.add(it) }
            return returnValue
        }

        private fun parseResponseNodeForCollection(
            responseNode: JsonNode,
            resourceAssignment: ResourceAssignment,
            raRuntimeService: ResourceAssignmentRuntimeService,
            outputKeyMapping: MutableMap<String, String>
        ): JsonNode {
            val dName = resourceAssignment.dictionaryName
            val metadata = resourceAssignment.property!!.metadata
            var resultNode: JsonNode
            if ((resourceAssignment.property?.entrySchema?.type).isNullOrEmpty()) {
                throw BlueprintProcessorException(
                    "Couldn't get data type for dictionary type " +
                        "(${resourceAssignment.property!!.type}) and dictionary name ($dName)"
                )
            }
            val entrySchemaType = resourceAssignment.property!!.entrySchema!!.type

            var arrayNode = JacksonUtils.objectMapper.createArrayNode()
            if (outputKeyMapping.isNotEmpty()) {
                when (responseNode) {
                    is ArrayNode -> {
                        val responseArrayNode = responseNode.toList()
                        for (responseSingleJsonNode in responseArrayNode) {
                            val arrayChildNode = parseSingleElementOfArrayResponseNode(
                                entrySchemaType, resourceAssignment,
                                outputKeyMapping, raRuntimeService, responseSingleJsonNode, metadata
                            )
                            arrayNode.add(arrayChildNode)
                        }
                        resultNode = arrayNode
                    }
                    is ObjectNode -> {
                        val responseArrayNode = responseNode.rootFieldsToMap()
                        resultNode =
                            parseObjectResponseNode(
                                resourceAssignment, entrySchemaType, outputKeyMapping,
                                responseArrayNode, metadata
                            )
                    }
                    else -> {
                        throw BlueprintProcessorException("Key-value response expected to match the responseNode.")
                    }
                }
            } else {
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
                            logKeyValueResolvedResource(metadata, key, responseSingleJsonNode, entrySchemaType)
                            JacksonUtils.populateJsonNodeValues(
                                key,
                                responseSingleJsonNode,
                                entrySchemaType,
                                arrayChildNode
                            )
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

        private fun parseSingleElementOfArrayResponseNode(
            entrySchemaType: String,
            resourceAssignment: ResourceAssignment,
            outputKeyMapping: MutableMap<String, String>,
            raRuntimeService: ResourceAssignmentRuntimeService,
            responseNode: JsonNode,
            metadata: MutableMap<String, String>?
        ): ObjectNode {
            val outputKeyMappingHasOnlyOneElement = checkIfOutputKeyMappingProvideOneElement(outputKeyMapping)
            when (entrySchemaType) {
                in BlueprintTypes.validPrimitiveTypes() -> {
                    if (outputKeyMappingHasOnlyOneElement) {
                        val outputKeyMap = outputKeyMapping.entries.first()
                        if (resourceAssignment.keyIdentifiers.none { it.name == outputKeyMap.key }) {
                            resourceAssignment.keyIdentifiers.add(
                                KeyIdentifier(outputKeyMap.key, JacksonUtils.objectMapper.createArrayNode())
                            )
                        }
                        return parseSingleElementNodeWithOneOutputKeyMapping(
                            resourceAssignment,
                            responseNode,
                            outputKeyMap.key,
                            outputKeyMap.value,
                            entrySchemaType,
                            metadata
                        )
                    } else {
                        throw BlueprintProcessorException("Expect one entry in output-key-mapping")
                    }
                }
                else -> {
                    return when {
                        checkOutputKeyMappingAllElementsInDataTypeProperties(
                            entrySchemaType,
                            outputKeyMapping,
                            raRuntimeService
                        ) -> {
                            parseSingleElementNodeWithAllOutputKeyMapping(
                                resourceAssignment,
                                responseNode,
                                outputKeyMapping,
                                entrySchemaType,
                                metadata
                            )
                        }
                        outputKeyMappingHasOnlyOneElement -> {
                            val outputKeyMap = outputKeyMapping.entries.first()
                            parseSingleElementNodeWithOneOutputKeyMapping(
                                resourceAssignment,
                                responseNode,
                                outputKeyMap.key,
                                outputKeyMap.value,
                                entrySchemaType,
                                metadata
                            )
                        }
                        else -> {
                            throw BlueprintProcessorException("Output-key-mapping do not map the Data Type $entrySchemaType")
                        }
                    }
                }
            }
        }

        private fun parseObjectResponseNode(
            resourceAssignment: ResourceAssignment,
            entrySchemaType: String,
            outputKeyMapping: MutableMap<String, String>,
            responseArrayNode: MutableMap<String, JsonNode>,
            metadata: MutableMap<String, String>?
        ): ObjectNode {
            val outputKeyMappingHasOnlyOneElement = checkIfOutputKeyMappingProvideOneElement(outputKeyMapping)
            if (outputKeyMappingHasOnlyOneElement) {
                val outputKeyMap = outputKeyMapping.entries.first()
                val returnValue = parseObjectResponseNodeWithOneOutputKeyMapping(
                    responseArrayNode, outputKeyMap.key, outputKeyMap.value,
                    entrySchemaType, metadata
                )
                resourceAssignment.keyIdentifiers.add(KeyIdentifier(outputKeyMap.key, returnValue))
                return returnValue
            } else {
                throw BlueprintProcessorException("Output-key-mapping do not map the Data Type $entrySchemaType")
            }
        }

        private fun parseSingleElementNodeWithOneOutputKeyMapping(
            resourceAssignment: ResourceAssignment,
            responseSingleJsonNode: JsonNode,
            outputKeyMappingKey: String,
            outputKeyMappingValue: String,
            type: String,
            metadata: MutableMap<String, String>?
        ): ObjectNode {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()

            val responseKeyValue = if (responseSingleJsonNode.has(outputKeyMappingValue)) {
                responseSingleJsonNode.get(outputKeyMappingValue)
            } else {
                NullNode.getInstance()
            }

            logKeyValueResolvedResource(metadata, outputKeyMappingKey, responseKeyValue, type)
            JacksonUtils.populateJsonNodeValues(outputKeyMappingKey, responseKeyValue, type, arrayChildNode)
            resourceAssignment.keyIdentifiers.find { it.name == outputKeyMappingKey && it.value.isArray }
                .let {
                    if (it != null)
                        (it.value as ArrayNode).add(responseKeyValue)
                    else
                        resourceAssignment.keyIdentifiers.add(
                            KeyIdentifier(outputKeyMappingKey, responseKeyValue)
                        )
                }
            return arrayChildNode
        }

        private fun parseSingleElementNodeWithAllOutputKeyMapping(
            resourceAssignment: ResourceAssignment,
            responseSingleJsonNode: JsonNode,
            outputKeyMapping: MutableMap<String, String>,
            type: String,
            metadata: MutableMap<String, String>?
        ): ObjectNode {
            val arrayChildNode = JacksonUtils.objectMapper.createObjectNode()
            outputKeyMapping.map {
                val responseKeyValue = if (responseSingleJsonNode.has(it.value)) {
                    responseSingleJsonNode.get(it.value)
                } else {
                    NullNode.getInstance()
                }

                logKeyValueResolvedResource(metadata, it.key, responseKeyValue, type)
                JacksonUtils.populateJsonNodeValues(it.key, responseKeyValue, type, arrayChildNode)
                resourceAssignment.keyIdentifiers.add(KeyIdentifier(it.key, responseKeyValue))
            }
            return arrayChildNode
        }

        private fun parseObjectResponseNodeWithOneOutputKeyMapping(
            responseArrayNode: MutableMap<String, JsonNode>,
            outputKeyMappingKey: String,
            outputKeyMappingValue: String,
            type: String,
            metadata: MutableMap<String, String>?
        ): ObjectNode {
            val objectNode = JacksonUtils.objectMapper.createObjectNode()
            val responseSingleJsonNode = responseArrayNode.filterKeys { key ->
                key == outputKeyMappingValue
            }.entries.firstOrNull()

            if (responseSingleJsonNode == null) {
                logKeyValueResolvedResource(metadata, outputKeyMappingKey, NullNode.getInstance(), type)
                JacksonUtils.populateJsonNodeValues(outputKeyMappingKey, NullNode.getInstance(), type, objectNode)
            } else {
                logKeyValueResolvedResource(metadata, outputKeyMappingKey, responseSingleJsonNode.value, type)
                JacksonUtils.populateJsonNodeValues(outputKeyMappingKey, responseSingleJsonNode.value, type, objectNode)
            }
            return objectNode
        }

        private fun parseResponseNodeForComplexType(
            responseNode: JsonNode,
            resourceAssignment: ResourceAssignment,
            raRuntimeService: ResourceAssignmentRuntimeService,
            outputKeyMapping: MutableMap<String, String>
        ): JsonNode {
            val entrySchemaType = resourceAssignment.property!!.type
            val dictionaryName = resourceAssignment.dictionaryName!!
            val metadata = resourceAssignment.property!!.metadata
            val outputKeyMappingHasOnlyOneElement = checkIfOutputKeyMappingProvideOneElement(outputKeyMapping)

            if (outputKeyMapping.isNotEmpty()) {
                return when {
                    checkOutputKeyMappingAllElementsInDataTypeProperties(
                        entrySchemaType,
                        outputKeyMapping,
                        raRuntimeService
                    ) -> {
                        parseSingleElementNodeWithAllOutputKeyMapping(
                            resourceAssignment,
                            responseNode,
                            outputKeyMapping,
                            entrySchemaType,
                            metadata
                        )
                    }
                    outputKeyMappingHasOnlyOneElement -> {
                        val outputKeyMap = outputKeyMapping.entries.first()
                        parseSingleElementNodeWithOneOutputKeyMapping(
                            resourceAssignment, responseNode, outputKeyMap.key,
                            outputKeyMap.value, entrySchemaType, metadata
                        )
                    }
                    else -> {
                        throw BlueprintProcessorException("Output-key-mapping do not map the Data Type $entrySchemaType")
                    }
                }
            } else {
                val childNode = JacksonUtils.objectMapper.createObjectNode()
                JacksonUtils.populateJsonNodeValues(dictionaryName, responseNode, entrySchemaType, childNode)
                return childNode
            }
        }

        private fun checkOutputKeyMappingAllElementsInDataTypeProperties(
            dataTypeName: String,
            outputKeyMapping: MutableMap<String, String>,
            raRuntimeService: ResourceAssignmentRuntimeService
        ): Boolean {
            val dataTypeProps = raRuntimeService.bluePrintContext().dataTypeByName(dataTypeName)?.properties
            var result: String? = null
            if (dataTypeProps != null)
                result = outputKeyMapping.filterKeys { !dataTypeProps.containsKey(it) }.keys.firstOrNull()
            return result == null
        }

        private fun logKeyValueResolvedResource(
            metadata: MutableMap<String, String>?,
            key: String,
            value: JsonNode,
            type: String
        ) {
            val valueToPrint = getValueToLog(metadata, value)

            logger.info(
                "For List Type Resource: key ($key), value ($valueToPrint), " +
                    "type  ({$type})"
            )
        }

        private fun checkIfOutputKeyMappingProvideOneElement(outputKeyMapping: MutableMap<String, String>): Boolean {
            return (outputKeyMapping.size == 1)
        }

        fun getValueToLog(metadata: MutableMap<String, String>?, value: Any): Any =
            if (hasLogProtect(metadata)) LOG_REDACTED else value
    }
}
