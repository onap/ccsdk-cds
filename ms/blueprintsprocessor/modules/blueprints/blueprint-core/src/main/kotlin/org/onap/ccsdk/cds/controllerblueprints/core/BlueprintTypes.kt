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

package org.onap.ccsdk.cds.controllerblueprints.core

import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition

/**
 *
 *
 * @author Brinda Santh
 */
object BlueprintTypes {

    @JvmStatic
    val validNodeTypeDerivedFroms: MutableList<String> = arrayListOf(
        BlueprintConstants.MODEL_TYPE_NODES_ROOT,
        BlueprintConstants.MODEL_TYPE_NODE_WORKFLOW,
        BlueprintConstants.MODEL_TYPE_NODE_COMPONENT,
        BlueprintConstants.MODEL_TYPE_NODE_VNF,
        BlueprintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        BlueprintConstants.MODEL_TYPE_NODES_COMPONENT_JAVA,
        BlueprintConstants.MODEL_TYPE_NODES_COMPONENT_BUNDLE,
        BlueprintConstants.MODEL_TYPE_NODES_COMPONENT_SCRIPT,
        BlueprintConstants.MODEL_TYPE_NODES_COMPONENT_PYTHON,
        BlueprintConstants.MODEL_TYPE_NODES_COMPONENT_JYTHON,
        BlueprintConstants.MODEL_TYPE_NODES_COMPONENT_JAVA_SCRIPT
    )

    @JvmStatic
    val validArtifactTypeDerivedFroms: MutableList<String> = arrayListOf(
        BlueprintConstants.MODEL_TYPE_ARTIFACTS_ROOT,
        BlueprintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION
    )

    @JvmStatic
    val validDataTypeDerivedFroms: MutableList<String> = arrayListOf(
        BlueprintConstants.MODEL_TYPE_DATATYPES_ROOT,
        BlueprintConstants.MODEL_TYPE_DATA_TYPE_DYNAMIC
    )

    @JvmStatic
    val validRelationShipDerivedFroms: MutableList<String> = arrayListOf(
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_DEPENDS_ON,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_HOSTED_ON,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_ATTACH_TO,
        BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_ROUTES_TO
    )

    @JvmStatic
    val validCapabilityTypes: MutableList<String> = arrayListOf(
        BlueprintConstants.MODEL_TYPE_CAPABILITIES_ROOT,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_NODE,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_COMPUTE,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_NETWORK,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_STORAGE,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_PUBLIC,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_ADMIN,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_DATABASE,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_ATTACHMENT,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_OPERATION_SYSTEM,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_BINDABLE,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_CONTENT,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_MAPPING,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_NETCONF,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_RESTCONF,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_SSH,
        BlueprintConstants.MODEL_TYPE_CAPABILITY_TYPE_SFTP
    )

    @JvmStatic
    fun validModelTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BlueprintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
        validTypes.add(BlueprintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE)
        validTypes.add(BlueprintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE)
        validTypes.add(BlueprintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE)
        validTypes.add(BlueprintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE)
        return validTypes
    }

    @JvmStatic
    fun validPropertyTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.addAll(validPrimitiveTypes())
        validTypes.addAll(validComplexTypes())
        validTypes.addAll(validCollectionTypes())
        return validTypes
    }

    @JvmStatic
    fun validPrimitiveTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BlueprintConstants.DATA_TYPE_STRING)
        validTypes.add(BlueprintConstants.DATA_TYPE_INTEGER)
        validTypes.add(BlueprintConstants.DATA_TYPE_FLOAT)
        validTypes.add(BlueprintConstants.DATA_TYPE_DOUBLE)
        validTypes.add(BlueprintConstants.DATA_TYPE_BOOLEAN)
        validTypes.add(BlueprintConstants.DATA_TYPE_TIMESTAMP)
        validTypes.add(BlueprintConstants.DATA_TYPE_NULL)
        return validTypes
    }

    @JvmStatic
    fun validComplexTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BlueprintConstants.DATA_TYPE_JSON)
        validTypes.add(BlueprintConstants.DATA_TYPE_MAP)
        return validTypes
    }

    @JvmStatic
    fun validCollectionTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BlueprintConstants.DATA_TYPE_LIST)
        return validTypes
    }

    @JvmStatic
    fun validPrimitiveOrCollectionPrimitive(propertyDefinition: PropertyDefinition): Boolean {
        val entrySchema = propertyDefinition.entrySchema?.type ?: BlueprintConstants.DATA_TYPE_NULL
        return BlueprintTypes.validPropertyTypes().contains(propertyDefinition.type) &&
            BlueprintTypes.validPrimitiveTypes().contains(entrySchema)
    }

    @JvmStatic
    fun validCommands(): List<String> {
        return listOf(
            BlueprintConstants.EXPRESSION_DSL_REFERENCE,
            BlueprintConstants.EXPRESSION_GET_INPUT,
            BlueprintConstants.EXPRESSION_GET_ATTRIBUTE,
            BlueprintConstants.EXPRESSION_GET_PROPERTY,
            BlueprintConstants.EXPRESSION_GET_ARTIFACT,
            BlueprintConstants.EXPRESSION_GET_OPERATION_OUTPUT,
            BlueprintConstants.EXPRESSION_GET_NODE_OF_TYPE
        )
    }

    @JvmStatic
    fun rootNodeTypes(): List<String> {
        return listOf(BlueprintConstants.MODEL_TYPE_NODES_ROOT)
    }

    @JvmStatic
    fun rootRelationshipTypes(): List<String> {
        return listOf(BlueprintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT)
    }

    @JvmStatic
    fun rootDataTypes(): List<String> {
        return listOf(BlueprintConstants.MODEL_TYPE_DATATYPES_ROOT)
    }
}
