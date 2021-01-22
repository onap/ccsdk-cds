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
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonNode
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.isNullOrMissing
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import java.util.HashMap

abstract class ResourceAssignmentProcessor : BlueprintFunctionNode<ResourceAssignment, Boolean> {

    private val log = LoggerFactory.getLogger(ResourceAssignmentProcessor::class.java)

    lateinit var raRuntimeService: ResourceAssignmentRuntimeService
    var resourceDictionaries: MutableMap<String, ResourceDefinition> = hashMapOf()
    var resourceAssignments: MutableList<ResourceAssignment> = arrayListOf()

    var scriptPropertyInstances: MutableMap<String, Any> = hashMapOf()
    lateinit var scriptType: String

    /**
     * This will be called from the scripts to serve instance from runtime to scripts.
     */
    open fun <T> scriptPropertyInstanceType(name: String): T {
        return scriptPropertyInstances as? T
            ?: throw BlueprintProcessorException("couldn't get script property instance ($name)")
    }

    open fun setFromInput(resourceAssignment: ResourceAssignment): Boolean {
        try {
            val value = raRuntimeService.getInputValue(resourceAssignment.name)
            if (!value.isNullOrMissing()) {
                log.debug(
                    "For Resource:(${resourceAssignment.name}) found value:({}) in input-data.",
                    ResourceAssignmentUtils.getValueToLog(resourceAssignment.property?.metadata, value)
                )
                ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)
                return true
            }
        } catch (e: BlueprintProcessorException) {
            // NoOp - couldn't find value from input
        }
        return false
    }

    open fun setFromInputKeyDependencies(keys: MutableList<String>, resourceAssignment: ResourceAssignment): Boolean {
        try {
            for (dependencyKey in keys) {
                var value = raRuntimeService.getInputValue(dependencyKey)
                if (!value.isNullOrMissing()) {
                    log.debug(
                        "For Resource:(${resourceAssignment.name}) found value:({}) in input-data under: ($dependencyKey).",
                        ResourceAssignmentUtils.getValueToLog(resourceAssignment.property?.metadata, value)
                    )
                    ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)
                    return true
                }
            }
        } catch (e: BlueprintProcessorException) {
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
            // TODO("Optimize to JSON Node directly without velocity").asJsonNode().toString()
            return BlueprintVelocityTemplateService.generateContent(valueToResolve, keyMapping.asJsonNode().toString())
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
            BlueprintConstants.SCRIPT_JYTHON -> {
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
        throw BlueprintException("Not Implemented, use applyNB method")
    }

    final override fun prepareRequest(resourceAssignment: ResourceAssignment): ResourceAssignment {
        throw BlueprintException("Not Implemented required")
    }

    final override fun prepareResponse(): Boolean {
        throw BlueprintException("Not Implemented required")
    }

    final override suspend fun prepareRequestNB(resourceAssignment: ResourceAssignment): ResourceAssignment {
        throw BlueprintException("Not Implemented required")
    }

    final override suspend fun prepareResponseNB(): Boolean {
        throw BlueprintException("Not Implemented required")
    }

    override fun process(resourceAssignment: ResourceAssignment) {
        throw BlueprintException("Not Implemented, child class will implement this")
    }

    override fun recover(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {
        throw BlueprintException("Not Implemented, child class will implement this")
    }

    fun addError(type: String, name: String, error: String) {
        raRuntimeService.getBlueprintError().addError(type, name, error)
    }

    fun addError(error: String) {
        raRuntimeService.getBlueprintError().addError(error)
    }

    fun isTemplateKeyValueNull(resourceAssignment: ResourceAssignment): Boolean {
        val resourceProp = checkNotNull(resourceAssignment.property) {
            "Failed to populate mandatory resource resource mapping $resourceAssignment"
        }
        if (resourceProp.required != null && resourceProp.required!! &&
            resourceProp.value.isNullOrMissing()
        ) {
            return true
        }
        return false
    }
}
