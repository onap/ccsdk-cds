/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.services.workflow

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceOutput
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.StepData
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.putJsonElement
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Service
open class NodeTemplateExecutionService(private val applicationContext: ApplicationContext) {

    private val log = LoggerFactory.getLogger(NodeTemplateExecutionService::class.java)!!

    suspend fun executeNodeTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, nodeTemplateName: String,
                                    executionServiceInput: ExecutionServiceInput): ExecutionServiceOutput {
        // Get the Blueprint Context
        val blueprintContext = bluePrintRuntimeService.bluePrintContext()
        // Get the Component Name, NodeTemplate type is mapped to Component Name
        val componentName = blueprintContext.nodeTemplateByName(nodeTemplateName).type

        val interfaceName = blueprintContext.nodeTemplateFirstInterfaceName(nodeTemplateName)

        val operationName = blueprintContext.nodeTemplateFirstInterfaceFirstOperationName(nodeTemplateName)

        log.info("executing node template($nodeTemplateName) component($componentName) " +
                "interface($interfaceName) operation($operationName)")

        // Get the Component Instance
        val plugin = applicationContext.getBean(componentName, AbstractComponentFunction::class.java)
        // Set the Blueprint Service
        plugin.bluePrintRuntimeService = bluePrintRuntimeService
        plugin.stepName = nodeTemplateName

        // Parent request shouldn't tamper, so need to clone the request and send to the actual component.
        val clonedExecutionServiceInput = ExecutionServiceInput().apply {
            commonHeader = executionServiceInput.commonHeader
            actionIdentifiers = executionServiceInput.actionIdentifiers
            payload = executionServiceInput.payload
        }

        // Populate Step Meta Data
        val stepInputs: MutableMap<String, JsonNode> = hashMapOf()
        stepInputs.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_NODE_TEMPLATE, nodeTemplateName)
        stepInputs.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_INTERFACE, interfaceName)
        stepInputs.putJsonElement(BluePrintConstants.PROPERTY_CURRENT_OPERATION, operationName)
        val stepInputData = StepData().apply {
            name = nodeTemplateName
            properties = stepInputs
        }
        clonedExecutionServiceInput.stepData = stepInputData

        // Get the Request from the Context and Set to the Function Input and Invoke the function
        return plugin.applyNB(clonedExecutionServiceInput)
    }

}