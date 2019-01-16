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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.processor

import org.onap.ccsdk.apps.blueprintsprocessor.functions.resource.resolution.CapabilityResourceSource
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.utils.ResourceAssignmentUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service("resource-assignment-processor-capability")
open class CapabilityResourceAssignmentProcessor : ResourceAssignmentProcessor() {

    companion object {
        const val CAPABILITY_TYPE_KOTLIN_COMPONENT = "KOTLIN-COMPONENT"
        const val CAPABILITY_TYPE_JAVA_COMPONENT = "JAVA-COMPONENT"
        const val CAPABILITY_TYPE_JYTHON_COMPONENT = "JYTHON-COMPONENT"
    }

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    override fun getName(): String {
        return "resource-assignment-processor-capability"
    }

    override fun process(executionRequest: ResourceAssignment) {

        val resourceDefinition = resourceDictionaries[executionRequest.dictionaryName]
                ?: throw BluePrintProcessorException("couldn't get resource definition for ${executionRequest.dictionaryName}")

        val resourceSource = resourceDefinition.sources[executionRequest.dictionarySource]
                ?: throw BluePrintProcessorException("couldn't get resource definition ${executionRequest.dictionaryName} source(${executionRequest.dictionarySource})")

        checkNotNull(resourceSource.properties) { "failed to get ${executionRequest.dictionarySource} properties" }

        val capabilityResourceSourceProperty = ResourceAssignmentUtils.transformResourceSource(resourceSource.properties!!, CapabilityResourceSource::class.java)

        val instanceType = capabilityResourceSourceProperty.type
        val instanceName = capabilityResourceSourceProperty.instanceName


        var componentResourceAssignmentProcessor: ResourceAssignmentProcessor? = null

        when (instanceType) {
            CAPABILITY_TYPE_KOTLIN_COMPONENT ->{
                TODO("NO implementation")
            }
            CAPABILITY_TYPE_JAVA_COMPONENT -> {
                // Initialize Capability Resource Assignment Processor
                componentResourceAssignmentProcessor = applicationContext.getBean(instanceName, ResourceAssignmentProcessor::class.java)
            }
            CAPABILITY_TYPE_JYTHON_COMPONENT -> {
                TODO(" No implementation")
            }
        }

        checkNotNull(componentResourceAssignmentProcessor) { "failed to get capability resource assignment processor($instanceName)" }

        // Assign Current Blueprint runtime and ResourceDictionaries
        componentResourceAssignmentProcessor.bluePrintRuntimeService = bluePrintRuntimeService
        componentResourceAssignmentProcessor.resourceDictionaries = resourceDictionaries

        // Invoke componentResourceAssignmentProcessor
        componentResourceAssignmentProcessor.apply(executionRequest)
    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ResourceAssignment) {

        TODO("To Implement")
    }
}