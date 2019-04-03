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
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintJinjaTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import java.util.*

abstract class ResourceAssignmentProcessor : BlueprintFunctionNode<ResourceAssignment, Boolean> {

    private val log = LoggerFactory.getLogger(ResourceAssignmentProcessor::class.java)

    lateinit var raRuntimeService: ResourceAssignmentRuntimeService
    lateinit var resourceDictionaries: MutableMap<String, ResourceDefinition>

    var scriptPropertyInstances: MutableMap<String, Any> = hashMapOf()
    lateinit var scriptType: String

    /**
     * This will be called from the scripts to serve instance from runtime to scripts.
     */
    open fun <T> scriptPropertyInstanceType(name: String): T {
        return scriptPropertyInstances as? T
                ?: throw BluePrintProcessorException("couldn't get script property instance ($name)")
    }

    open fun getFromInput(resourceAssignment: ResourceAssignment): JsonNode? {
        var value: JsonNode? = null
        try {
            value = raRuntimeService.getInputValue(resourceAssignment.name)
            ResourceAssignmentUtils.setResourceDataValue(resourceAssignment, raRuntimeService, value)
        } catch (e: BluePrintProcessorException) {
            // NoOp - couldn't find value from input
        }
        return value
    }

    open fun resourceDefinition(name: String): ResourceDefinition {
        return resourceDictionaries[name]
                ?: throw BluePrintProcessorException("couldn't get resource definition for ($name)")
    }

    open fun resolveInputKeyMappingVariables(inputKeyMapping: Map<String, String>): Map<String, Any> {
        val resolvedInputKeyMapping = HashMap<String, Any>()
        if (MapUtils.isNotEmpty(inputKeyMapping)) {
            for ((key, value) in inputKeyMapping) {
                val resultValue = raRuntimeService.getResolutionStore(value)
                val expressionValue = JacksonUtils.getValue(resultValue)
                log.trace("Reference dictionary key ({}), value ({})", key, expressionValue)
                resolvedInputKeyMapping[key] = expressionValue
            }
        }
        return resolvedInputKeyMapping
    }

    open suspend fun resolveFromInputKeyMapping(valueToResolve: String, keyMapping: MutableMap<String, Any>):
            String {
        if (valueToResolve.isEmpty() || !valueToResolve.contains("$")) {
            return valueToResolve
        }
        return BluePrintVelocityTemplateService.generateContent(valueToResolve, additionalContext = keyMapping)
    }

    final override suspend fun applyNB(resourceAssignment: ResourceAssignment): Boolean {
        try {
            processNB(resourceAssignment)
        } catch (runtimeException: RuntimeException) {
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
            recoverNB(runtimeException, resourceAssignment)
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
            log.error("failed in ${getName()} : ${runtimeException.message}", runtimeException)
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