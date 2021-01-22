/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.core.interfaces

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactType
import org.onap.ccsdk.cds.controllerblueprints.core.data.AttributeDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.DataType
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.ServiceTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.TopologyTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService

interface BlueprintValidator<T> {

    fun validate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, type: T)
}

interface BlueprintServiceTemplateValidator : BlueprintValidator<ServiceTemplate>

interface BlueprintTopologyTemplateValidator : BlueprintValidator<TopologyTemplate>

interface BlueprintArtifactTypeValidator : BlueprintValidator<ArtifactType>

interface BlueprintArtifactDefinitionValidator : BlueprintValidator<ArtifactDefinition>

interface BlueprintDataTypeValidator : BlueprintValidator<DataType>

interface BlueprintNodeTypeValidator : BlueprintValidator<NodeType>

interface BlueprintNodeTemplateValidator : BlueprintValidator<NodeTemplate>

interface BlueprintWorkflowValidator : BlueprintValidator<Workflow>

interface BlueprintPropertyDefinitionValidator : BlueprintValidator<PropertyDefinition>

interface BlueprintAttributeDefinitionValidator : BlueprintValidator<AttributeDefinition>

/**
 * Blueprint Validation Interface.
 */
interface BlueprintValidatorService {

    @Throws(BlueprintException::class)
    suspend fun validateBlueprints(basePath: String): Boolean

    @Throws(BlueprintException::class)
    suspend fun validateBlueprints(bluePrintRuntimeService: BlueprintRuntimeService<*>): Boolean
}

interface BlueprintTypeValidatorService {

    fun <T : BlueprintValidator<*>> bluePrintValidator(referenceName: String, classType: Class<T>): T?

    fun <T : BlueprintValidator<*>> bluePrintValidators(referenceNamePrefix: String, classType: Class<T>): List<T>?

    fun <T : BlueprintValidator<*>> bluePrintValidators(classType: Class<T>): List<T>?

    fun getServiceTemplateValidators(): List<BlueprintServiceTemplateValidator>

    fun getDataTypeValidators(): List<BlueprintDataTypeValidator>

    fun getArtifactTypeValidators(): List<BlueprintArtifactTypeValidator>

    fun getArtifactDefinitionsValidators(): List<BlueprintArtifactDefinitionValidator>

    fun getNodeTypeValidators(): List<BlueprintNodeTypeValidator>

    fun getTopologyTemplateValidators(): List<BlueprintTopologyTemplateValidator>

    fun getNodeTemplateValidators(): List<BlueprintNodeTemplateValidator>

    fun getWorkflowValidators(): List<BlueprintWorkflowValidator>

    fun getPropertyDefinitionValidators(): List<BlueprintPropertyDefinitionValidator>

    fun getAttributeDefinitionValidators(): List<BlueprintAttributeDefinitionValidator>

    fun validateServiceTemplate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, serviceTemplate: ServiceTemplate) {
        val validators = getServiceTemplateValidators()
        doValidation(bluePrintRuntimeService, name, serviceTemplate, validators)
    }

    fun validateArtifactType(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, artifactType: ArtifactType) {
        val validators = getArtifactTypeValidators()
        doValidation(bluePrintRuntimeService, name, artifactType, validators)
    }

    fun validateArtifactDefinition(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        artifactDefinition: ArtifactDefinition
    ) {
        val validators = getArtifactDefinitionsValidators()
        doValidation(bluePrintRuntimeService, name, artifactDefinition, validators)
    }

    fun validateDataType(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, dataType: DataType) {
        val validators = getDataTypeValidators()
        doValidation(bluePrintRuntimeService, name, dataType, validators)
    }

    fun validateNodeType(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, nodeType: NodeType) {
        val validators = getNodeTypeValidators()
        doValidation(bluePrintRuntimeService, name, nodeType, validators)
    }

    fun validateTopologyTemplate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, topologyTemplate: TopologyTemplate) {
        val validators = getTopologyTemplateValidators()
        doValidation(bluePrintRuntimeService, name, topologyTemplate, validators)
    }

    fun validateNodeTemplate(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, nodeTemplate: NodeTemplate) {
        val validators = getNodeTemplateValidators()
        doValidation(bluePrintRuntimeService, name, nodeTemplate, validators)
    }

    fun validateWorkflow(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, workflow: Workflow) {
        val validators = getWorkflowValidators()
        doValidation(bluePrintRuntimeService, name, workflow, validators)
    }

    fun validatePropertyDefinitions(bluePrintRuntimeService: BlueprintRuntimeService<*>, properties: MutableMap<String, PropertyDefinition>) {
        properties.forEach { propertyName, propertyDefinition ->
            validatePropertyDefinition(bluePrintRuntimeService, propertyName, propertyDefinition)
        }
    }

    fun validatePropertyDefinition(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, propertyDefinition: PropertyDefinition) {
        val validators = getPropertyDefinitionValidators()
        doValidation(bluePrintRuntimeService, name, propertyDefinition, validators)
    }

    fun validateAttributeDefinitions(bluePrintRuntimeService: BlueprintRuntimeService<*>, attributes: MutableMap<String, AttributeDefinition>) {
        attributes.forEach { attributeName, attributeDefinition ->
            validateAttributeDefinition(bluePrintRuntimeService, attributeName, attributeDefinition)
        }
    }

    fun validateAttributeDefinition(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, attributeDefinition: AttributeDefinition) {
        val validators = getAttributeDefinitionValidators()
        doValidation(bluePrintRuntimeService, name, attributeDefinition, validators)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> doValidation(
        bluePrintRuntimeService: BlueprintRuntimeService<*>,
        name: String,
        definition: Any,
        validators: List<BlueprintValidator<T>>
    ) {
        validators.forEach {
            it.validate(bluePrintRuntimeService, name, definition as T)
        }
    }
}
