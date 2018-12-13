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

package org.onap.ccsdk.apps.controllerblueprints.service.enhancer

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant
import org.onap.ccsdk.apps.controllerblueprints.core.data.DataType
import org.onap.ccsdk.apps.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.apps.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintRepoService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintTypeEnhancerService
import org.onap.ccsdk.apps.controllerblueprints.core.interfaces.BluePrintWorkflowEnhancer
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintWorkflowEnhancerImpl(private val bluePrintRepoService: BluePrintRepoService,
                                         private val bluePrintTypeEnhancerService: BluePrintTypeEnhancerService,
                                         private val resourceAssignmentEnhancerService: ResourceAssignmentEnhancerService)
    : BluePrintWorkflowEnhancer {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(BluePrintNodeTemplateEnhancerImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext

    val PROPERTY_DEPENDENCY_NODE_TEMPLATES = "dependency-node-templates"


    private val workflowDataTypes: MutableMap<String, DataType> = hashMapOf()

    override fun enhance(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, workflow: Workflow) {
        log.info("Enhancing Workflow($name)")
       this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val dynamicPropertyName = "$name-properties"
        if (workflow.inputs == null) {
            workflow.inputs = hashMapOf()
        }
        // Clean Dynamic Property Field, If present
        workflow.inputs?.remove(dynamicPropertyName)

        // Enrich Only for Resource Assignment and Dynamic Input Properties if any
        enhanceStepTargets(name, workflow)

        // Enrich Workflow Inputs
        enhanceWorkflowInputs(name, workflow)
    }

    open fun enhanceWorkflowInputs(name: String, workflow: Workflow) {

        workflow.inputs?.let { inputs ->
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, inputs)
        }
    }

    private fun enhanceStepTargets(name: String, workflow: Workflow) {

        // Get the first Step Target NodeTemplate name( Since that is the DG Node Template)
        val dgNodeTemplateName = bluePrintContext.workflowFirstStepNodeTemplate(name)

        val dgNodeTemplate = bluePrintContext.nodeTemplateByName(dgNodeTemplateName)

        // Get the Dependent Component Node Template Names
        val dependencyNodeTemplateNodes = dgNodeTemplate.properties?.get(PROPERTY_DEPENDENCY_NODE_TEMPLATES)
                ?: throw BluePrintException("couldn't get property($PROPERTY_DEPENDENCY_NODE_TEMPLATES) ")

        val dependencyNodeTemplates = JacksonUtils.getListFromJsonNode(dependencyNodeTemplateNodes, String::class.java)

        log.info("workflow($name) dependent component NodeTemplates($dependencyNodeTemplates)")

        // Check and Get Resource Assignment File
        val resourceAssignmentArtifacts = dependencyNodeTemplates?.mapNotNull { componentNodeTemplateName ->
            log.info("Identified workflow($name) targets($componentNodeTemplateName")
            val resourceAssignmentArtifacts = bluePrintContext.nodeTemplateByName(componentNodeTemplateName)
                    .artifacts?.filter {
                it.value.type == "artifact-mapping-resource"
            }?.map {
                log.info("resource assignment artifacts(${it.key}) for NodeType(${componentNodeTemplateName})")
                it.value.file
            }
            resourceAssignmentArtifacts
        }?.flatten()

        log.info("Workflow($name) resource assignment files($resourceAssignmentArtifacts")

        if (resourceAssignmentArtifacts != null && resourceAssignmentArtifacts.isNotEmpty()) {

            // Add Workflow Dynamic Property
            addWorkFlowDynamicPropertyDefinitions(name, workflow)

            resourceAssignmentArtifacts.forEach { fileName ->

                val absoluteFilePath = "${bluePrintContext.rootPath}/$fileName"

                log.info("enriching workflow($name) artifacts file(${absoluteFilePath}")
                // Enhance Resource Assignment File
                val resourceAssignmentProperties = enhanceResourceAssignmentFile(absoluteFilePath)
                // Add Workflow Dynamic DataType
                addWorkFlowDynamicDataType(name, resourceAssignmentProperties)
            }
        }
    }

    private fun enhanceResourceAssignmentFile(filePath: String): MutableMap<String, PropertyDefinition> {

        val resourceAssignmentProperties: MutableMap<String, PropertyDefinition> = hashMapOf()

        val resourceAssignments: MutableList<ResourceAssignment> = JacksonUtils.getListFromFile(filePath, ResourceAssignment::class.java)
                as? MutableList<ResourceAssignment>
                ?: throw BluePrintProcessorException("couldn't get ResourceAssignment definitions for the file($filePath)")

        // Call Resource Assignment Enhancer
        resourceAssignmentEnhancerService.enhanceBluePrint(bluePrintTypeEnhancerService, bluePrintRuntimeService, resourceAssignments)

        resourceAssignments.forEach { resourceAssignment ->
            resourceAssignmentProperties[resourceAssignment.name] = resourceAssignment.property!!
        }
        return resourceAssignmentProperties
    }

    private fun addWorkFlowDynamicPropertyDefinitions(name: String, workflow: Workflow) {
        val dynamicPropertyName = "$name-properties"
        val propertyDefinition = PropertyDefinition()
        propertyDefinition.description = "Dynamic PropertyDefinition for workflow($name)."
        propertyDefinition.type = "dt-$dynamicPropertyName"
        propertyDefinition.required = true
        // Add to Workflow Inputs
        workflow.inputs?.put(dynamicPropertyName, propertyDefinition)
    }

    private fun addWorkFlowDynamicDataType(workflowName: String, mappingProperties: MutableMap<String, PropertyDefinition>) {

        val dataTypeName = "dt-$workflowName-properties"

        var recipeDataType: DataType? = bluePrintContext.serviceTemplate.dataTypes?.get(dataTypeName)

        if (recipeDataType == null) {
            log.info("DataType not present for the recipe({})", dataTypeName)
            recipeDataType = DataType()
            recipeDataType.version = "1.0.0"
            recipeDataType.description = "Dynamic DataType definition for workflow($workflowName)."
            recipeDataType.derivedFrom = ConfigModelConstant.MODEL_TYPE_DATA_TYPE_DYNAMIC

            val dataTypeProperties: MutableMap<String, PropertyDefinition> = hashMapOf()
            recipeDataType.properties = dataTypeProperties

            // Overwrite WorkFlow DataType
            bluePrintContext.serviceTemplate.dataTypes?.put(dataTypeName, recipeDataType)

        } else {
            log.info("Dynamic dataType($dataTypeName) already present for workflow($workflowName).")
        }
        // Merge all the Recipe Properties
        mappingProperties.forEach { propertyName, propertyDefinition ->
            recipeDataType.properties?.put(propertyName, propertyDefinition)
        }
    }
}