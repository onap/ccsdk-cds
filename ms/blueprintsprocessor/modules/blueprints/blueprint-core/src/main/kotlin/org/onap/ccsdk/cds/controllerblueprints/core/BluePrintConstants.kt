/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 - 2020 IBM, Bell Canada
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

/**
 * BluePrintConstants
 *
 * @author Brinda Santh
 */
object BluePrintConstants {

    val APP_NAME = System.getenv("APP_NAME")
        ?: "cds-controller"

    const val DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    const val RESPONSE_HEADER_MINOR_VERSION: String = "X-MinorVersion"
    const val RESPONSE_HEADER_PATCH_VERSION: String = "X-PatchVersion"
    const val RESPONSE_HEADER_LATEST_VERSION: String = "X-LatestVersion"

    const val ONAP_REQUEST_ID = "X-ONAP-RequestID"
    const val ONAP_SUBREQUEST_ID = "X-ONAP-SubRequestID"
    const val ONAP_ORIGINATOR_ID = "X-ONAP-OriginatorID"
    const val ONAP_INVOCATION_ID = "X-ONAP-InvocationID"
    const val ONAP_PARTNER_NAME = "X-ONAP-PartnerName"

    const val STATUS_SUCCESS: String = "success"
    const val STATUS_PROCESSING: String = "processing"
    const val STATUS_FAILURE: String = "failure"

    const val FLAG_Y: String = "Y"
    const val FLAG_N: String = "N"

    const val TYPE_DEFAULT: String = "default"

    const val DATA_TYPE_STRING: String = "string"
    const val DATA_TYPE_INTEGER: String = "integer"
    const val DATA_TYPE_FLOAT: String = "float"
    const val DATA_TYPE_DOUBLE: String = "double"
    const val DATA_TYPE_BOOLEAN: String = "boolean"
    const val DATA_TYPE_TIMESTAMP: String = "timestamp"
    const val DATA_TYPE_NULL: String = "null"
    const val DATA_TYPE_LIST: String = "list"
    const val DATA_TYPE_MAP: String = "map"
    const val DATA_TYPE_JSON: String = "json"

    const val BLUEPRINT_TYPE_DEFAULT = "DEFAULT"
    const val BLUEPRINT_TYPE_KOTLIN_DSL = "KOTLIN_DSL"
    const val BLUEPRINT_TYPE_GENERIC_SCRIPT = "GENERIC_SCRIPT"

    const val SCRIPT_KOTLIN = "kotlin"
    const val SCRIPT_JYTHON = "jython"
    const val SCRIPT_INTERNAL = "internal"

    const val USER_SYSTEM: String = "System"

    const val PATH_DIVIDER: String = "/"
    const val PATH_SERVICE_TEMPLATE: String = "service_template"
    const val PATH_TOPOLOGY_TEMPLATE: String = "topology_template"
    const val PATH_METADATA: String = "metadata"
    const val PATH_NODE_TYPES: String = "node_types"
    const val PATH_POLICY_TYPES: String = "policy_types"
    const val PATH_RELATIONSHIP_TYPES: String = "relationship_types"
    const val PATH_ARTIFACT_TYPES: String = "artifact_types"
    const val PATH_DATA_TYPES: String = "data_types"
    const val PATH_INPUTS: String = "inputs"
    const val PATH_NODE_WORKFLOWS: String = "workflows"
    const val PATH_NODE_TEMPLATES: String = "node_templates"
    const val PATH_RELATIONSHIP_TEMPLATES: String = "relationship_templates"
    const val PATH_CAPABILITIES: String = "capabilities"
    const val PATH_REQUIREMENTS: String = "requirements"
    const val PATH_INTERFACES: String = "interfaces"
    const val PATH_OPERATIONS: String = "operations"
    const val PATH_OUTPUTS: String = "outputs"
    const val PATH_PROPERTIES: String = "properties"
    const val PATH_ATTRIBUTES: String = "attributes"
    const val PATH_ARTIFACTS: String = "artifacts"

    const val MODEL_DIR_MODEL_TYPE: String = "definition-type"

    const val MODEL_DEFINITION_TYPE_NODE_TYPE: String = "node_type"
    const val MODEL_DEFINITION_TYPE_ARTIFACT_TYPE: String = "artifact_type"
    const val MODEL_DEFINITION_TYPE_CAPABILITY_TYPE: String = "capability_type"
    const val MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE: String = "relationship_type"
    const val MODEL_DEFINITION_TYPE_DATA_TYPE: String = "data_type"
    const val MODEL_DEFINITION_TYPE_NODE_TEMPLATE: String = "node_template"
    const val MODEL_DEFINITION_TYPE_RELATIONSHIP_TEMPLATE: String = "relationship_template"
    const val MODEL_DEFINITION_TYPE_WORKFLOW: String = "workflow"
    const val MODEL_DEFINITION_TYPE_DSL: String = "dsl"

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
    // CDS Defined Relationship Types
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_DB = "tosca.relationships.ConnectsTo.Db"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_REST_CLIENT = "tosca.relationships.ConnectsTo.RestClient"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_SSH_CLIENT = "tosca.relationships.ConnectsTo.SshClient"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_PRODUCER = "tosca.relationships.ConnectsTo.MessageProducer"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_MESSAGE_CONSUMER = "tosca.relationships.ConnectsTo.MessageConsumer"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_SERVER = "tosca.relationships.ConnectsTo.GrpcServer"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_GRPC_CLIENT = "tosca.relationships.ConnectsTo.GrpcClient"
    const val MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO_NATS = "tosca.relationships.ConnectsTo.Nats"

