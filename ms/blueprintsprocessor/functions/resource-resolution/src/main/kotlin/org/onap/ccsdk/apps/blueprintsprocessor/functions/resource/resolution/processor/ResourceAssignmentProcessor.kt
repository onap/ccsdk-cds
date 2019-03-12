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

import com.fasterxml.jackson.databind.JsonNode
import org.apache.commons.collections.MapUtils
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.ResourceAssignmentRuntimeService
import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.utils.ResourceAssignmentUtils
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintTemplateService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceDefinition
import org.slf4j.LoggerFactory
import java.util.*

abstract class ResourceAssignmentProcessor : BlueprintFunctionNode<ResourceAssignment, ResourceAssignment> {

    private val log = LoggerFactory.getLogger(ResourceAssignmentProcessor::class.java)

    lateinit var raRuntimeService: ResourceAssignmentRuntimeService
    lateinit var resourceDictionaries: MutableMap<String, ResourceDefinition>

    var scriptPropertyInstances: Map<String, Any> = hashMapOf()

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

    open fun resolveFromInputKeyMapping(valueToResolve: String, keyMapping: Map<String, Any>): String {
        if (valueToResolve.isEmpty() || !valueToResolve.contains("$")) {
            return valueToResolve
        }
        return BluePrintTemplateService.generateContent(valueToResolve, additionalContext = keyMapping)
    }

    override fun prepareRequest(resourceAssignment: ResourceAssignment): ResourceAssignment {
        log.info("prepareRequest for ${resourceAssignment.name}, dictionary(${resourceAssignment.dictionaryName})," +
                "source(${resourceAssignment.dictionarySource})")
        return resourceAssignment
    }

    override fun prepareResponse(): ResourceAssignment {
        log.info("Preparing Response...")
        return ResourceAssignment()
    }

    override fun apply(resourceAssignment: ResourceAssignment): ResourceAssignment {
        try {
            prepareRequest(resourceAssignment)
            process(resourceAssignment)
        } catch (runtimeException: RuntimeException) {
            recover(runtimeException, resourceAssignment)
        }
        return prepareResponse()
    }

}