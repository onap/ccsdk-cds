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

package org.onap.ccsdk.cds.controllerblueprints.validation

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.checkNotEmpty
import org.onap.ccsdk.cds.controllerblueprints.core.data.CapabilityDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.Implementation
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.RequirementDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTypeValidator
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service("default-node-type-validator")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintNodeTypeValidatorImpl(private val bluePrintTypeValidatorService: BlueprintTypeValidatorService) : BlueprintNodeTypeValidator {

    private val log = LoggerFactory.getLogger(BlueprintServiceTemplateValidatorImpl::class.toString())

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext
    var paths: MutableList<String> = arrayListOf()

    override fun validate(bluePrintRuntimeService: BlueprintRuntimeService<*>, nodeTypeName: String, nodeType: NodeType) {
        log.trace("Validating NodeType($nodeTypeName)")
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        paths.add(nodeTypeName)

        val derivedFrom: String = nodeType.derivedFrom
        // Check Derived From
        checkValidNodeTypesDerivedFrom(nodeTypeName, derivedFrom)

        if (!BlueprintTypes.rootNodeTypes().contains(derivedFrom)) {
            bluePrintContext.serviceTemplate.nodeTypes?.get(derivedFrom)
                ?: throw BlueprintException("Failed to get derivedFrom NodeType($derivedFrom)'s for NodeType($nodeTypeName)")
        }

        nodeType.attributes?.let {
            bluePrintTypeValidatorService.validateAttributeDefinitions(bluePrintRuntimeService, nodeType.attributes!!)
        }

        nodeType.properties?.let {
            bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, nodeType.properties!!)
        }

        nodeType.capabilities?.let { validateCapabilityDefinitions(nodeTypeName, nodeType) }
        nodeType.requirements?.let { validateRequirementDefinitions(nodeTypeName, nodeType) }
        nodeType.interfaces?.let { validateInterfaceDefinitions(nodeType.interfaces!!) }

        paths.removeAt(paths.lastIndex)
    }

    fun checkValidNodeTypesDerivedFrom(nodeTypeName: String, derivedFrom: String) {
        check(BlueprintTypes.validNodeTypeDerivedFroms.contains(derivedFrom)) {
            throw BlueprintException("Failed to get node type ($nodeTypeName)'s  derivedFrom($derivedFrom) definition ")
        }
    }

    open fun validateCapabilityDefinitions(nodeTypeName: String, nodeType: NodeType) {
        val capabilities = nodeType.capabilities
        paths.add("capabilities")
        capabilities?.forEach { capabilityName, capabilityDefinition ->
            paths.add(capabilityName)

            validateCapabilityDefinition(nodeTypeName, nodeType, capabilityName, capabilityDefinition)

            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    open fun validateCapabilityDefinition(
        nodeTypeName: String,
        nodeType: NodeType,
        capabilityName: String,
        capabilityDefinition: CapabilityDefinition
    ) {
        val capabilityType = capabilityDefinition.type
        check(BlueprintTypes.validCapabilityTypes.contains(capabilityType)) {
            throw BlueprintException("failed to get CapabilityType($capabilityType) for NodeType($nodeTypeName)")
        }
    }

    open fun validateRequirementDefinitions(nodeName: String, nodeType: NodeType) {
        paths.add("requirements")
        val requirements = nodeType.requirements

        requirements?.forEach { requirementDefinitionName, requirementDefinition ->
            paths.add(requirementDefinitionName)
            validateRequirementDefinition(nodeName, nodeType, requirementDefinitionName, requirementDefinition)
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    open fun validateRequirementDefinition(
        nodeTypeName: String,
        nodeType: NodeType,
        requirementDefinitionName: String,
        requirementDefinition: RequirementDefinition
    ) {

        log.info("validating NodeType({}) RequirementDefinition ({}) ", nodeTypeName, requirementDefinitionName)
        val requirementNodeTypeName = requirementDefinition.node!!
        val capabilityName = requirementDefinition.capability
        val relationship = requirementDefinition.relationship!!

        check(BlueprintTypes.validRelationShipDerivedFroms.contains(relationship)) {
            throw BlueprintException("failed to get relationship($relationship) for NodeType($nodeTypeName)'s requirement($requirementDefinitionName)")
        }

        val relationShipNodeType = bluePrintContext.serviceTemplate.nodeTypes?.get(requirementNodeTypeName)
            ?: throw BlueprintException("failed to get requirement NodeType($requirementNodeTypeName)'s for requirement($requirementDefinitionName) ")

        relationShipNodeType.capabilities?.get(capabilityName)
            ?: throw BlueprintException(
                "failed to get requirement NodeType($requirementNodeTypeName)'s " +
                    "capability($nodeTypeName) for NodeType ($capabilityName)'s requirement($requirementDefinitionName) "
            )
    }

    open fun validateInterfaceDefinitions(interfaces: MutableMap<String, InterfaceDefinition>) {
        paths.add("interfaces")
        interfaces.forEach { interfaceName, interfaceDefinition ->
            paths.add(interfaceName)
            interfaceDefinition.operations?.let { validateOperationDefinitions(interfaceDefinition.operations!!) }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    open fun validateOperationDefinitions(operations: MutableMap<String, OperationDefinition>) {
        paths.add("operations")
        operations.forEach { opertaionName, operationDefinition ->
            paths.add(opertaionName)
            operationDefinition.implementation?.let { validateImplementation(operationDefinition.implementation!!) }

            operationDefinition.inputs?.let {
                bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, operationDefinition.inputs!!)
            }

            operationDefinition.outputs?.let {
                bluePrintTypeValidatorService.validatePropertyDefinitions(bluePrintRuntimeService, operationDefinition.outputs!!)
            }
            paths.removeAt(paths.lastIndex)
        }
        paths.removeAt(paths.lastIndex)
    }

    open fun validateImplementation(implementation: Implementation) {
        checkNotEmpty(implementation.primary) { "couldn't get implementation" }
    }
}
