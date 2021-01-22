/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.python.executor.scripts

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import org.apache.commons.io.FilenameUtils
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.AbstractComponentFunction
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.scripts.BlueprintJythonService
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintFunctionNode
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import java.io.File

@Deprecated("CDS won't support JythonService")
@Service("blueprintJythonService")
class BlueprintJythonServiceImpl(
    val pythonExecutorProperty: PythonExecutorProperty,
    private val applicationContext: ApplicationContext
) : BlueprintJythonService {

    val log: Logger = LoggerFactory.getLogger(BlueprintJythonService::class.java)

    inline fun <reified T> jythonInstance(
        blueprintContext: BlueprintContext,
        pythonClassName: String,
        content: String,
        dependencyInstanceNames: MutableMap<String, Any>?
    ): T {

        val blueprintBasePath: String = blueprintContext.rootPath
        val pythonPath: MutableList<String> = arrayListOf()
        pythonPath.add(blueprintBasePath)
        pythonPath.addAll(pythonExecutorProperty.modulePaths)

        val blueprintPythonConfigurations =
            BlueprintPython(pythonExecutorProperty.executionPath, pythonPath, arrayListOf())

        val blueprintPythonHost = BlueprintPythonHost(blueprintPythonConfigurations)
        val pyObject = blueprintPythonHost.getPythonComponent(content, pythonClassName, dependencyInstanceNames)

        log.info("Component Object {}", pyObject)

        return pyObject.__tojava__(T::class.java) as T
    }

    override fun jythonComponentInstance(bluePrintContext: BlueprintContext, scriptClassReference: String):
        BlueprintFunctionNode<*, *> {

            val pythonFileName = bluePrintContext.rootPath
                .plus(File.separator)
                .plus(scriptClassReference)

            val pythonClassName = FilenameUtils.getBaseName(pythonFileName)
            log.info("Getting Jython Script Class($pythonClassName)")

            val content: String = JacksonUtils.getContent(pythonFileName)

            val jythonInstances: MutableMap<String, Any> = hashMapOf()
            jythonInstances["log"] = LoggerFactory.getLogger(pythonClassName)

            return jythonInstance<BlueprintFunctionNode<*, *>>(
                bluePrintContext, pythonClassName,
                content, jythonInstances
            )
        }

    suspend fun jythonComponentInstance(abstractComponentFunction: AbstractComponentFunction): AbstractComponentFunction {

        val bluePrintRuntimeService = abstractComponentFunction.bluePrintRuntimeService
        val bluePrintContext = bluePrintRuntimeService.bluePrintContext()
        val nodeTemplateName: String = abstractComponentFunction.nodeTemplateName
        val operationInputs: MutableMap<String, JsonNode> = abstractComponentFunction.operationInputs

        val operationAssignment: OperationAssignment = bluePrintContext
            .nodeTemplateInterfaceOperation(
                abstractComponentFunction.nodeTemplateName,
                abstractComponentFunction.interfaceName, abstractComponentFunction.operationName
            )

        val blueprintBasePath: String = bluePrintContext.rootPath

        val artifactName: String = operationAssignment.implementation?.primary
            ?: throw BlueprintProcessorException("missing primary field to get artifact name for node template ($nodeTemplateName)")

        val artifactDefinition =
            bluePrintRuntimeService.resolveNodeTemplateArtifactDefinition(nodeTemplateName, artifactName)

        val pythonFileName = artifactDefinition.file
            ?: throw BlueprintProcessorException("missing file name for node template ($nodeTemplateName)'s artifactName($artifactName)")

        val pythonClassName = FilenameUtils.getBaseName(pythonFileName)
        log.info("Getting Jython Script Class($pythonClassName)")

        val content: String? = bluePrintRuntimeService.resolveNodeTemplateArtifact(nodeTemplateName, artifactName)

        checkNotEmpty(content) { "artifact ($artifactName) content is empty" }

        val pythonPath: MutableList<String> = operationAssignment.implementation?.dependencies ?: arrayListOf()
        pythonPath.add(blueprintBasePath)
        pythonPath.addAll(pythonExecutorProperty.modulePaths)

        val jythonInstances: MutableMap<String, Any> = hashMapOf()
        jythonInstances["log"] = LoggerFactory.getLogger(nodeTemplateName)

        val instanceDependenciesNode: ArrayNode =
            operationInputs[PythonExecutorConstants.INPUT_INSTANCE_DEPENDENCIES] as? ArrayNode
                ?: throw BlueprintProcessorException("Failed to get property(${PythonExecutorConstants.INPUT_INSTANCE_DEPENDENCIES})")

        instanceDependenciesNode.forEach { instanceName ->
            jythonInstances[instanceName.textValue()] = applicationContext.getBean(instanceName.textValue())
        }

        val scriptComponentFunction = jythonInstance<AbstractComponentFunction>(
            bluePrintContext, pythonClassName,
            content!!, jythonInstances
        )

        return scriptComponentFunction
    }
}
