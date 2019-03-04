/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor

import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.CapabilityResourceSource
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.scripts.BlueprintJythonService
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintScriptsService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service
import java.io.File

@Service("rr-processor-source-capability")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class CapabilityResourceResolutionProcessor(private var applicationContext: ApplicationContext,
                                                 private val bluePrintScriptsService: BluePrintScriptsService,
                                                 private val bluePrintJythonService: BlueprintJythonService) :
        ResourceAssignmentProcessor() {

    override fun getName(): String {
        return "resource-assignment-processor-capability"
    }

    override fun process(resourceAssignment: ResourceAssignment) {

        val resourceDefinition = resourceDictionaries[resourceAssignment.dictionaryName]
                ?: throw BluePrintProcessorException("couldn't get resource definition for ${resourceAssignment.dictionaryName}")

        val resourceSource = resourceDefinition.sources[resourceAssignment.dictionarySource]
                ?: throw BluePrintProcessorException("couldn't get resource definition " +
                        "${resourceAssignment.dictionaryName} source(${resourceAssignment.dictionarySource})")

        val resourceSourceProps = checkNotNull(resourceSource.properties) { "failed to get $resourceSource properties" }
        /**
         * Get the Capability Resource Source Info from Property Definitions.
         */
        val capabilityResourceSourceProperty = JacksonUtils
                .getInstanceFromMap(resourceSourceProps, CapabilityResourceSource::class.java)

        val scriptType = capabilityResourceSourceProperty.scriptType
        val scriptClassReference = capabilityResourceSourceProperty.scriptClassReference

        var componentResourceAssignmentProcessor: ResourceAssignmentProcessor? = null

        when (scriptType) {
            BluePrintConstants.SCRIPT_KOTLIN -> {
                componentResourceAssignmentProcessor = getKotlinResourceAssignmentProcessorInstance(scriptClassReference,
                        capabilityResourceSourceProperty.instanceDependencies)
            }
            BluePrintConstants.SCRIPT_INTERNAL -> {
                // Initialize Capability Resource Assignment Processor
                componentResourceAssignmentProcessor = applicationContext.getBean(scriptClassReference, ResourceAssignmentProcessor::class.java)
            }
            BluePrintConstants.SCRIPT_JYTHON -> {
                val content = getJythonContent(scriptClassReference)
                componentResourceAssignmentProcessor = getJythonResourceAssignmentProcessorInstance(scriptClassReference,
                        content, capabilityResourceSourceProperty.instanceDependencies)
            }
        }

        checkNotNull(componentResourceAssignmentProcessor) { "failed to get capability resource assignment processor($scriptClassReference)" }

        // Assign Current Blueprint runtime and ResourceDictionaries
        componentResourceAssignmentProcessor.raRuntimeService = raRuntimeService
        componentResourceAssignmentProcessor.resourceDictionaries = resourceDictionaries

        // Invoke componentResourceAssignmentProcessor
        componentResourceAssignmentProcessor.apply(resourceAssignment)
    }

    override fun recover(runtimeException: RuntimeException, resourceAssignment: ResourceAssignment) {

        TODO("To Implement")
    }

    private fun getKotlinResourceAssignmentProcessorInstance(scriptClassName: String,
                                                             instanceNames: List<String>? = null): ResourceAssignmentProcessor {
        var scriptPropertyInstances: MutableMap<String, Any>? = null

        if (instanceNames != null && instanceNames.isNotEmpty()) {
            scriptPropertyInstances = hashMapOf()
            instanceNames.forEach {
                scriptPropertyInstances[it] = applicationContext.getBean(it)
                        ?: throw BluePrintProcessorException("couldn't get the dependency instance($it)")
            }
        }

        return getKotlinResourceAssignmentProcessorInstance(scriptClassName, scriptPropertyInstances)

    }

    fun getKotlinResourceAssignmentProcessorInstance(scriptClassName: String,
                                                     scriptPropertyInstances: MutableMap<String, Any>? = null):
            ResourceAssignmentProcessor {

        val resourceAssignmentProcessor = bluePrintScriptsService
                .scriptInstance<ResourceAssignmentProcessor>(raRuntimeService.bluePrintContext(),
                        scriptClassName, false)

        // Add additional Instance
        if (scriptPropertyInstances != null) {
            resourceAssignmentProcessor.scriptPropertyInstances = scriptPropertyInstances
        }

        return resourceAssignmentProcessor
    }

    private fun getJythonContent(instanceName: String): String {
        val absolutePath = raRuntimeService.bluePrintContext().rootPath
                .plus(File.separator)
                .plus(BluePrintConstants.TOSCA_SCRIPTS_JYTHON_DIR)
                .plus(File.separator)
                .plus("$instanceName.py")

        return JacksonUtils.getContent(absolutePath)

    }

    /**
     * getJythonResourceAssignmentProcessorInstance Purpose: prepare the jython
     * executor component as a resource assignment processor
     *
     * @param pythonClassName String
     * @param content String
     * @param dependencyInstances List<String>
     * @return resourceAssignmentProcessor ResourceAssignmentProcessor
     */
    private fun getJythonResourceAssignmentProcessorInstance(pythonClassName: String, content: String,
                                                             dependencyInstances: List<String>?):
            ResourceAssignmentProcessor {
        val jythonContextInstance: MutableMap<String, Any> = hashMapOf()
        jythonContextInstance["log"] = LoggerFactory.getLogger(pythonClassName)
        jythonContextInstance["raRuntimeService"] = raRuntimeService
        dependencyInstances?.forEach { instanceName ->
            jythonContextInstance[instanceName] = applicationContext.getBean(instanceName)
        }

        return getJythonResourceAssignmentProcessorInstance(pythonClassName, content, jythonContextInstance)
    }

    fun getJythonResourceAssignmentProcessorInstance(pythonClassName: String, content: String,
                                                     dependencyInstances: MutableMap<String, Any>):
            ResourceAssignmentProcessor {

        val resourceAssignmentProcessor = bluePrintJythonService
                .jythonInstance<ResourceAssignmentProcessor>(raRuntimeService.bluePrintContext(), pythonClassName,
                        content, dependencyInstances)

        // Add additional Instance
        if (dependencyInstances != null) {
            resourceAssignmentProcessor.scriptPropertyInstances = dependencyInstances
        }

        return resourceAssignmentProcessor
    }
}