    const val MODEL_TYPE_NODE_WORKFLOW = "tosca.nodes.Workflow"
    const val MODEL_TYPE_NODE_COMPONENT = "tosca.nodes.Component"
    const val MODEL_TYPE_NODE_VNF = "tosca.nodes.Vnf"
    const val MODEL_TYPE_NODE_RESOURCE_SOURCE = "tosca.nodes.ResourceSource"

    const val MODEL_TYPE_NODES_COMPONENT_JAVA: String = "tosca.nodes.component.Java"
    const val MODEL_TYPE_NODES_COMPONENT_BUNDLE: String = "tosca.nodes.component.Bundle"
    const val MODEL_TYPE_NODES_COMPONENT_SCRIPT: String = "tosca.nodes.component.Script"
    const val MODEL_TYPE_NODES_COMPONENT_PYTHON: String = "tosca.nodes.component.Python"
    const val MODEL_TYPE_NODES_COMPONENT_JYTHON: String = "tosca.nodes.component.Jython"
    const val MODEL_TYPE_NODES_COMPONENT_KOTLIN: String = "tosca.nodes.component.Kotlin"
    const val MODEL_TYPE_NODES_COMPONENT_JAVA_SCRIPT: String = "tosca.nodes.component.JavaScript"

    const val MODEL_TYPE_ARTIFACT_TYPE_IMPLEMENTATION = "tosca.artifacts.Implementation"

    const val MODEL_TYPE_DATA_TYPE_DYNAMIC = "tosca.datatypes.Dynamic"

    const val MODEL_TYPE_CAPABILITY_TYPE_NODE = "tosca.capabilities.Node"
    const val MODEL_TYPE_CAPABILITY_TYPE_COMPUTE = "tosca.capabilities.Compute"
    const val MODEL_TYPE_CAPABILITY_TYPE_NETWORK = "tosca.capabilities.Network"
    const val MODEL_TYPE_CAPABILITY_TYPE_STORAGE = "tosca.capabilities.Storage"
    const val MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT = "tosca.capabilities.Endpoint"
    const val MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_PUBLIC = "tosca.capabilities.Endpoint.Public"
    const val MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_ADMIN = "tosca.capabilities.Endpoint.Admin"
    const val MODEL_TYPE_CAPABILITY_TYPE_ENDPOINT_DATABASE = "tosca.capabilities.Endpoint.Database"
    const val MODEL_TYPE_CAPABILITY_TYPE_ATTACHMENT = "tosca.capabilities.Attachment"
    const val MODEL_TYPE_CAPABILITY_TYPE_OPERATION_SYSTEM = "tosca.capabilities.OperatingSystem"
    const val MODEL_TYPE_CAPABILITY_TYPE_BINDABLE = "tosca.capabilities.network.Bindable"
    // Custom capabilities
    const val MODEL_TYPE_CAPABILITY_TYPE_CONTENT = "tosca.capabilities.Content"
    const val MODEL_TYPE_CAPABILITY_TYPE_MAPPING = "tosca.capabilities.Mapping"
    const val MODEL_TYPE_CAPABILITY_TYPE_NETCONF = "tosca.capabilities.Netconf"
    const val MODEL_TYPE_CAPABILITY_TYPE_SSH = "tosca.capabilities.Ssh"
    const val MODEL_TYPE_CAPABILITY_TYPE_SFTP = "tosca.capabilities.Sftp"

    const val EXPRESSION_DSL_REFERENCE: String = "*"
    const val EXPRESSION_GET_INPUT: String = "get_input"
    const val EXPRESSION_GET_ATTRIBUTE: String = "get_attribute"
    const val EXPRESSION_GET_ARTIFACT: String = "get_artifact"
    const val EXPRESSION_GET_PROPERTY: String = "get_property"
    const val EXPRESSION_GET_OPERATION_OUTPUT: String = "get_operation_output"
    const val EXPRESSION_GET_NODE_OF_TYPE: String = "get_nodes_of_type"

    const val PROPERTY_BLUEPRINT_PROCESS_ID: String = "blueprint-process-id"
    const val PROPERTY_BLUEPRINT_VALID: String = "blueprint-valid"
    const val PROPERTY_BLUEPRINT_BASE_PATH: String = "blueprint-basePath"
    const val PROPERTY_BLUEPRINT_RUNTIME: String = "blueprint-runtime"
    const val PROPERTY_BLUEPRINT_INPUTS_DATA: String = "blueprint-inputs-data"
    const val PROPERTY_BLUEPRINT_CONTEXT: String = "blueprint-context"
    const val PROPERTY_BLUEPRINT_NAME: String = "template_name"
    const val PROPERTY_BLUEPRINT_VERSION: String = "template_version"

