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

package org.onap.ccsdk.apps.controllerblueprints.core.service

import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintTypes
import org.onap.ccsdk.apps.controllerblueprints.core.data.*
import org.onap.ccsdk.apps.controllerblueprints.core.format
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.Serializable

/**
 * BluePrintEnhancerService
 * @author Brinda Santh
 *
 */
interface BluePrintEnhancerService : Serializable {

    @Throws(BluePrintException::class)
    fun enhance(content: String): ServiceTemplate

    @Throws(BluePrintException::class)
    fun enhance(serviceTemplate: ServiceTemplate): ServiceTemplate

    /**
     * Read Blueprint from CSAR structure Directory
     */
    @Throws(BluePrintException::class)
    fun enhance(fileName: String, basePath: String): ServiceTemplate
}

open class BluePrintEnhancerDefaultService(val bluePrintEnhancerRepoService: BluePrintEnhancerRepoService) : BluePrintEnhancerService {

    private val log: Logger = LoggerFactory.getLogger(BluePrintEnhancerDefaultService::class.java)

    lateinit var serviceTemplate: ServiceTemplate

    override fun enhance(content: String): ServiceTemplate {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enhance(fileName: String, basePath: String): ServiceTemplate {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun enhance(serviceTemplate: ServiceTemplate): ServiceTemplate {
        this.serviceTemplate = serviceTemplate
        initialCleanUp()
        enrichTopologyTemplate(serviceTemplate)

       // log.info("Enriched Blueprint :\n {}", JacksonUtils.getJson(serviceTemplate, true))
        return this.serviceTemplate
    }

    open fun initialCleanUp() {
        serviceTemplate.artifactTypes?.clear()
        serviceTemplate.nodeTypes?.clear()
        serviceTemplate.dataTypes?.clear()

        serviceTemplate.artifactTypes = HashMap()
        serviceTemplate.nodeTypes = HashMap()
        serviceTemplate.dataTypes = HashMap()

    }

    open fun enrichTopologyTemplate(serviceTemplate: ServiceTemplate) {
        serviceTemplate.topologyTemplate?.let { topologyTemplate ->
            enrichTopologyTemplateInputs(topologyTemplate)
            enrichTopologyTemplateNodeTemplates(topologyTemplate)
        }
    }

    open fun enrichTopologyTemplateInputs(topologyTemplate: TopologyTemplate) {
        topologyTemplate.inputs?.let { inputs ->
            enrichPropertyDefinitions(inputs)
        }
    }

    open fun enrichTopologyTemplateNodeTemplates(topologyTemplate: TopologyTemplate) {
        topologyTemplate.nodeTemplates?.forEach { nodeTemplateName, nodeTemplate ->
            enrichNodeTemplate(nodeTemplateName, nodeTemplate)
        }
    }

    @Throws(BluePrintException::class)
    open fun enrichNodeTemplate(nodeTemplateName: String, nodeTemplate: NodeTemplate) {
        val nodeTypeName = nodeTemplate.type
        // Get NodeType from Repo and Update Service Template
        val nodeType = populateNodeType(nodeTypeName)

        // Enrich NodeType
        enrichNodeType(nodeTypeName, nodeType)

        //Enrich Node Template Artifacts
        enrichNodeTemplateArtifactDefinition(nodeTemplateName, nodeTemplate)
    }

    open fun enrichNodeType(nodeTypeName: String, nodeType: NodeType) {

        // NodeType Property Definitions
        enrichNodeTypeProperties(nodeTypeName, nodeType)

        //NodeType Requirement
        enrichNodeTypeRequirements(nodeTypeName, nodeType)

        //NodeType Capability
        enrichNodeTypeCapabilityProperties(nodeTypeName, nodeType)

        //NodeType Interface
        enrichNodeTypeInterfaces(nodeTypeName, nodeType)
    }

    open fun enrichNodeTypeProperties(nodeTypeName: String, nodeType: NodeType) {
        nodeType.properties?.let { enrichPropertyDefinitions(nodeType.properties!!) }
    }

    open fun enrichNodeTypeRequirements(nodeTypeName: String, nodeType: NodeType) {

        nodeType.requirements?.forEach { requirementDefinitionName, requirementDefinition ->
            // Populate Requirement Node
            requirementDefinition.node?.let { requirementNodeTypeName ->
                // Get Requirement NodeType from Repo and Update Service Template
                val requirementNodeType = populateNodeType(requirementNodeTypeName)

                enrichNodeType(requirementNodeTypeName, requirementNodeType)
            }
        }
    }

    open fun enrichNodeTypeCapabilityProperties(nodeTypeName: String, nodeType: NodeType) {
        nodeType.capabilities?.forEach { capabilityDefinitionName, capabilityDefinition ->
            capabilityDefinition.properties?.let { properties ->
                enrichPropertyDefinitions(properties)
            }
        }
    }

    open fun enrichNodeTypeInterfaces(nodeTypeName: String, nodeType: NodeType) {
        nodeType.interfaces?.forEach { interfaceName, interfaceObj ->
            // Populate Node type Interface Operation
            log.info("*** ** Enriching NodeType: {} Interface {}", nodeTypeName, interfaceName)
            populateNodeTypeInterfaceOperation(nodeTypeName, interfaceName, interfaceObj)

        }
    }

    open fun populateNodeTypeInterfaceOperation(nodeTypeName: String, interfaceName: String, interfaceObj: InterfaceDefinition) {

        interfaceObj.operations?.forEach { operationName, operation ->
            enrichNodeTypeInterfaceOperationInputs(nodeTypeName, operationName, operation)
            enrichNodeTypeInterfaceOperationOputputs(nodeTypeName, operationName, operation)
        }
    }

    open fun enrichNodeTypeInterfaceOperationInputs(nodeTypeName: String, operationName: String, operation: OperationDefinition) {
        operation.inputs?.let { inputs ->
            enrichPropertyDefinitions(inputs)
        }
    }

    open fun enrichNodeTypeInterfaceOperationOputputs(nodeTypeName: String, operationName: String, operation: OperationDefinition) {
        operation.outputs?.let { inputs ->
            enrichPropertyDefinitions(inputs)
        }
    }

    open fun enrichPropertyDefinitions(properties: MutableMap<String, PropertyDefinition>) {

        properties.forEach { propertyName, propertyDefinition ->
            enrichPropertyDefinition(propertyName, propertyDefinition)
        }
    }

    open fun enrichPropertyDefinition(propertyName: String, propertyDefinition: PropertyDefinition) {
        val propertyType = propertyDefinition.type
                ?: throw BluePrintException(format("Property type is missing for property : {}", propertyName))
        if (BluePrintTypes.validPrimitiveTypes().contains(propertyType)) {

        } else if (BluePrintTypes.validCollectionTypes().contains(propertyType)) {
            val entrySchema = propertyDefinition.entrySchema
                    ?: throw BluePrintException(format("Entry Schema is missing for collection property : {}", propertyName))

            if (!BluePrintTypes.validPrimitiveTypes().contains(entrySchema.type)) {
                populateDataTypes(entrySchema.type)
            }
        } else {
            populateDataTypes(propertyType)
        }

    }

    open fun enrichNodeTemplateArtifactDefinition(nodeTemplateName: String, nodeTemplate: NodeTemplate) {

        nodeTemplate.artifacts?.forEach { artifactDefinitionName, artifactDefinition ->
            val artifactTypeName = artifactDefinition.type
                    ?: throw BluePrintException(format("Artifact type is missing for NodeTemplate({}) artifact({})", nodeTemplateName, artifactDefinitionName))

            // Populate Artifact Type
            populateArtifactType(artifactTypeName)
        }
    }

    open fun populateNodeType(nodeTypeName: String): NodeType {
        val nodeType = bluePrintEnhancerRepoService.getNodeType(nodeTypeName)
                ?: throw BluePrintException(format("Couldn't get NodeType({}) from repo.", nodeTypeName))
        serviceTemplate.nodeTypes?.put(nodeTypeName, nodeType)
        return nodeType
    }

    open fun populateArtifactType(artifactTypeName: String): ArtifactType {
        val artifactType = bluePrintEnhancerRepoService.getArtifactType(artifactTypeName)
                ?: throw BluePrintException(format("Couldn't get ArtifactType({}) from repo.", artifactTypeName))
        serviceTemplate.artifactTypes?.put(artifactTypeName, artifactType)
        return artifactType
    }

    open fun populateDataTypes(dataTypeName: String): DataType {
        val dataType = bluePrintEnhancerRepoService.getDataType(dataTypeName)
                ?: throw BluePrintException(format("Couldn't get DataType({}) from repo.", dataTypeName))
        serviceTemplate.dataTypes?.put(dataTypeName, dataType)
        return dataType
    }

}

