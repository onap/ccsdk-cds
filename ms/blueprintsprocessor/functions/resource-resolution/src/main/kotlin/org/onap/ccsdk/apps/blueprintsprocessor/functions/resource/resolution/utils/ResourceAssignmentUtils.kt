/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.*
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import java.util.*

class ResourceAssignmentUtils {
    companion object {

        private val logger: EELFLogger = EELFManager.getInstance().getLogger(ResourceAssignmentUtils::class.toString())

        @Synchronized
        @Throws(BluePrintProcessorException::class)
        fun setResourceDataValue(resourceAssignment: ResourceAssignment, raRuntimeService: ResourceAssignmentRuntimeService, value: Any?) {

            val resourceProp = checkNotNull(resourceAssignment.property) { "Failed in setting resource value for resource mapping $resourceAssignment" }
            checkNotEmptyOrThrow(resourceAssignment.name, "Failed in setting resource value for resource mapping $resourceAssignment")

            if (checkNotEmpty(resourceAssignment.dictionaryName)) {
                resourceAssignment.dictionaryName = resourceAssignment.name
                logger.warn("Missing dictionary key, setting with template key (${resourceAssignment.name}) as dictionary key (${resourceAssignment.dictionaryName})")
            }

            try {
                if (checkNotEmpty(resourceProp.type)) {
                    val convertedValue = convertResourceValue(resourceProp.type, value)
                    logger.info("Setting Resource Value ($convertedValue) for Resource Name (${resourceAssignment.dictionaryName}) of type (${resourceProp.type})")
                    setResourceValue(resourceAssignment, raRuntimeService, convertedValue)
                    resourceAssignment.updatedDate = Date()
                    resourceAssignment.updatedBy = BluePrintConstants.USER_SYSTEM
                    resourceAssignment.status = BluePrintConstants.STATUS_SUCCESS
                }
            } catch (e: Exception) {
                throw BluePrintProcessorException("Failed in setting value for template key (${resourceAssignment.name}) and " +
                        "dictionary key (${resourceAssignment.dictionaryName}) of type (${resourceProp.type}) with error message (${e.message})", e)
            }
        }

        private fun setResourceValue(resourceAssignment: ResourceAssignment, raRuntimeService: ResourceAssignmentRuntimeService, value: JsonNode) {
            raRuntimeService.putResolutionStore(resourceAssignment.name, value)
            raRuntimeService.putDictionaryStore(resourceAssignment.dictionaryName!!, value)
            resourceAssignment.property!!.value = value
        }

        private fun convertResourceValue(type: String, value: Any?): JsonNode {

            return if (value == null || value is NullNode) {
                logger.info("Returning {} value from convertResourceValue", value)
                NullNode.instance
            } else if (BluePrintTypes.validPrimitiveTypes().contains(type) && value is String) {
                JacksonUtils.convertPrimitiveResourceValue(type, value)
            } else if (value is String) {
                JacksonUtils.jsonNode(value)
            } else {
                JacksonUtils.getJsonNode(value)
            }

        }

        @Synchronized
        fun setFailedResourceDataValue(resourceAssignment: ResourceAssignment, message: String?) {
            if (checkNotEmpty(resourceAssignment.name)) {
                resourceAssignment.updatedDate = Date()
                resourceAssignment.updatedBy = BluePrintConstants.USER_SYSTEM
                resourceAssignment.status = BluePrintConstants.STATUS_FAILURE
                resourceAssignment.message = message
            }
        }

        @Synchronized
        @Throws(BluePrintProcessorException::class)
        fun assertTemplateKeyValueNotNull(resourceAssignment: ResourceAssignment) {
            val resourceProp = checkNotNull(resourceAssignment.property) { "Failed to populate mandatory resource resource mapping $resourceAssignment" }
            if (resourceProp.required != null && resourceProp.required!! && (resourceProp.value == null || resourceProp.value !is NullNode)) {
                logger.error("failed to populate mandatory resource mapping ($resourceAssignment)")
                throw BluePrintProcessorException("failed to populate mandatory resource mapping ($resourceAssignment)")
            }
        }

        @Synchronized
        @Throws(BluePrintProcessorException::class)
        fun generateResourceDataForAssignments(assignments: List<ResourceAssignment>): String {
            var result = "{}"
            try {
                val mapper = ObjectMapper()
                val root = mapper.readTree(result)

                assignments.forEach {
                    if (checkNotEmpty(it.name) && it.property != null) {
                        val rName = it.name
                        val type = nullToEmpty(it.property?.type).toLowerCase()
                        val value = it.property?.value
                        logger.info("Generating Resource name ($rName), type ($type), value ($value)")

                        when (value) {
                            null -> (root as ObjectNode).set(rName, null)
                            is JsonNode -> (root as ObjectNode).set(rName, value)
                            else -> {
                                when (type) {
                                    BluePrintConstants.DATA_TYPE_TIMESTAMP -> (root as ObjectNode).put(rName, value as String)
                                    BluePrintConstants.DATA_TYPE_STRING -> (root as ObjectNode).put(rName, value as String)
                                    BluePrintConstants.DATA_TYPE_BOOLEAN -> (root as ObjectNode).put(rName, value as Boolean)
                                    BluePrintConstants.DATA_TYPE_INTEGER -> (root as ObjectNode).put(rName, value as Int)
                                    BluePrintConstants.DATA_TYPE_FLOAT -> (root as ObjectNode).put(rName, value as Float)
                                    else -> {
                                        if (JacksonUtils.getJsonNode(value) != null) {
                                            (root as ObjectNode).set(rName, JacksonUtils.getJsonNode(value))
                                        } else {
                                            (root as ObjectNode).set(rName, null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                logger.info("Generated Resource Param Data ($result)")
            } catch (e: Exception) {
                throw BluePrintProcessorException("Resource Assignment is failed with $e.message", e)
            }

            return result
        }

        fun transformToRARuntimeService(blueprintRuntimeService: BluePrintRuntimeService<*>, templateArtifactName: String): ResourceAssignmentRuntimeService {
            val resourceAssignmentRuntimeService = ResourceAssignmentRuntimeService(blueprintRuntimeService.id(), blueprintRuntimeService.bluePrintContext())
            resourceAssignmentRuntimeService.createUniqueId(templateArtifactName)
            resourceAssignmentRuntimeService.setExecutionContext(blueprintRuntimeService.getExecutionContext() as MutableMap<String, JsonNode>)

            return resourceAssignmentRuntimeService
        }

        /*
         * Populate the Field property type for the Data type
         */
        @Synchronized
        @Throws(BluePrintProcessorException::class)
        fun getPropertyType(raRuntimeService: ResourceAssignmentRuntimeService, dataTypeName: String, propertyName: String): String {
            lateinit var type: String
            try {
                val dataTypeProps = checkNotNull(raRuntimeService.bluePrintContext().dataTypeByName(dataTypeName)?.properties)
                val propertyDefinition = checkNotNull(dataTypeProps[propertyName])
                type = returnNotEmptyOrThrow(propertyDefinition.type) { "Couldn't get data type ($dataTypeName)" }
                logger.trace("Data type({})'s property ({}) is ({})", dataTypeName, propertyName, type)
            } catch (e: Exception) {
                logger.error("couldn't get data type($dataTypeName)'s property ($propertyName), error message $e")
                throw BluePrintProcessorException("${e.message}", e)
            }
            return type
        }
    }
}