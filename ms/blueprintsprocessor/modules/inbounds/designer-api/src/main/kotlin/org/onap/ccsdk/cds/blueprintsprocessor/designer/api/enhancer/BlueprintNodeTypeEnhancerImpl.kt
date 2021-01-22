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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.utils.BlueprintEnhancerUtils
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintException
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintTypes
import org.onap.ccsdk.cds.controllerblueprints.core.data.InterfaceDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.data.NodeType
import org.onap.ccsdk.cds.controllerblueprints.core.data.OperationDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintNodeTypeEnhancer
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintRepoService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintTypeEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintContext
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintRuntimeService
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Service

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
open class BlueprintNodeTypeEnhancerImpl(
    private val bluePrintRepoService: BlueprintRepoService,
    private val bluePrintTypeEnhancerService: BlueprintTypeEnhancerService
) : BlueprintNodeTypeEnhancer {

    private val log = logger(BlueprintNodeTypeEnhancerImpl::class)

    lateinit var bluePrintRuntimeService: BlueprintRuntimeService<*>
    lateinit var bluePrintContext: BlueprintContext

    override fun enhance(bluePrintRuntimeService: BlueprintRuntimeService<*>, name: String, nodeType: NodeType) {
        this.bluePrintRuntimeService = bluePrintRuntimeService
        this.bluePrintContext = bluePrintRuntimeService.bluePrintContext()

        val derivedFrom = nodeType.derivedFrom

        if (!BlueprintTypes.rootNodeTypes().contains(derivedFrom)) {
            val derivedFromNodeType = BlueprintEnhancerUtils.populateNodeType(bluePrintContext, bluePrintRepoService, name)
            // Enrich NodeType
            enhance(bluePrintRuntimeService, derivedFrom, derivedFromNodeType)
        }

        // NodeType Attribute Definitions
        enrichNodeTypeAttributes(name, nodeType)

        // NodeType Property Definitions
        enrichNodeTypeProperties(name, nodeType)

        // NodeType Requirement
        enrichNodeTypeRequirements(name, nodeType)

        // NodeType Capability
        enrichNodeTypeCapabilityProperties(name, nodeType)

        // NodeType Interface
        enrichNodeTypeInterfaces(name, nodeType)
    }

    open fun enrichNodeTypeAttributes(nodeTypeName: String, nodeType: NodeType) {
        nodeType.attributes?.let {
            bluePrintTypeEnhancerService.enhanceAttributeDefinitions(bluePrintRuntimeService, nodeType.attributes!!)
        }
    }

    open fun enrichNodeTypeProperties(nodeTypeName: String, nodeType: NodeType) {
        nodeType.properties?.let {
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, nodeType.properties!!)
        }
    }

    open fun enrichNodeTypeRequirements(nodeTypeName: String, nodeType: NodeType) {

        nodeType.requirements?.forEach { requirementName, requirementDefinition ->
            // Populate Requirement Node
            requirementDefinition.node?.let { requirementNodeTypeName ->
                // Get Requirement NodeType from Repo and Update Service Template
                val requirementNodeType = BlueprintEnhancerUtils.populateNodeType(
                    bluePrintContext,
                    bluePrintRepoService, requirementNodeTypeName
                )
                // Enhance Node Type
                enhance(bluePrintRuntimeService, requirementNodeTypeName, requirementNodeType)

                // Enhance Relationship Type
                val relationShipTypeName = requirementDefinition.relationship
                    ?: throw BlueprintException(
                        "couldn't get relationship name for the NodeType($nodeTypeName) " +
                            "Requirement($requirementName)"
                    )
                enrichRelationShipType(relationShipTypeName)
            }
        }
    }

    open fun enrichNodeTypeCapabilityProperties(nodeTypeName: String, nodeType: NodeType) {
        nodeType.capabilities?.forEach { _, capabilityDefinition ->
            capabilityDefinition.properties?.let { properties ->
                bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, properties)
            }
        }
    }

    open fun enrichNodeTypeInterfaces(nodeTypeName: String, nodeType: NodeType) {
        nodeType.interfaces?.forEach { interfaceName, interfaceObj ->
            // Populate Node type Interface Operation
            log.debug("Enriching NodeType({}) Interface({})", nodeTypeName, interfaceName)
            populateNodeTypeInterfaceOperation(nodeTypeName, interfaceName, interfaceObj)
        }
    }

    open fun populateNodeTypeInterfaceOperation(nodeTypeName: String, interfaceName: String, interfaceObj: InterfaceDefinition) {

        interfaceObj.operations?.forEach { operationName, operation ->
            enrichNodeTypeInterfaceOperationInputs(nodeTypeName, operationName, operation)
            enrichNodeTypeInterfaceOperationOutputs(nodeTypeName, operationName, operation)
        }
    }

    open fun enrichNodeTypeInterfaceOperationInputs(nodeTypeName: String, operationName: String, operation: OperationDefinition) {
        operation.inputs?.let { inputs ->
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, inputs)
        }
    }

    open fun enrichNodeTypeInterfaceOperationOutputs(
        nodeTypeName: String,
        operationName: String,
        operation: OperationDefinition
    ) {
        operation.outputs?.let { inputs ->
            bluePrintTypeEnhancerService.enhancePropertyDefinitions(bluePrintRuntimeService, inputs)
        }
    }

    /**
     * Get the Relationship Type from database and add to Blueprint Context
     */
    open fun enrichRelationShipType(relationshipName: String) {
        BlueprintEnhancerUtils.populateRelationshipType(bluePrintContext, bluePrintRepoService, relationshipName)
    }
}
