/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.apps.blueprintsprocessor.db.primary.service.PrimaryDBLibGenericService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.DatabaseResourceSource
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.apps.controllerblueprints.core.*
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

/**
 * PrimaryDataResourceAssignmentProcessor
 *
 * @author Brinda Santh
 */
@Service("resource-assignment-processor-primary-db")
open class PrimaryDataResourceAssignmentProcessor(private val primaryDBLibGenericService: PrimaryDBLibGenericService)
    : ResourceAssignmentProcessor() {

    private val logger = LoggerFactory.getLogger(PrimaryDataResourceAssignmentProcessor::class.java)

    override fun getName(): String {
        return "resource-assignment-processor-primary-db"
    }

    override fun process(resourceAssignment: ResourceAssignment) {
        try {
            validate(resourceAssignment)

            // Check if It has Input
            val value = raRuntimeService.getInputValue(resourceAssignment.name)
            if (value != null && value !is NullNode) {
                logger.info("primary-db source template key (${resourceAssignment.name}) found from input and value is ($value)")
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)
                return
            }

            val dName = resourceAssignment.dictionaryName
            val dSource = resourceAssignment.dictionarySource
            val resourceDefinition = resourceDictionaries[dName]
                    ?: throw BluePrintProcessorException("couldn't get resource dictionary definition for $dName")
            val resourceSource = resourceDefinition.sources[dSource]
                    ?: throw BluePrintProcessorException("couldn't get resource definition $dName source($dSource)")
            val resourceSourceProperties = checkNotNull(resourceSource.properties) { "failed to get source properties for $dName " }
            val sourceProperties = JacksonUtils.getInstanceFromMap(resourceSourceProperties, DatabaseResourceSource::class.java)
            val sql = checkNotNull(sourceProperties.query) { "failed to get request query for $dName under $dSource properties" }
            val inputKeyMapping = checkNotNull(sourceProperties.inputKeyMapping) { "failed to get input-key-mappings for $dName under $dSource properties" }

            logger.info("$dSource dictionary information : ($sql), ($inputKeyMapping), (${sourceProperties.outputKeyMapping})")

            val rows = primaryDBLibGenericService.query(sql, populateNamedParameter(inputKeyMapping))
            if (rows.isNullOrEmpty()) {
                logger.warn("Failed to get $dSource result for dictionary name ($dName) the query ($sql)")
            } else {
                processDBResults(resourceAssignment, sourceProperties, rows)
            }

            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BluePrintProcessorException("Failed in template key ($resourceAssignment) assignments with: ${e.message}", e)
        }
    }

    @Throws(BluePrintProcessorException::class)
    private fun validate(resourceAssignment: ResourceAssignment) {
        checkNotEmptyOrThrow(resourceAssignment.name, "resource assignment template key is not defined")
        checkNotEmptyOrThrow(resourceAssignment.dictionaryName, "resource assignment dictionary name is not defined for template key (${resourceAssignment.name})")
        checkEqualsOrThrow(ResourceDictionaryConstants.SOURCE_PRIMARY_DB, resourceAssignment.dictionarySource) {
            "resource assignment source is not ${ResourceDictionaryConstants.SOURCE_PRIMARY_DB} but it is ${resourceAssignment.dictionarySource}"
        }
    }

    private fun populateNamedParameter(inputKeyMapping: Map<String, String>): Map<String, Any> {
        val namedParameters = HashMap<String, Any>()
        inputKeyMapping.forEach {
            val expressionValue = raRuntimeService.getDictionaryStore(it.value)
            logger.trace("Reference dictionary key (${it.key}) resulted in value ($expressionValue)")
            namedParameters[it.key] = expressionValue
        }
        logger.info("Parameter information : ({})", namedParameters)
        return namedParameters
    }

    @Throws(BluePrintProcessorException::class)
    private fun processDBResults(resourceAssignment: ResourceAssignment, sourceProperties: DatabaseResourceSource, rows: List<Map<String, Any>>) {
        val dName = resourceAssignment.dictionaryName
        val dSource = resourceAssignment.dictionarySource
        val type = nullToEmpty(resourceAssignment.property?.type)

        val outputKeyMapping = checkNotNull(sourceProperties.outputKeyMapping) { "failed to get output-key-mappings for $dName under $dSource properties" }
        logger.info("Response processing type($type)")

        // Primitive Types
        if (BluePrintTypes.validPrimitiveTypes().contains(type)) {
            val dbColumnValue = rows[0][outputKeyMapping[dName]]
            ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, dbColumnValue)
        }
        // Array Types
        else if (BluePrintTypes.validCollectionTypes().contains(type)) {
            lateinit var entrySchemaType: String
            if (resourceAssignment.property?.entrySchema != null) {
                entrySchemaType = nullToEmpty(resourceAssignment.property?.entrySchema?.type)
            }

            if (checkNotEmptyOrThrow(entrySchemaType, "Entry schema is not defined for dictionary ($dName) info")) {
                val arrayNode = JsonNodeFactory.instance.arrayNode()
                rows.forEach {
                    if (BluePrintTypes.validPrimitiveTypes().contains(entrySchemaType)) {
                        val dbColumnValue = it[outputKeyMapping[dName]]
                        // Add Array JSON
                        JacksonUtils.populatePrimitiveValues(dbColumnValue!!, entrySchemaType, arrayNode)
                    } else {
                        val arrayChildNode = JsonNodeFactory.instance.objectNode()
                        for (mapping in outputKeyMapping.entries) {
                            val dbColumnValue = checkNotNull(it[mapping.key])
                            val propertyTypeForDataType = ResourceAssignmentUtils.getPropertyType(raRuntimeService, entrySchemaType, mapping.key)
                            JacksonUtils.populatePrimitiveValues(mapping.key, dbColumnValue, propertyTypeForDataType, arrayChildNode)
                        }
                        arrayNode.add(arrayChildNode)
                    }
                }
                // Set the List of Complex Values
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, arrayNode)
            }
        } else {
            // Complex Types
            val row = rows[0]
            val objectNode = JsonNodeFactory.instance.objectNode()
            for (mapping in outputKeyMapping.entries) {
                val dbColumnValue = checkNotNull(row[mapping.key])
                val propertyTypeForDataType = ResourceAssignmentUtils.getPropertyType(raRuntimeService, type, mapping.key)
                JacksonUtils.populatePrimitiveValues(mapping.key, dbColumnValue, propertyTypeForDataType, objectNode)
            }
            ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, objectNode)
        }
    }

    override fun recover(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
    }
}