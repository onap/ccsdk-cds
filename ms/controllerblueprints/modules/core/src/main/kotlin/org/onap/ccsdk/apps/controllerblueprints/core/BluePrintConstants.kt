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

package org.onap.ccsdk.apps.controllerblueprints.core
/**
 *
 *
 * @author Brinda Santh
 */
object BluePrintConstants {

    const val TYPE_DEFAULT: String = "default"

    const val DATA_TYPE_STRING: String = "string"
    const val DATA_TYPE_INTEGER: String = "integer"
    const val DATA_TYPE_FLOAT: String = "float"
    const val DATA_TYPE_BOOLEAN: String = "boolean"
    const val DATA_TYPE_TIMESTAMP: String = "timestamp"
    const val DATA_TYPE_NULL: String = "null"
    const val DATA_TYPE_LIST: String = "list"
    const val DATA_TYPE_MAP: String = "map"

    const val USER_SYSTEM: String = "System"

    const val MODEL_CONTENT_TYPE_JSON: String = "JSON"
    const val MODEL_CONTENT_TYPE_YAML: String = "YAML"
    const val MODEL_CONTENT_TYPE_YANG: String = "YANG"
    const val MODEL_CONTENT_TYPE_SCHEMA: String = "SCHEMA"

    const val PATH_DIVIDER: String = "/"
    const val PATH_INPUTS: String = "inputs"
    const val PATH_NODE_WORKFLOWS: String = "workflows"
    const val PATH_NODE_TEMPLATES: String = "node_templates"
    const val PATH_CAPABILITIES: String = "capabilities"
    const val PATH_REQUIREMENTS: String = "requirements"
    const val PATH_INTERFACES: String = "interfaces"
    const val PATH_OPERATIONS: String = "operations"
    const val PATH_OUTPUTS: String = "outputs"
    const val PATH_PROPERTIES: String = "properties"
    const val PATH_ATTRIBUTES: String = "attributes"
    const val PATH_ARTIFACTS: String = "artifacts"

    const val MODEL_DIR_MODEL_TYPE: String = "model_type"

    const val MODEL_DEFINITION_TYPE_NODE_TYPE: String = "node_type"
    const val MODEL_DEFINITION_TYPE_ARTIFACT_TYPE: String = "artifact_type"
    const val MODEL_DEFINITION_TYPE_CAPABILITY_TYPE: String = "capability_type"
    const val MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE: String = "relationship_type"
    const val MODEL_DEFINITION_TYPE_DATA_TYPE: String = "data_type"

    const val MODEL_TYPE_DATATYPES_ROOT: String = "tosca.datatypes.Root"
    const val MODEL_TYPE_NODES_ROOT: String = "tosca.nodes.Root"
    const val MODEL_TYPE_GROUPS_ROOT: String = "tosca.groups.Root"
    const val MODEL_TYPE_RELATIONSHIPS_ROOT: String = "tosca.relationships.Root"
    const val MODEL_TYPE_ARTIFACTS_ROOT: String = "tosca.artifacts.Root"
    const val MODEL_TYPE_CAPABILITIES_ROOT: String = "tosca.capabilities.Root"
    const val MODEL_TYPE_INTERFACES_ROOT: String = "tosca.interfaces.Root"

    const val MODEL_TYPE_RELATIONSHIPS_DEPENDS_ON = "tosca.relationships.DependsOn"
    const val MODEL_TYPE_RELATIONSHIPS_HOSTED_ON = "tosca.relationships.HostedOn"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO = "tosca.relationships.ConnectsTo"
    const val MODEL_TYPE_RELATIONSHIPS_ATTACH_TO = "tosca.relationships.AttachesTo"
    const val MODEL_TYPE_RELATIONSHIPS_ROUTES_TO = "tosca.relationships.RoutesTo"

