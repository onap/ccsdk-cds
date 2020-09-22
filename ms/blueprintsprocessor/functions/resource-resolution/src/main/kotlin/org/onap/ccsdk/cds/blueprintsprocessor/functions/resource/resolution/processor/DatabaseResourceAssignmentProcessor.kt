/*
 *  Copyright © 2018 IBM.
 *  Modifications Copyright © 2017-2018 AT&T Intellectual Property, Bell Canada.
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

import org.onap.ccsdk.cds.blueprintsprocessor.db.BluePrintDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.db.primary.BluePrintDBLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.db.PrimaryDBLibGenericService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.DatabaseResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionConstants.PREFIX_RESOURCE_RESOLUTION_PROCESSOR
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.ExecutionServiceDomains
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.isNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.nullToEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.updateErrorMessage
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.KeyIdentifier
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDictionaryConstants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.util.HashMap

/**
 * DatabaseResourceAssignmentProcessor
 *
 * @author Kapil Singal
 */
@Service("${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-db")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class DatabaseResourceAssignmentProcessor(
    private val bluePrintDBLibPropertyService: BluePrintDBLibPropertyService,
    private val primaryDBLibGenericService: PrimaryDBLibGenericService
) : ResourceAssignmentProcessor() {

    private val logger = LoggerFactory.getLogger(DatabaseResourceAssignmentProcessor::class.java)

    override fun getName(): String {
        return "${PREFIX_RESOURCE_RESOLUTION_PROCESSOR}source-db"
    }

    override suspend fun processNB(resourceAssignment: ResourceAssignment) {
        try {
            validate(resourceAssignment)
            // Check if It has Input
            if (!setFromInput(resourceAssignment)) {
                setValueFromDB(resourceAssignment)
            }
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(resourceAssignment)
        } catch (e: BluePrintProcessorException) {
            val errorMsg = "Failed to process Database resource resolution in template key ($resourceAssignment) assignments."
            throw e.updateErrorMessage(
                ExecutionServiceDomains.RESOURCE_RESOLUTION, errorMsg,
                "Wrong resource definition or DB resolution failed."
            )
        } catch (e: Exception) {
            ResourceAssignmentUtils.setFailedResourceDataValue(resourceAssignment, e.message)
            throw BluePrintProcessorException("Failed in template key ($resourceAssignment) assignments with: ${e.message}", e)
        }
    }

    private fun setValueFromDB(resourceAssignment: ResourceAssignment) {
        val dName = resourceAssignment.dictionaryName!!
        val dSource = resourceAssignment.dictionarySource!!
        val resourceDefinition = resourceDefinition(dName)

        /** Check Resource Assignment has the source definitions, If not get from Resource Definition **/
        val resourceSource = resourceAssignment.dictionarySourceDefinition
            ?: resourceDefinition?.sources?.get(dSource)
            ?: throw BluePrintProcessorException("couldn't get resource definition $dName source($dSource)")
        val resourceSourceProperties = checkNotNull(resourceSource.properties) {
            "failed to get source properties for $dName "
        }
        val sourceProperties =
            JacksonUtils.getInstanceFromMap(resourceSourceProperties, DatabaseResourceSource::class.java)

        val sql = checkNotNull(sourceProperties.query) {
            "failed to get request query for $dName under $dSource properties"
        }
        val inputKeyMapping = checkNotNull(sourceProperties.inputKeyMapping) {
            "failed to get input-key-mappings for $dName under $dSource properties"
        }

        sourceProperties.inputKeyMapping
            ?.mapValues { raRuntimeService.getDictionaryStore(it.value) }
            ?.map { KeyIdentifier(it.key, it.value) }
            ?.let { resourceAssignment.keyIdentifiers.addAll(it) }

        logger.info(
            "DatabaseResource ($dSource) dictionary information: " +
                "Query:($sql), input-key-mapping:($inputKeyMapping), output-key-mapping:(${sourceProperties.outputKeyMapping})"
        )
        val jdbcTemplate = blueprintDBLibService(sourceProperties, dSource)

        val rows = jdbcTemplate.query(sql, populateNamedParameter(inputKeyMapping))
        if (rows.isNullOrEmpty()) {
            logger.warn("Failed to get $dSource result for dictionary name ($dName) the query ($sql)")
        } else {
            populateResource(resourceAssignment, sourceProperties, rows)
        }
    }

    private fun blueprintDBLibService(sourceProperties: DatabaseResourceSource, selector: String): BluePrintDBLibGenericService {
        return if (isNotEmpty(sourceProperties.endpointSelector)) {
            val dbPropertiesJson = raRuntimeService.resolveDSLExpression(sourceProperties.endpointSelector!!)
            bluePrintDBLibPropertyService.JdbcTemplate(dbPropertiesJson)
        } else {
            bluePrintDBLibPropertyService.JdbcTemplate(selector)
        }
    }

    @Throws(BluePrintProcessorException::class)
    private fun validate(resourceAssignment: ResourceAssignment) {
        checkNotEmpty(resourceAssignment.name) { "resource assignment template key is not defined" }
        checkNotEmpty(resourceAssignment.dictionaryName) {
            "resource assignment dictionary name is not defined for template key (${resourceAssignment.name})"
        }
        check(resourceAssignment.dictionarySource in getListOfDBSources()) {
            "resource assignment source is not ${ResourceDictionaryConstants.PROCESSOR_DB} but it is ${resourceAssignment.dictionarySource}"
        }
    }

    // placeholder to get the list of DB sources.
    // TODO: This will be replaced with a DB
    private fun getListOfDBSources(): Array<String> = arrayOf(ResourceDictionaryConstants.PROCESSOR_DB)

    private fun populateNamedParameter(inputKeyMapping: Map<String, String>): Map<String, Any> {
        val namedParameters = HashMap<String, Any>()
        inputKeyMapping.forEach {
            val expressionValue = raRuntimeService.getResolutionStore(it.value).textValue()
            logger.trace("Reference dictionary key (${it.key}) resulted in value ($expressionValue)")
            namedParameters[it.key] = expressionValue
        }
        if (namedParameters.isNotEmpty()) {
            logger.info("Parameter information : ($namedParameters)")
        }
        return namedParameters
    }

    @Throws(BluePrintProcessorException::class)
    private fun populateResource(
        resourceAssignment: ResourceAssignment,
        sourceProperties: DatabaseResourceSource,
        rows: List<Map<String, Any>>
    ) {
        val dName = resourceAssignment.dictionaryName
        val dSource = resourceAssignment.dictionarySource
        val type = nullToEmpty(resourceAssignment.property?.type)

        val outputKeyMapping = checkNotNull(sourceProperties.outputKeyMapping) {
            "failed to get output-key-mappings for $dName under $dSource properties"
        }
        logger.info("Response processing type ($type)")

        val responseNode = checkNotNull(JacksonUtils.getJsonNode(rows)) {
            "Failed to get database query result into Json node."
        }

        val parsedResponseNode = ResourceAssignmentUtils.parseResponseNode(
            responseNode, resourceAssignment,
            raRuntimeService, outputKeyMapping
        )
        ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, parsedResponseNode)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        raRuntimeService.getBluePrintError().addError(runtimeException.message!!)
    }
}