    const val TOSCA_METADATA_DIR: String = "TOSCA-Metadata"
    const val TOSCA_METADATA_ENTRY_DEFINITION_FILE: String = "TOSCA-Metadata/TOSCA.meta"
    const val TOSCA_DEFINITIONS_DIR: String = "Definitions"
    const val TOSCA_PLANS_DIR: String = "Plans"
    const val TOSCA_SCRIPTS_DIR: String = "Scripts"
    const val TOSCA_MAPPINGS_DIR: String = "Mappings"
    const val TOSCA_TEMPLATES_DIR: String = "Templates"
    const val TOSCA_ENVIRONMENTS_DIR: String = "Environments"
    const val TOSCA_SCRIPTS_KOTLIN_DIR: String = "$TOSCA_SCRIPTS_DIR/kotlin"
    const val TOSCA_SCRIPTS_JYTHON_DIR: String = "$TOSCA_SCRIPTS_DIR/python"

    const val UAT_SPECIFICATION_FILE = "Tests/uat.yaml"

    const val GRAPH_START_NODE_NAME = "START"
    const val GRAPH_END_NODE_NAME = "END"

    const val PROPERTY_ENV = "ENV"
    const val PROPERTY_APP = "APP"
    const val PROPERTY_BPP = "BPP"
    const val PROPERTY_SELF = "SELF"

    const val METADATA_TEMPLATE_NAME = "template_name"
    const val METADATA_TEMPLATE_VERSION = "template_version"
    const val METADATA_TEMPLATE_TYPE = "template_type"
    const val METADATA_TEMPLATE_AUTHOR = "template_author"
    const val METADATA_TEMPLATE_TAGS = "template_tags"
    const val METADATA_TEMPLATE_DESCRIPTION = "template_description"
    const val METADATA_WORKFLOW_NAME = "workflow_name"

    const val PAYLOAD_DATA = "payload-data"
    const val PROPERTY_CURRENT_STEP = "current-step"
    const val PROPERTY_CURRENT_NODE_TEMPLATE = "current-node-template"
    const val PROPERTY_CURRENT_INTERFACE = "current-interface"
    const val PROPERTY_CURRENT_OPERATION = "current-operation"
    const val PROPERTY_CURRENT_TIMEOUT = "current-timeout"
    const val PROPERTY_CURRENT_IMPLEMENTATION = "current-implementation"
    const val PROPERTY_EXECUTION_REQUEST = "execution-request"
    const val PROPERTY_CONNECTION_CONFIG = "connection-config"

    const val DEFAULT_VERSION_NUMBER = "1.0.0"
    const val DEFAULT_STEP_OPERATION = "process"
    const val DEFAULT_STEP_INTERFACE = "ComponentInterface"

    const val MODEL_TYPE_ARTIFACT_TEMPLATE_VELOCITY = "artifact-template-velocity"
    const val MODEL_TYPE_ARTIFACT_TEMPLATE_JINJA = "artifact-template-jinja"
    const val MODEL_TYPE_ARTIFACT_MAPPING_RESOURCE = "artifact-mapping-resource"
    const val MODEL_TYPE_ARTIFACT_SCRIPT_JYTHON = "artifact-script-jython"
    const val MODEL_TYPE_ARTIFACT_SCRIPT_KOTLIN = "artifact-script-kotlin"
    const val MODEL_TYPE_ARTIFACT_DIRECTED_GRAPH = "artifact-directed-graph"
    const val MODEL_TYPE_ARTIFACT_COMPONENT_JAR = "artifact-component-jar"
    const val MODEL_TYPE_ARTIFACT_K8S_PROFILE = "artifact-k8sprofile-content"

    const val TOSCA_SPEC = "TOSCA"

    val USE_SCRIPT_COMPILE_CACHE: Boolean = (System.getenv("USE_SCRIPT_COMPILE_CACHE") ?: "true").toBoolean()

    const val LOG_PROTECT: String = "log-protect"

    /** Cluster Properties */
    val CLUSTER_ENABLED = (System.getenv("CLUSTER_ENABLED") ?: "false").toBoolean()
    const val PROPERTY_CLUSTER_ID = "CLUSTER_ID"
    const val PROPERTY_CLUSTER_NODE_ID = "CLUSTER_NODE_ID"
    const val PROPERTY_CLUSTER_NODE_ADDRESS = "CLUSTER_NODE_ADDRESS"
    const val PROPERTY_CLUSTER_JOIN_AS_CLIENT = "CLUSTER_JOIN_AS_CLIENT"
    const val PROPERTY_CLUSTER_CONFIG_FILE = "CLUSTER_CONFIG_FILE"

    const val NODE_TEMPLATE_TYPE_COMPONENT_RESOURCE_RESOLUTION = "component-resource-resolution"
    const val NODE_TEMPLATE_TYPE_DG = "dg-generic"
    const val PROPERTY_DG_DEPENDENCY_NODE_TEMPLATE = "dependency-node-templates"
}
