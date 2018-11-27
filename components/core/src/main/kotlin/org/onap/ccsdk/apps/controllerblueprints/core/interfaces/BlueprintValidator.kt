package org.onap.ccsdk.apps.controllerblueprints.core.interfaces

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintValidationError
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.service.BluePrintContext


interface BluePrintValidator<T> {

    fun validate(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, type: T)

}


interface BluePrintServiceTemplateValidator : BluePrintValidator<ServiceTemplate>

interface BluePrintTopologyTemplateValidator : BluePrintValidator<TopologyTemplate>

interface BluePrintArtifactTypeValidator : BluePrintValidator<ArtifactType>

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
    fun validateBluePrints(bluePrintContext: BluePrintContext, properties: MutableMap<String, Any>) : Boolean
}


interface BluePrintTypeValidatorService {

    fun getServiceTemplateValidators(): List<BluePrintServiceTemplateValidator>

    fun getDataTypeValidators(): List<BluePrintDataTypeValidator>

    fun getArtifactTypeValidators(): List<BluePrintArtifactTypeValidator>

    fun getNodeTypeValidators(): List<BluePrintNodeTypeValidator>

    fun getTopologyTemplateValidators(): List<BluePrintTopologyTemplateValidator>

    fun getNodeTemplateValidators(): List<BluePrintNodeTemplateValidator>

    fun getWorkflowValidators(): List<BluePrintWorkflowValidator>

    fun getPropertyDefinitionValidators(): List<BluePrintPropertyDefinitionValidator>

    fun getAttributeDefinitionValidators(): List<BluePrintAttributeDefinitionValidator>

    fun validateServiceTemplate(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, serviceTemplate: ServiceTemplate) {
        val validators = getServiceTemplateValidators()
        doValidation(bluePrintContext, error, name, serviceTemplate, validators)
    }

    fun validateArtifactType(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, artifactType: ArtifactType) {
        val validators = getArtifactTypeValidators()
        doValidation(bluePrintContext, error, name, artifactType, validators)
    }

    fun validateDataType(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, dataType: DataType) {
        val validators = getDataTypeValidators()
        doValidation(bluePrintContext, error, name, dataType, validators)
    }

    fun validateNodeType(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, nodeType: NodeType) {
        val validators = getNodeTypeValidators()
        doValidation(bluePrintContext, error, name, nodeType, validators)
    }

    fun validateTopologyTemplate(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, topologyTemplate: TopologyTemplate) {
        val validators = getTopologyTemplateValidators()
        doValidation(bluePrintContext, error, name, topologyTemplate, validators)
    }

    fun validateNodeTemplate(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, nodeTemplate: NodeTemplate) {
        val validators = getNodeTemplateValidators()
        doValidation(bluePrintContext, error, name, nodeTemplate, validators)
    }

    fun validateWorkflow(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, workflow: Workflow) {
        val validators = getWorkflowValidators()
        doValidation(bluePrintContext, error, name, workflow, validators)
    }

    fun validatePropertyDefinitions(bluePrintContext: BluePrintContext, error: BluePrintValidationError, properties: MutableMap<String, PropertyDefinition>) {
        properties.forEach { propertyName, propertyDefinition ->
            validatePropertyDefinition(bluePrintContext, error, propertyName, propertyDefinition)
        }
    }

    fun validatePropertyDefinition(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, propertyDefinition: PropertyDefinition) {
        val validators = getPropertyDefinitionValidators()
        doValidation(bluePrintContext, error, name, propertyDefinition, validators)
    }

    fun validateAttributeDefinitions(bluePrintContext: BluePrintContext, error: BluePrintValidationError, attributes: MutableMap<String, AttributeDefinition>) {
        attributes.forEach { attributeName, attributeDefinition ->
            validateAttributeDefinition(bluePrintContext, error, attributeName, attributeDefinition)
        }
    }

    fun validateAttributeDefinition(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, attributeDefinition: AttributeDefinition) {
        val validators = getAttributeDefinitionValidators()
        doValidation(bluePrintContext, error, name, attributeDefinition, validators)
    }

    private fun <T> doValidation(bluePrintContext: BluePrintContext, error: BluePrintValidationError, name: String, definition: Any, validators: List<BluePrintValidator<T>>) {
        validators.forEach {
            it.validate(bluePrintContext, error, name, definition as T)
        }
    }
}



