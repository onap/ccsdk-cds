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

import java.util.Date
import org.apache.commons.lang3.StringUtils
import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.apps.controllerblueprints.core.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment

class ResourceResolutionUtils {
    companion object {

        private val logger: EELFLogger = EELFManager.getInstance().getLogger(ResourceResolutionUtils::class.toString())

        @Synchronized
        @Throws(BluePrintProcessorException::class)
        fun setResourceDataValue(resourceAssignment: ResourceAssignment, value: Any?) {

            val resourceProp = checkNotNull(resourceAssignment.property) { "Failed in setting resource value for resource mapping $resourceAssignment" }
            checkNotEmptyNThrow(resourceAssignment.name, "Failed in setting resource value for resource mapping $resourceAssignment")

            if (checkNotEmpty(resourceAssignment.dictionaryName)) {
                resourceAssignment.dictionaryName = resourceAssignment.name
                logger.warn("Missing dictionary key, setting with template key (${resourceAssignment.name}) as dictionary key (${resourceAssignment.dictionaryName})")
            }

            try {
                if (checkNotEmpty(resourceProp.type)) {
                    val convertedValue = convertResourceValue(resourceProp.type, value)
                    logger.info("Setting Resource Value ($convertedValue) for Resource Name (${resourceAssignment.dictionaryName}) of type (${resourceProp.type})")
                    resourceProp.value = convertedValue
                    resourceAssignment.updatedDate = Date()
                    resourceAssignment.updatedBy = BluePrintConstants.USER_SYSTEM
                    resourceAssignment.status = BluePrintConstants.STATUS_SUCCESS
                }
            } catch (e: Exception) {
                throw BluePrintProcessorException("Failed in setting value for template key (%s) and " +
                        "dictionary key (${resourceAssignment.name}) of type (${resourceProp.type}) with error message (${e.message})", e)
            }
        }

        private fun convertResourceValue(type: String, value: Any?): JsonNode? {
            var convertedValue: JsonNode?

            if (value == null || value is NullNode) {
                logger.info("Returning {} value from convertResourceValue", value)
                return null
            } else if (BluePrintTypes.validPrimitiveTypes().contains(type) && value is String) {
                convertedValue = JacksonUtils.convertPrimitiveResourceValue(type, value)
            } else {
                // Case where Resource is non-primitive type
                if (value is String) {
                    convertedValue = JacksonUtils.jsonNode(value)
                } else {
                    convertedValue = JacksonUtils.getJsonNode(value)
                }
            }
            return convertedValue
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

                        val type = it.property?.type
                        val value = it.property?.value
                        logger.info("Generating Resource name ({}), type ({}), value ({})", it.name, type,
                                value)
                        if (value == null) {
                            (root as ObjectNode).set(it.name, null)
                        } else if (value is JsonNode) {
                            (root as ObjectNode).put(it.name, value as JsonNode)
                        } else if (BluePrintConstants.DATA_TYPE_STRING.equals(type, ignoreCase = true)) {
                            (root as ObjectNode).put(it.name, value as String)
                        } else if (BluePrintConstants.DATA_TYPE_BOOLEAN.equals(type, ignoreCase = true)) {
                            (root as ObjectNode).put(it.name, value as Boolean)
                        } else if (BluePrintConstants.DATA_TYPE_INTEGER.equals(type, ignoreCase = true)) {
                            (root as ObjectNode).put(it.name, value as Int)
                        } else if (BluePrintConstants.DATA_TYPE_FLOAT.equals(type, ignoreCase = true)) {
                            (root as ObjectNode).put(it.name, value as Float)
                        } else if (BluePrintConstants.DATA_TYPE_TIMESTAMP.equals(type, ignoreCase = true)) {
                            (root as ObjectNode).put(it.name, value as String)
                        } else {
                            val jsonNode = JacksonUtils.getJsonNode(value)
                            if (jsonNode != null) {
                                (root as ObjectNode).put(it.name, jsonNode)
                            } else {
                                (root as ObjectNode).set(it.name, null)
                            }
                        }
                    }
                }
                result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                logger.info("Generated Resource Param Data ({})", result)
            } catch (e: Exception) {
                throw BluePrintProcessorException("kapil is failing with $e.message", e)
            }

            return result
        }

        fun <T> transformResourceSource(properties: MutableMap<String, JsonNode>, classType: Class<T>): T {
            val content = JacksonUtils.getJson(properties)
            return JacksonUtils.readValue(content, classType)
                    ?: throw BluePrintProcessorException("failed to transform content($content) to type($classType)")
        }

    }
}