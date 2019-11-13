/*
 *  Copyright © 2018 IBM.
 *
 *  Modifications Copyright © 2017-2019 AT&T, Bell Canada
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

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.collections.MapUtils
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.RestResourceSource
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.*
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import java.util.*

abstract class ResourceAssignmentProcessor : BlueprintFunctionNode<ResourceAssignment, Boolean> {

    private val log = LoggerFactory.getLogger(ResourceAssignmentProcessor::class.java)

    lateinit var raRuntimeService: ResourceAssignmentRuntimeService
    var resourceDictionaries: MutableMap<String, ResourceDefinition> = hashMapOf()

    var scriptPropertyInstances: MutableMap<String, Any> = hashMapOf()
    lateinit var scriptType: String

    /**
     * This will be called from the scripts to serve instance from runtime to scripts.
     */
    open fun <T> scriptPropertyInstanceType(name: String): T {
        return scriptPropertyInstances as? T
            ?: throw BluePrintProcessorException("couldn't get script property instance ($name)")
    }

    open suspend fun setFromInput(resourceAssignment: ResourceAssignment): Boolean {
        val dName = resourceAssignment.dictionaryName!!
        val dSource = resourceAssignment.dictionarySource!!
        val resourceDefinition = resourceDefinition(dName)

        /** Check Resource Assignment has the source definitions, If not get from Resource Definitions **/
        val resourceSource = resourceAssignment.dictionarySourceDefinition
                ?: resourceDefinition?.sources?.get(dSource)
                ?: throw BluePrintProcessorException("couldn't get resource definition $dName source($dSource)")

        val resourceSourceProperties =
                checkNotNull(resourceSource.properties) { "failed to get source properties for $dName " }

        val sourceProperties =
                JacksonUtils.getInstanceFromMap(resourceSourceProperties, RestResourceSource::class.java)

        val inputKeyMapping =
                checkNotNull(sourceProperties.inputKeyMapping) { "failed to get input-key-mappings for $dName under $dSource properties" }
        val outputKeyMapping = checkNotNull(sourceProperties.outputKeyMapping) {
            "failed to get output-key-mappings for $dName under $dSource properties"
        }
        val resolvedInputKeyMapping = resolveInputKeyMappingVariables(inputKeyMapping).toMutableMap()
        try {
            // Resolving content Variables
            val valueResolved = resolveFromInputKeyMapping(raRuntimeService.getInputValue(resourceAssignment.name).toString(),
                    resolvedInputKeyMapping)

            val parsedResponseNode = ResourceAssignmentUtils.parseResponseNode(
                    valueResolved.asJsonType(resourceAssignment.property!!.type), resourceAssignment,
                    raRuntimeService, outputKeyMapping
            )
            if (!parsedResponseNode.isNullOrMissing()) {
                log.debug("For Resource:(${resourceAssignment.name}) found value:({}) in input-data.",
                    ResourceAssignmentUtils.getValueToLog(resourceAssignment.property?.metadata, parsedResponseNode))
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, parsedResponseNode)
                return true
            }
        } catch (e: BluePrintProcessorException) {
            // NoOp - couldn't find value from input
        }
        return false
    }

    open fun resourceDefinition(name: String): ResourceDefinition? {
        return if (resourceDictionaries.containsKey(name)) resourceDictionaries[name] else null
    }

    open fun resolveInputKeyMappingVariables(inputKeyMapping: Map<String, String>): Map<String, JsonNode> {
        val resolvedInputKeyMapping = HashMap<String, JsonNode>()
        if (MapUtils.isNotEmpty(inputKeyMapping)) {
            for ((key, value) in inputKeyMapping) {
                val resultValue = raRuntimeService.getResolutionStore(value)
                resolvedInputKeyMapping[key] = resultValue
            }
        }
        return resolvedInputKeyMapping
    }

    open suspend fun resolveFromInputKeyMapping(valueToResolve: String, keyMapping: MutableMap<String, JsonNode>):
            String {
        if (valueToResolve.isEmpty() || !valueToResolve.contains("$")) {
            return valueToResolve
        }
        //TODO("Optimize to JSON Node directly without velocity").asJsonNode().toString()
        return BluePrintVelocityTemplateService.generateContent(valueToResolve, keyMapping.asJsonNode().toString())
    }

    final override suspend fun applyNB(resourceAssignment: ResourceAssignment): Boolean {
        try {
            processNB(resourceAssignment)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, resourceAssignment)
            return false
        }
        return true
    }

    suspend fun executeScript(resourceAssignment: ResourceAssignment) {
        return when (scriptType) {
            BluePrintConstants.SCRIPT_JYTHON -> {
                executeScriptBlocking(resourceAssignment)
            }
            else -> {
                executeScriptNB(resourceAssignment)
            }
        }
    }

    private suspend fun executeScriptNB(resourceAssignment: ResourceAssignment) {
        try {
            processNB(resourceAssignment)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, resourceAssignment)
        }
    }

    private fun executeScriptBlocking(resourceAssignment: ResourceAssignment) {
        try {
            process(resourceAssignment)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ResourceAssignmentProcessor : ${runtimeException.message}", runtimeException)
            recover(runtimeException, resourceAssignment)
        }
    }

    /**
     * If Jython Script, Override Blocking methods(process() and recover())
     * If Kotlin or Internal Scripts, Override non blocking methods ( processNB() and recoverNB()), so default
     * blocking
     * methods will have default implementation,
     *
     * Always applyNB() method will be invoked, apply() won't be called from parent
     */

    final override fun apply(resourceAssignment: ResourceAssignment): Boolean {
        throw BluePrintException("Not Implemented, use applyNB method")
    }

    final override fun prepareRequest(resourceAssignment: ResourceAssignment): ResourceAssignment {
        throw BluePrintException("Not Implemented required")
    }

    final override fun prepareResponse(): Boolean {
        throw BluePrintException("Not Implemented required")
    }

    final override suspend fun prepareRequestNB(resourceAssignment: ResourceAssignment): ResourceAssignment {
        throw BluePrintException("Not Implemented required")
    }

    final override suspend fun prepareResponseNB(): Boolean {
        throw BluePrintException("Not Implemented required")
    }

    override fun process(resourceAssignment: ResourceAssignment) {
        throw BluePrintException("Not Implemented, child class will implement this")
    }

    override fun recover(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        throw BluePrintException("Not Implemented, child class will implement this")
    }

    fun addError(type: String, name: String, error: String) {
        raRuntimeService.getBluePrintError().addError(type, name, error)
    }

    fun addError(error: String) {
        raRuntimeService.getBluePrintError().addError(error)
    }
}