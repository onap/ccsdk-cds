/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintProcessorException
import org.onap.ccsdk.cds.controllerblueprints.core.asJsonPrimitive
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintWorkflowEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.onap.ccsdk.cds.controllerblueprints.resource.dict.ResourceAssignment
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintWorkflowEnhancerImpl(
    private val bluePrintRepoService: BlueprintRepoService,
    private val bluePrintTypeEnhancerService: BlueprintTypeEnhancerService,
    private val resourceAssignmentEnhancerService: ResourceAssignmentEnhancerService
) :
    BlueprintWorkflowEnhancer {

    private val log = logger(BlueprintWorkflowEnhancerImpl::class)

    companion object {

        const val ARTIFACT_TYPE_MAPPING_SOURCE: String = "artifact-mapping-resource"
        const val PROPERTY_DEPENDENCY_NODE_TEMPLATES = "dependency-node-templates"
    }

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext

    private val workflowDataTypes: MutableMap<String, DataType> = hashMapOf()

    override fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, workflow: Workflow) {
        log.info("##### Enhancing Workflow($name)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val dynamicPropertyName = "$name-properties"
        if (workflow.inputs == null) {
            workflow.inputs = hashMapOf()
        }
        // Clean Dynamic Property Field, If present
        workflow.inputs?.remove(dynamicPropertyName)

        // Enrich Workflow Inputs
        enhanceWorkflowInputs(name, workflow)

        // Enrich Workflow Outputs
        enhanceWorkflowOutputs(name, workflow)

        // Enrich Only for Resource Assignment and Dynamic Input Properties if any
        enhanceStepTargets(name, workflow)
    }

    open fun enhanceWorkflowInputs(name: String, workflow: Workflow) {

        workflow.inputs?.let { inputs ->
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, inputs)
        }
    }

    open fun enhanceWorkflowOutputs(name: String, workflow: Workflow) {
        workflow.outputs?.let { outputs ->
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, outputs)
        }
    }

    private fun enhanceStepTargets(name: String, workflow: Workflow) {

        // Get the first Step Target NodeTemplate name( It may be Component or DG Node Template)
        val firstNodeTemplateName = bluePrintContext.workflowFirstStepNodeTemplate(name)

        val derivedFrom = bluePrintContext.nodeTemplateNodeType(firstNodeTemplateName).derivedFrom

        when {
            derivedFrom.startsWith(BlueprintConstants.MODEL_TYPE_NODE_COMPONENT, true) -> {
                enhanceStepTargets(name, workflow, firstNodeTemplateName, false)
            }
            derivedFrom.startsWith(BlueprintConstants.MODEL_TYPE_NODE_WORKFLOW, true) -> {
                enhanceStepTargets(name, workflow, firstNodeTemplateName, true)
            }
            else -> {
                throw BlueprintProcessorException(
                    "couldn't execute workflow($name) step mapped " +
                        "to node template($firstNodeTemplateName) derived from($derivedFrom)"
                )
            }
        }
    }

    private fun enhanceStepTargets(name: String, workflow: Workflow, nodeTemplateName: String, isDG: Boolean) {

        val dependencyNodeTemplates: List<String>
        if (isDG) {
            val dgNodeTemplate = bluePrintContext.nodeTemplateByName(nodeTemplateName)

            // Get the Dependent Component Node Template Names
            val dependencyNodeTemplateNodes = dgNodeTemplate.properties?.get(PROPERTY_DEPENDENCY_NODE_TEMPLATES)
                ?: throw BlueprintException("couldn't get property($PROPERTY_DEPENDENCY_NODE_TEMPLATES) ")

            dependencyNodeTemplates =
                JacksonUtils.getListFromJsonNode(dependencyNodeTemplateNodes, String::class.java)
        } else {
            dependencyNodeTemplates = listOf(nodeTemplateName)
        }

        log.info("workflow($name) dependent component NodeTemplates($dependencyNodeTemplates)")

        // Check and Get Resource Assignment File
        val resourceAssignmentArtifacts = dependencyNodeTemplates?.mapNotNull { componentNodeTemplateName ->
            log.info("identified workflow($name) targets($componentNodeTemplateName)")

            val resourceAssignmentArtifacts = bluePrintContext.nodeTemplateByName(componentNodeTemplateName)
                .artifacts?.filter {
                    it.value.type == ARTIFACT_TYPE_MAPPING_SOURCE
                }?.map {
                    log.info("resource assignment artifacts(${it.key}) for NodeType($componentNodeTemplateName)")
                    it.value.file
                }
            resourceAssignmentArtifacts
        }?.flatten()

        log.info("workflow($name) resource assignment files($resourceAssignmentArtifacts")

        if (resourceAssignmentArtifacts != null && resourceAssignmentArtifacts.isNotEmpty()) {

            // Add Workflow Dynamic Property
            addWorkFlowDynamicPropertyDefinitions(name, workflow)

            resourceAssignmentArtifacts.forEach { fileName ->
                // Enhance Resource Assignment File
                val resourceAssignmentProperties = enhanceResourceAssignmentFile(fileName!!)
                // Add Workflow Dynamic DataType
                addWorkFlowDynamicDataType(name, resourceAssignmentProperties)
            }
        }
    }

    // Enhancement for Dynamic Properties, Resource Assignment Properties, Resource Sources
    private fun enhanceResourceAssignmentFile(fileName: String): MutableMap<String, PropertyDefinition> {

        val filePath = "${bluePrintContext.rootPath}/$fileName"

        log.info("enriching artifacts file($filePath")

        val resourceAssignmentProperties: MutableMap<String, PropertyDefinition> = hashMapOf()

        val resourceAssignments: MutableList<ResourceAssignment> = JacksonUtils.getListFromFile(filePath, ResourceAssignment::class.java)
            as? MutableList<ResourceAssignment>
            ?: throw BlueprintProcessorException("couldn't get ResourceAssignment definitions for the file($filePath)")

        val alreadyEnhancedKey = "enhanced-$fileName"
        val alreadyEnhanced = bluePrintRuntimeService.check(alreadyEnhancedKey)

        log.info("enhancing workflow resource mapping file($fileName) already enhanced($alreadyEnhanced)")

        if (!alreadyEnhanced) {
            // Call Resource Assignment Enhancer
            resourceAssignmentEnhancerService.enhanceBlueprint(bluePrintTypeEnhancerService, bluePrintRuntimeService, resourceAssignments)
            bluePrintRuntimeService.put(alreadyEnhancedKey, true.asJsonPrimitive())
        }

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

        var dynamicDataType: DataType? = bluePrintContext.serviceTemplate.dataTypes?.get(dataTypeName)

        if (dynamicDataType == null) {
            log.info("dataType not present for the recipe({})", dataTypeName)
            dynamicDataType = DataType()
            dynamicDataType.version = "1.0.0"
            dynamicDataType.description = "Dynamic DataType definition for workflow($workflowName)."
            dynamicDataType.derivedFrom = BlueprintConstants.MODEL_TYPE_DATA_TYPE_DYNAMIC

            val dataTypeProperties: MutableMap<String, PropertyDefinition> = hashMapOf()
            dynamicDataType.properties = dataTypeProperties

            // Overwrite WorkFlow DataType
            bluePrintContext.serviceTemplate.dataTypes?.put(dataTypeName, dynamicDataType)
        } else {
            log.info("dynamic dataType($dataTypeName) already present for workflow($workflowName).")
        }
        // Merge all the Recipe Properties
        mappingProperties.forEach { (propertyName, propertyDefinition) ->
            dynamicDataType.properties?.put(propertyName, propertyDefinition)
        }
    }
}
