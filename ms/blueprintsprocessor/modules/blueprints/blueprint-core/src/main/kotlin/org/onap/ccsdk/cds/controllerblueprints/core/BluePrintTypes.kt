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
object BluePrintTypes {

    @JvmStatic
    val validNodeTypeDerivedFroms: MutableList<String> = arrayListOf(
        BluePrintConstants.MODEL_TYPE_NODES_ROOT,
        BluePrintConstants.MODEL_TYPE_NODE_WORKFLOW,
        BluePrintConstants.MODEL_TYPE_NODE_COMPONENT,
        BluePrintConstants.MODEL_TYPE_NODE_VNF,
        BluePrintConstants.MODEL_TYPE_NODE_RESOURCE_SOURCE,
        BluePrintConstants.MODEL_TYPE_NODES_COMPONENT_JAVA,
        BluePrintConstants.MODEL_TYPE_NODES_COMPONENT_BUNDLE,
        BluePrintConstants.MODEL_TYPE_NODES_COMPONENT_SCRIPT,
        BluePrintConstants.MODEL_TYPE_NODES_COMPONENT_PYTHON,
        BluePrintConstants.MODEL_TYPE_NODES_COMPONENT_JYTHON,
        BluePrintConstants.MODEL_TYPE_NODES_COMPONENT_JAVA_SCRIPT
    )

    @JvmStatic
    val validArtifactTypeDerivedFroms: MutableList<String> = arrayListOf(
        BluePrintConstants.MODEL_TYPE_ARTIFACTS_ROOT,
        BluePrintConstants.MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION
    )

    @JvmStatic
    val validDataTypeDerivedFroms: MutableList<String> = arrayListOf(
        BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT,
        BluePrintConstants.MODEL_TYPE_DATA_TYPE_DYNAMIC
    )

    @JvmStatic
    val validRelationShipDerivedFroms: MutableList<String> = arrayListOf(
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_DEPENDS_ON,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_HOSTED_ON,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ATTACH_TO,
        BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROUTES_TO
    )

    @JvmStatic
    val validCapabilityTypes: MutableList<String> = arrayListOf(
        BluePrintConstants.MODEL_TYPE_CAPABILITIES_ROOT,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_NODE,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_COMPUTE,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_NETWORK,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_STORAGE,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_PUBLIC,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_ADMIN,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_DATABASE,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_ATTACHMENT,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_OPERATION_SYSTEM,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_BINDABLE,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_CONTENT,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_MAPPING,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_NETCONF,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_RESTCONF,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_SSH,
        BluePrintConstants.MODEL_TYPE_CAPABILITY_TYPE_SFTP
    )

    @JvmStatic
    fun validModelTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_ARTIFACT_TYPE)
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_NODE_TYPE)
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_CAPABILITY_TYPE)
        validTypes.add(BluePrintConstants.MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE)
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
        validTypes.add(BluePrintConstants.DATA_TYPE_STRING)
        validTypes.add(BluePrintConstants.DATA_TYPE_INTEGER)
        validTypes.add(BluePrintConstants.DATA_TYPE_FLOAT)
        validTypes.add(BluePrintConstants.DATA_TYPE_DOUBLE)
        validTypes.add(BluePrintConstants.DATA_TYPE_BOOLEAN)
        validTypes.add(BluePrintConstants.DATA_TYPE_TIMESTAMP)
        validTypes.add(BluePrintConstants.DATA_TYPE_NULL)
        return validTypes
    }

    @JvmStatic
    fun validComplexTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BluePrintConstants.DATA_TYPE_JSON)
        validTypes.add(BluePrintConstants.DATA_TYPE_MAP)
        return validTypes
    }

    @JvmStatic
    fun validCollectionTypes(): List<String> {
        val validTypes: MutableList<String> = arrayListOf()
        validTypes.add(BluePrintConstants.DATA_TYPE_LIST)
        return validTypes
    }

    @JvmStatic
    fun validPrimitiveOrCollectionPrimitive(propertyDefinition: PropertyDefinition): Boolean {
        val entrySchema = propertyDefinition.entrySchema?.type ?: BluePrintConstants.DATA_TYPE_NULL
        return BluePrintTypes.validPropertyTypes().contains(propertyDefinition.type) &&
            BluePrintTypes.validPrimitiveTypes().contains(entrySchema)
    }

    @JvmStatic
    fun validCommands(): List<String> {
        return listOf(
            BluePrintConstants.EXPRESSION_DSL_REFERENCE,
            BluePrintConstants.EXPRESSION_GET_INPUT,
            BluePrintConstants.EXPRESSION_GET_ATTRIBUTE,
            BluePrintConstants.EXPRESSION_GET_PROPERTY,
            BluePrintConstants.EXPRESSION_GET_ARTIFACT,
            BluePrintConstants.EXPRESSION_GET_OPERATION_OUTPUT,
            BluePrintConstants.EXPRESSION_GET_NODE_OF_TYPE
        )
    }

    @JvmStatic
    fun rootNodeTypes(): List<String> {
        return listOf(BluePrintConstants.MODEL_TYPE_NODES_ROOT)
    }

    @JvmStatic
    fun rootRelationshipTypes(): List<String> {
        return listOf(BluePrintConstants.MODEL_TYPE_RELATIONSHIPS_ROOT)
    }

    @JvmStatic
    fun rootDataTypes(): List<String> {
        return listOf(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT)
    }
}
