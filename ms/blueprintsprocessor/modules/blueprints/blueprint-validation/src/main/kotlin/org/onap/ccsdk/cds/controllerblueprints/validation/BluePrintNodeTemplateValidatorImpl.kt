/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018-2019 IBM.
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

package org.onap.ccsdk.cds.controllerblueprints.validation

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.ArtifactDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeTemplate
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementAssignment
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintNodeTemplateValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.onap.ccsdk.cds.controllerblueprints.validation.utils.PropertyAssignmentValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-node-template-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BluePrintNodeTemplateValidatorImpl(private val bluePrintTypeValidatorService: BluePrintTypeValidatorService) :
    BluePrintNodeTemplateValidator {

    private val log = LoggerFactory.getLogger(BluePrintNodeTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BluePrintRuntimeService<*>
    lateinit var bluePrintContext: BluePrintContext
    lateinit var propertyAssignmentValidationUtils: PropertyAssignmentValidationUtils
    var paths: MutableList<String> = arrayListOf()

    override fun validate(bluePrintRuntimeService: BluePrintRuntimeService<*>, name: String, nodeTemplate: NodeTemplate) {
        log.debug("Validating NodeTemplate($name)")

        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        propertyAssignmentValidationUtils = PropertyAssignmentValidationUtils(bluePrintContext)

        paths.add(name)

        val type: String = nodeTemplate.type

        val nodeType: NodeType = bluePrintContext.serviceTemplate.nodeTypes?.get(type)
            ?: throw BluePrintException("Failed to get NodeType($type) definition for NodeTemplate($name)")

        nodeTemplate.properties?.let {
            propertyAssignmentValidationUtils
                .validatePropertyAssignments(nodeType.properties!!, nodeTemplate.properties!!)
        }
        nodeTemplate.capabilities?.let { validateCapabilityAssignments(nodeType, name, nodeTemplate) }
        nodeTemplate.requirements?.let { validateRequirementAssignments(nodeType, name, nodeTemplate) }
        nodeTemplate.interfaces?.let { validateInterfaceAssignments(nodeType, name, nodeTemplate) }
        nodeTemplate.artifacts?.let { validateArtifactDefinitions(nodeTemplate.artifacts!!) }

        // Perform Extension Validation
        validateExtension("$type-node-template-validator", name, nodeTemplate)

        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateArtifactDefinitions(artifacts: MutableMap<String, ArtifactDefinition>) {
        paths.add("artifacts")
        artifacts.forEach { artifactDefinitionName, artifactDefinition ->
            bluePrintTypeValidatorService.validateArtifactDefinition(
                bluePrintRuntimeService,
                artifactDefinitionName, artifactDefinition
            )
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateCapabilityAssignments(nodeType: NodeType, nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        val capabilities = nodeTemplate.capabilities
        paths.add("capabilities")
        capabilities?.forEach { capabilityName, capabilityAssignment ->
            paths.add(capabilityName)

            val capabilityDefinition = nodeType.capabilities?.get(capabilityName)
                ?: throw BluePrintException(
                    "Failed to get NodeTemplate($nodeTemplateName) capability definition ($capabilityName) " +
                        "from NodeType(${nodeTemplate.type})"
                )

            validateCapabilityAssignment(nodeTemplateName, capabilityName, capabilityDefinition, capabilityAssignment)

            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateCapabilityAssignment(
        nodeTemplateName: String,
        capabilityName: String,
        capabilityDefinition: CapabilityDefinition,
        capabilityAssignment: CapabilityAssignment
    ) {

        capabilityAssignment.properties?.let {
            propertyAssignmentValidationUtils
                .validatePropertyAssignments(capabilityDefinition.properties!!, capabilityAssignment.properties!!)
        }
    }

    @Throws(BluePrintException::class)
    open fun validateRequirementAssignments(nodeType: NodeType, nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        val requirements = nodeTemplate.requirements
        paths.add("requirements")
        requirements?.forEach { requirementName, requirementAssignment ->
            paths.add(requirementName)
            val requirementDefinition = nodeType.requirements?.get(requirementName)
                ?: throw BluePrintException(
                    "Failed to get NodeTemplate($nodeTemplateName) requirement definition ($requirementName) from" +
                        " NodeType(${nodeTemplate.type})"
                )
            // Validate Requirement Assignment
            validateRequirementAssignment(nodeTemplateName, requirementName, requirementDefinition, requirementAssignment)
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateRequirementAssignment(
        nodeTemplateName: String,
        requirementAssignmentName: String,
        requirementDefinition: RequirementDefinition,
        requirementAssignment: RequirementAssignment
    ) {
        log.debug(
            "Validating NodeTemplate({}) requirement assignment ({}) ", nodeTemplateName,
            requirementAssignmentName
        )
        val requirementNodeTemplateName = requirementAssignment.node!!
        val capabilityName = requirementAssignment.capability
        val relationship = requirementAssignment.relationship!!

        check(BluePrintTypes.validRelationShipDerivedFroms.contains(relationship)) {
            throw BluePrintException("Failed to get relationship type ($relationship) for NodeTemplate($nodeTemplateName)'s requirement($requirementAssignmentName)")
        }

        val relationShipNodeTemplate = bluePrintContext.serviceTemplate.topologyTemplate?.nodeTemplates?.get(requirementNodeTemplateName)
            ?: throw BluePrintException(
                "Failed to get requirement NodeTemplate($requirementNodeTemplateName)'s " +
                    "for NodeTemplate($nodeTemplateName) requirement($requirementAssignmentName)"
            )

        relationShipNodeTemplate.capabilities?.get(capabilityName)
            ?: throw BluePrintException(
                "Failed to get requirement NodeTemplate($requirementNodeTemplateName)'s " +
                    "capability($capabilityName) for NodeTemplate ($nodeTemplateName)'s requirement($requirementAssignmentName)"
            )
    }

    @Throws(BluePrintException::class)
    open fun validateInterfaceAssignments(nodeType: NodeType, nodeTemplateName: String, nodeTemplate: NodeTemplate) {

        val interfaces = nodeTemplate.interfaces
        paths.add("interfaces")
        interfaces?.forEach { interfaceAssignmentName, interfaceAssignment ->
            paths.add(interfaceAssignmentName)
            val interfaceDefinition = nodeType.interfaces?.get(interfaceAssignmentName)
                ?: throw BluePrintException(
                    "Failed to get NodeTemplate($nodeTemplateName) interface definition ($interfaceAssignmentName) from" +
                        " NodeType(${nodeTemplate.type})"
                )

            validateInterfaceAssignment(
                nodeTemplateName, interfaceAssignmentName, interfaceDefinition,
                interfaceAssignment
            )
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    @Throws(BluePrintException::class)
    open fun validateInterfaceAssignment(
        nodeTemplateName: String,
        interfaceAssignmentName: String,
        interfaceDefinition: InterfaceDefinition,
        interfaceAssignment: InterfaceAssignment
    ) {

        val operations = interfaceAssignment.operations
        operations?.let {
            validateInterfaceOperationsAssignment(
                nodeTemplateName, interfaceAssignmentName, interfaceDefinition,
                interfaceAssignment
            )
        }
    }

    @Throws(BluePrintException::class)
    open fun validateInterfaceOperationsAssignment(
        nodeTemplateName: String,
        interfaceAssignmentName: String,
        interfaceDefinition: InterfaceDefinition,
        interfaceAssignment: InterfaceAssignment
    ) {

        val operations = interfaceAssignment.operations
        operations?.let {
            it.forEach { operationAssignmentName, operationAssignments ->

                val operationDefinition = interfaceDefinition.operations?.get(operationAssignmentName)
                    ?: throw BluePrintException("Failed to get NodeTemplate($nodeTemplateName) operation definition ($operationAssignmentName)")

                log.debug(
                    "Validation NodeTemplate($nodeTemplateName) Interface($interfaceAssignmentName) Operation " +
                        "($operationAssignmentName)"
                )

                val inputs = operationAssignments.inputs
                val outputs = operationAssignments.outputs

                inputs?.forEach { propertyName, propertyAssignment ->
                    val propertyDefinition = operationDefinition.inputs?.get(propertyName)
                        ?: throw BluePrintException(
                            "Failed to get NodeTemplate($nodeTemplateName) operation " +
                                "definition ($operationAssignmentName) property definition($propertyName)"
                        )
                    // Check the property values with property definition
                    propertyAssignmentValidationUtils
                        .validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)
                }

                outputs?.forEach { propertyName, propertyAssignment ->
                    val propertyDefinition = operationDefinition.outputs?.get(propertyName)
                        ?: throw BluePrintException(
                            "Failed to get NodeTemplate($nodeTemplateName) operation definition ($operationAssignmentName) " +
                                "output property definition($propertyName)"
                        )
                    // Check the property values with property definition
                    propertyAssignmentValidationUtils
                        .validatePropertyAssignment(propertyName, propertyDefinition, propertyAssignment)
                }
            }
        }
    }

    private fun validateExtension(referencePrefix: String, name: String, nodeTemplate: NodeTemplate) {
        val customValidator = bluePrintTypeValidatorService
            .bluePrintValidator(referencePrefix, BluePrintNodeTemplateValidator::class.java)

        customValidator?.let {
            it.validate(bluePrintRuntimeService, name, nodeTemplate)
        }
    }
}
