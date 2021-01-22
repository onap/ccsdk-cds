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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor

import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.commons.io.FilenameUtils
import org.onap.ccsdk.cds.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts.BlueprintJythonServiceImpl
import org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts.PythonExecutorConstants
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component("component-jython-executor")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class ComponentJythonExecutor(
    private var applicationContext: ApplicationContext,
    private val blueprintJythonService: BlueprintJythonServiceImpl
) : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentJythonExecutor::class.java)

    private lateinit var componentFunction: JythonComponentFunction

    override suspend fun processNB(executionRequest: ExecutionServiceInput) {

        log.info("Processing : $operationInputs")
        // Populate Component Instance
        populateJythonComponentInstance()

        // Invoke Jython Component Script
        componentFunction.executeScript(executionServiceInput)
    }

    override suspend fun recoverNB(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        bluePrintRuntimeService.getBlueprintError()
            .addError("Failed in ComponentJythonExecutor : ${runtimeException.message}")
    }

    private suspend fun populateJythonComponentInstance() {
        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val operationAssignment: OperationAssignment = bluePrintContext
            .nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName)

        val artifactName: String = operationAssignment.implementation?.primary
            ?: throw BlueprintProcessorException("missing primary field to get artifact name for node template ($nodeTemplateName)")

        val artifactDefinition = bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)

        val pythonFileName = artifactDefinition.file
            ?: throw BlueprintProcessorException("missing file name for node template ($nodeTemplateName)'s artifactName($artifactName)")

        val pythonClassName = FilenameUtils.getBaseName(pythonFileName)

        val content: String? = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)

        checkNotEmpty(content) { "artifact ($artifactName) content is empty" }

        val instanceDependenciesNode: ArrayNode = operationInputs[PythonExecutorConstants.INPUT_INSTANCE_DEPENDENCIES] as? ArrayNode
            ?: throw BlueprintProcessorException("Failed to get property(${PythonExecutorConstants.INPUT_INSTANCE_DEPENDENCIES})")

        val jythonInstance: MutableMap<String, Any> = hashMapOf()
        jythonInstance["log"] = LoggerFactory.getLogger(pythonClassName)
        jythonInstance["bluePrintRuntimeService"] = bluePrintRuntimeService

        instanceDependenciesNode.forEach { instanceName ->
            jythonInstance[instanceName.textValue()] = applicationContext.getBean(instanceName.textValue())
        }

        // Setup componentFunction
        componentFunction = blueprintJythonService.jythonInstance(bluePrintContext, pythonClassName, content!!, jythonInstance)
        componentFunction.bluePrintRuntimeService = bluePrintRuntimeService
        componentFunction.executionServiceInput = executionServiceInput
        componentFunction.stepName = stepName
        componentFunction.interfaceName = interfaceName
        componentFunction.operationName = operationName
        componentFunction.processId = processId
        componentFunction.workflowName = workflowName
        componentFunction.scriptType = BlueprintConstants.SCRIPT_JYTHON
    }
}
