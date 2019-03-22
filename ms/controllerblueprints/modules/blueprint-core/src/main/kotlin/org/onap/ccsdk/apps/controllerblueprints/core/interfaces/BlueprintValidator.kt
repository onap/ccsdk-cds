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

package org.onap.ccsdk.apps.controllerblueprints.core.interfaces

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintRuntimeService


interface BluePrintValidator<T> {

    fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, type: T)

}


interface BluePrintServiceTemplateValidator : BluePrintValidator<ServiceTemplate>

interface BluePrintTopologyTemplateValidator : BluePrintValidator<TopologyTemplate>

interface BluePrintArtifactTypeValidator : BluePrintValidator<ArtifactType>

interface BluePrintArtifactDefinitionValidator : BluePrintValidator<ArtifactDefinition>

interface BluePrintDataTypeValidator : BluePrintValidator<DataType>

interface BluePrintNodeTypeValidator : BluePrintValidator<NodeType>

interface BluePrintNodeTemplateValidator : BluePrintValidator<NodeTemplate>

interface BluePrintWorkflowValidator : BluePrintValidator<Workflow>

interface BluePrintPropertyDefinitionValidator : BluePrintValidator<PropertyDefinition>

interface BluePrintAttributeDefinitionValidator : BluePrintValidator<AttributeDefinition>

/**
 * Blueprint Validation Interface.
 */
interface BluePrintValidatorService {

    @Throws(BluePrintException::class)
    fun validateBluePrints(basePath: String): Boolean

    @Throws(BluePrintException::class)
    fun validateBluePrints(bluePrintRuntimeService: BluePrintRuntimeService<*>): Boolean
}


interface BluePrintTypeValidatorService {

    fun <T : BluePrintValidator<*>> bluePrintValidator(referenceName: String, classType: Class<T>): T?

    fun <T : BluePrintValidator<*>> bluePrintValidators(referenceNamePrefix: String, classType: Class<T>): List<T>?

    fun <T : BluePrintValidator<*>> bluePrintValidators(classType: Class<T>): List<T>?

    fun getServiceTemplateValidators(): List<BluePrintServiceTemplateValidator>

    fun getDataTypeValidators(): List<BluePrintDataTypeValidator>

    fun getArtifactTypeValidators(): List<BluePrintArtifactTypeValidator>

    fun getArtifactDefinitionsValidators(): List<BluePrintArtifactDefinitionValidator>

    fun getNodeTypeValidators(): List<BluePrintNodeTypeValidator>

    fun getTopologyTemplateValidators(): List<BluePrintTopologyTemplateValidator>

    fun getNodeTemplateValidators(): List<BluePrintNodeTemplateValidator>

    fun getWorkflowValidators(): List<BluePrintWorkflowValidator>

    fun getPropertyDefinitionValidators(): List<BluePrintPropertyDefinitionValidator>

    fun getAttributeDefinitionValidators(): List<BluePrintAttributeDefinitionValidator>

    fun validateServiceTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, serviceTemplate: ServiceTemplate) {
        val validators = getServiceTemplateValidators()
        doValidation(bluePrintRuntimeService, name, serviceTemplate, validators)
    }

    fun validateArtifactType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, artifactType: ArtifactType) {
        val validators = getArtifactTypeValidators()
        doValidation(bluePrintRuntimeService, name, artifactType, validators)
    }

    fun validateArtifactDefinition(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String,
                                   artifactDefinition: ArtifactDefinition) {
        val validators = getArtifactDefinitionsValidators()
        doValidation(bluePrintRuntimeService, name, artifactDefinition, validators)
    }

    fun validateDataType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, dataType: DataType) {
        val validators = getDataTypeValidators()
        doValidation(bluePrintRuntimeService, name, dataType, validators)
    }

    fun validateNodeType(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeType: NodeType) {
        val validators = getNodeTypeValidators()
        doValidation(bluePrintRuntimeService, name, nodeType, validators)
    }

    fun validateTopologyTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, topologyTemplate: TopologyTemplate) {
        val validators = getTopologyTemplateValidators()
        doValidation(bluePrintRuntimeService, name, topologyTemplate, validators)
    }

    fun validateNodeTemplate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeTemplate: NodeTemplate) {
        val validators = getNodeTemplateValidators()
        doValidation(bluePrintRuntimeService, name, nodeTemplate, validators)
    }

    fun validateWorkflow(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, workflow: Workflow) {
        val validators = getWorkflowValidators()
        doValidation(bluePrintRuntimeService, name, workflow, validators)
    }

    fun validatePropertyDefinitions(bluePrintRuntimeService: BluePrintRuntimeService<*>, properties: MutableMap<String, PropertyDefinition>) {
        properties.forEach { propertyName, propertyDefinition ->
            validatePropertyDefinition(bluePrintRuntimeService, propertyName, propertyDefinition)
        }
    }

    fun validatePropertyDefinition(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, propertyDefinition: PropertyDefinition) {
        val validators = getPropertyDefinitionValidators()
        doValidation(bluePrintRuntimeService, name, propertyDefinition, validators)
    }

    fun validateAttributeDefinitions(bluePrintRuntimeService: BluePrintRuntimeService<*>, attributes: MutableMap<String, AttributeDefinition>) {
        attributes.forEach { attributeName, attributeDefinition ->
            validateAttributeDefinition(bluePrintRuntimeService, attributeName, attributeDefinition)
        }
    }

    fun validateAttributeDefinition(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, attributeDefinition: AttributeDefinition) {
        val validators = getAttributeDefinitionValidators()
        doValidation(bluePrintRuntimeService, name, attributeDefinition, validators)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> doValidation(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, definition: Any, validators: List<BluePrintValidator<T>>) {
        validators.forEach {
            it.validate(bluePrintRuntimeService, name, definition as T)
        }
    }
}



