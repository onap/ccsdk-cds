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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor

import org.apache.commons.io.FilenameUtils
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.functions.python.executor.utils.PythonExecutorUtils
import org.onap.ccsdk.apps.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.checkNotEmptyNThrow
import org.onap.ccsdk.apps.controllerblueprints.core.data.OperationAssignment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("component-jython-executor")
class ComponentJythonExecutor(private val pythonExecutorProperty: PythonExecutorProperty) : AbstractComponentFunction() {

    private val log = LoggerFactory.getLogger(ComponentJythonExecutor::class.java)

    private var componentFunction: AbstractComponentFunction? = null


    override fun process(executionServiceInput: ExecutionServiceInput) {

        log.info("Processing : ${executionServiceInput.metadata}")
        checkNotNull(bluePrintRuntimeService) { "failed to get bluePrintRuntimeService" }

        val bluePrintContext = bluePrintRuntimeService!!.bluePrintContext()

        val operationAssignment: OperationAssignment = bluePrintContext
                .nodeTemplateInterfaceOperation(nodeTemplateName, interfaceName, operationName)

        val blueprintBasePath: String = bluePrintRuntimeService!!.get(BluePrintConstants.PROPERTY_BLUEPRINT_BASE_PATH)?.asText()
                ?: throw BluePrintProcessorException("python execute path is missing for node template ($nodeTemplateName)")

        val artifactName: String = operationAssignment.implementation?.primary
                ?: throw BluePrintProcessorException("missing primary field to get artifact name for node template ($nodeTemplateName)")

        val artifactDefinition = bluePrintRuntimeService!!.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)

        val pythonFileName = artifactDefinition.file
                ?: throw BluePrintProcessorException("missing file name for node template ($nodeTemplateName)'s artifactName($artifactName)")

        val pythonClassName = FilenameUtils.getBaseName(pythonFileName)

        val content: String? = bluePrintRuntimeService!!.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)

        checkNotEmptyNThrow(content, "artifact ($artifactName) content is empty")

        val pythonPath: MutableList<String> = operationAssignment.implementation?.dependencies ?: arrayListOf()
        pythonPath.add(blueprintBasePath)
        pythonPath.addAll(pythonExecutorProperty.modulePaths)

        val properties: MutableMap<String, Any> = hashMapOf()
        properties["log"] = log

        componentFunction = PythonExecutorUtils.getPythonComponent(pythonExecutorProperty.executionPath,
                pythonPath, content, pythonClassName, properties)

        componentFunction!!.process(executionServiceInput)

    }

    override fun recover(runtimeException: RuntimeException, executionRequest: ExecutionServiceInput) {
        componentFunction!!.recover(runtimeException, executionRequest)
    }

}