    const val MODEL_TYPE_NODE_DG = "tosca.nodes.DG"
    const val MODEL_TYPE_NODE_COMPONENT = "tosca.nodes.Component"
    const val MODEL_TYPE_NODE_VNF = "tosca.nodes.Vnf"
    @Deprecated("Artifacts will be attached to Node Template")
    const val MODEL_TYPE_NODE_ARTIFACT = "tosca.nodes.Artifact"
    const val MODEL_TYPE_NODE_RESOURCE_SOURCE = "tosca.nodes.ResourceSource"

    const val MODEL_TYPE_NODES_COMPONENT_JAVA: String = "tosca.nodes.component.Java"
    const val MODEL_TYPE_NODES_COMPONENT_BUNDLE: String = "tosca.nodes.component.Bundle"
    const val MODEL_TYPE_NODES_COMPONENT_SCRIPT: String = "tosca.nodes.component.Script"
    const val MODEL_TYPE_NODES_COMPONENT_PYTHON: String = "tosca.nodes.component.Python"
    const val MODEL_TYPE_NODES_COMPONENT_JAVA_SCRIPT: String = "tosca.nodes.component.JavaScript"

    const val MODEL_TYPE_DATA_TYPE_DYNAMIC = "tosca.datatypes.Dynamic"

    const val EXPRESSION_GET_INPUT: String = "get_input"
    const val EXPRESSION_GET_ATTRIBUTE: String = "get_attribute"
    const val EXPRESSION_GET_ARTIFACT: String = "get_artifact"
    const val EXPRESSION_GET_PROPERTY: String = "get_property"
    const val EXPRESSION_GET_OPERATION_OUTPUT: String = "get_operation_output"
    const val EXPRESSION_GET_NODE_OF_TYPE: String = "get_nodes_of_type"

    const val PROPERTY_BLUEPRINT_PROCESS_ID: String = "blueprint-process-id"
    const val PROPERTY_BLUEPRINT_BASE_PATH: String = "blueprint-basePath"
    const val PROPERTY_BLUEPRINT_RUNTIME: String = "blueprint-runtime"
    const val PROPERTY_BLUEPRINT_INPUTS_DATA: String = "blueprint-inputs-data"
    const val PROPERTY_BLUEPRINT_CONTEXT: String = "blueprint-context"
    const val PROPERTY_BLUEPRINT_NAME: String = "template_name"
    const val PROPERTY_BLUEPRINT_VERSION: String = "template_version"

    const val TOSCA_METADATA_ENTRY_DEFINITION_FILE: String = "TOSCA-Metadata/TOSCA.meta"
    const val TOSCA_PLANS_DIR: String = "Plans"
    const val TOSCA_SCRIPTS_DIR: String = "Scripts"
    const val TOSCA_MAPPINGS_DIR: String = "Mappings"
    const val TOSCA_TEMPLATES_DIR: String = "Templates"

    const val METADATA_USER_GROUPS = "user-groups"
    const val METADATA_TEMPLATE_NAME = "template_name"
    const val METADATA_TEMPLATE_VERSION = "template_version"
    const val METADATA_TEMPLATE_AUTHOR = "template_author"
    const val METADATA_TEMPLATE_TAGS = "template_tags"

    const val PAYLOAD_CONTENT = "payload-content"
    const val PAYLOAD_DATA = "payload-data"
    const val SELECTOR = "selector"
    const val PROPERTY_CURRENT_INTERFACE = "current-interface"
    const val PROPERTY_CURRENT_OPERATION = "current-operation"
    const val PROPERTY_CURRENT_IMPLEMENTATION = "current-implementation"

    const val PROPERTY_ACTION_NAME = "action"

    const val OPERATION_PROCESS = "process"
    const val OPERATION_PREPARE = "prepare"

    const val BLUEPRINT_RETRIEVE_TYPE_DB = "db"
    const val BLUEPRINT_RETRIEVE_TYPE_FILE = "file"
    const val BLUEPRINT_RETRIEVE_TYPE_REPO = "repo"

}