/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonReactorUtils
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
                    logger.info("Setting Resource Value ($value) for Resource Name " +
                            "(${resourceAssignment.dictionaryName}) of type (${resourceProp.type})")
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
        fun assertTemplateKeyValueNotNull(resourceAssignment: ResourceAssignment) {
            val resourceProp = checkNotNull(resourceAssignment.property) {
                "Failed to populate mandatory resource resource mapping $resourceAssignment"
            }
            if (resourceProp.required != null && resourceProp.required!!
                    && (resourceProp.value == null || resourceProp.value !is NullNode)) {
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

                assignments.forEach {
                    if (isNotEmpty(it.name) && it.property != null) {
                        val rName = it.name
                        val type = nullToEmpty(it.property?.type).toLowerCase()
                        val value = it.property?.value
                        logger.info("Generating Resource name ($rName), type ($type), value ($value)")
                        root.set(rName, value)
                    }
                }
                result = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root)
                logger.info("Generated Resource Param Data ($result)")
            } catch (e: Exception) {
                throw BluePrintProcessorException("Resource Assignment is failed with $e.message", e)
            }

            return result
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
    }
}