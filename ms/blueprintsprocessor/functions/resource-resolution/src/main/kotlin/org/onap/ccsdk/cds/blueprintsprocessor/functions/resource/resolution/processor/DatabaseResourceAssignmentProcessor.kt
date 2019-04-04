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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.processor

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.NullNode
import org.onap.ccsdk.cds.blueprintsprocessor.db.BluePrintDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.BluePrintDBLibPropertySevice
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.PrimaryDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.DatabaseResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.*

/**
 * DatabaseResourceAssignmentProcessor
 *
 * @author Kapil Singal
 */
@Service("${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-processor-db")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class DatabaseResourceAssignmentProcessor(private val bluePrintDBLibPropertySevice: BluePrintDBLibPropertySevice, private val primaryDBLibGenericService: PrimaryDBLibGenericService)
    : ResourceAssignmentProcessor() {

    private val logger = LoggerFactory.getLogger(DatabaseResourceAssignmentProcessor::class.java)

    override fun getName(): String {
        return "${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-processor-db"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        try {
            validate(resourceAssignment)

            // Check if It has Input
            try {
                val value = raRuntimeService.getInputValue(resourceAssignment.name)
                if (value !is NullNode && value !is MissingNode) {
                    logger.info("processor-db source template key (${resourceAssignment.name}) found from input and value is ($value)")
                    ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)
                } else {
                    setValueFromDB(resourceAssignment)
                }
            } catch (e: BluePrintProcessorException) {
                setValueFromDB(resourceAssignment)
            }

            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BluePrintProcessorException("Failed in template key ($resourceAssignment) assignments with: ${e.message}", e)
        }
    }

    private fun setValueFromDB(resourceAssignment: ResourceAssignment) {
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
        val jdbcTemplate = blueprintDBLibService(sourceProperties)

        val rows = jdbcTemplate.query(sql, populateNamedParameter(inputKeyMapping))
        if (rows.isNullOrEmpty()) {
            logger.warn("Failed to get $dSource result for dictionary name ($dName) the query ($sql)")
        } else {
            populateResource(resourceAssignment, sourceProperties, rows)
        }
    }

    private fun blueprintDBLibService(sourceProperties: DatabaseResourceSource): BluePrintDBLibGenericService {
        return if (isNotEmpty(sourceProperties.endpointSelector)) {
            val dbPropertiesJson = raRuntimeService.resolveDSLExpression(sourceProperties.endpointSelector!!)
            bluePrintDBLibPropertySevice.JdbcTemplate(dbPropertiesJson)
        } else {
            primaryDBLibGenericService
        }

    }

    @Throws(BluePrintProcessorException::class)
    private fun validate(resourceAssignment: ResourceAssignment) {
        checkNotEmpty(resourceAssignment.name) { "resource assignment template key is not defined" }
        checkNotEmpty(resourceAssignment.dictionaryName) {
            "resource assignment dictionary name is not defined for template key (${resourceAssignment.name})"
        }
        checkEquals(ResourceDictionaryConstants.SOURCE_PROCESSOR_DB, resourceAssignment.dictionarySource) {
            "resource assignment source is not ${ResourceDictionaryConstants.SOURCE_PROCESSOR_DB} but it is ${resourceAssignment.dictionarySource}"
        }
    }

    private fun populateNamedParameter(inputKeyMapping: Map<String, String>): Map<String, Any> {
        val namedParameters = HashMap<String, Any>()
        inputKeyMapping.forEach {
            val expressionValue = raRuntimeService.getDictionaryStore(it.value).textValue()
            logger.trace("Reference dictionary key (${it.key}) resulted in value ($expressionValue)")
            namedParameters[it.key] = expressionValue
        }
        logger.info("Parameter information : ({})", namedParameters)
        return namedParameters
    }

    @Throws(BluePrintProcessorException::class)
    private fun populateResource(resourceAssignment: ResourceAssignment, sourceProperties: DatabaseResourceSource, rows: List<Map<String, Any>>) {
        val dName = resourceAssignment.dictionaryName
        val dSource = resourceAssignment.dictionarySource
        val type = nullToEmpty(resourceAssignment.property?.type)

        val outputKeyMapping = checkNotNull(sourceProperties.outputKeyMapping) { "failed to get output-key-mappings for $dName under $dSource properties" }
        logger.info("Response processing type($type)")

        // Primitive Types
        when (type) {
            in BluePrintTypes.validPrimitiveTypes() -> {
                val dbColumnValue = rows[0][outputKeyMapping[dName]]
                logger.info("For template key (${resourceAssignment.name}) setting value as ($dbColumnValue)")
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, dbColumnValue)
            }
            in BluePrintTypes.validCollectionTypes() -> {
                val entrySchemaType = returnNotEmptyOrThrow(resourceAssignment.property?.entrySchema?.type) { "Entry schema is not defined for dictionary ($dName) info" }
                val arrayNode = JsonNodeFactory.instance.arrayNode()
                rows.forEach {
                    if (entrySchemaType in BluePrintTypes.validPrimitiveTypes()) {
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
                logger.info("For template key (${resourceAssignment.name}) setting value as ($arrayNode)")
                // Set the List of Complex Values
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, arrayNode)
            }
            else -> {
                // Complex Types
                val row = rows[0]
                val objectNode = JsonNodeFactory.instance.objectNode()
                for (mapping in outputKeyMapping.entries) {
                    val dbColumnValue = checkNotNull(row[mapping.key])
                    val propertyTypeForDataType = ResourceAssignmentUtils.getPropertyType(raRuntimeService, type, mapping.key)
                    JacksonUtils.populatePrimitiveValues(mapping.key, dbColumnValue, propertyTypeForDataType, objectNode)
                }
                logger.info("For template key (${resourceAssignment.name}) setting value as ($objectNode)")
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, objectNode)
            }
        }
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        raRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }
}