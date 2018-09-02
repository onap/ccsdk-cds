/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.model;

public class ConfigModelConstant {
    
    private ConfigModelConstant() {
        
    }
    
    public static final String STATUS_CODE_SUCCESS = "200";
    public static final String STATUS_CODE_FAILURE = "400";
    
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILURE = "failure";
    public static final String STATUS_SKIPPED = "skipped";
    
    public static final String CONFIG_STATUS_PENDING = "pending";
    public static final String CONFIG_STATUS_FAILED = "failed";
    public static final String CONFIG_STATUS_SUCCESS = "success";
    
    public static final String USER_SYSTEM = "System";
    public static final String PROTOCOL_NETCONF = "netconf";
    
    public static final String MODEL_CONTENT_TYPE_TOSCA_JSON = "TOSCA_JSON";
    public static final String MODEL_CONTENT_TYPE_TOSCA_YAML = "TOSCA_YAML";
    public static final String MODEL_CONTENT_TYPE_TEMPLATE = "TEMPLATE";
    public static final String MODEL_CONTENT_TYPE_YANG = "YANG";
    public static final String MODEL_CONTENT_TYPE_SCHEMA = "SCHEMA";
    
    public static final String SERVICE_TEMPLATE_KEY_ARTIFACT_AUTHOR = "author";
    public static final String SERVICE_TEMPLATE_KEY_ARTIFACT_NAME = "service-template-name";
    public static final String SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION = "service-template-version";
    
    public static final String MODEL_DEFINITION_TYPE_NODE_TYPE = "node_type";
    public static final String MODEL_DEFINITION_TYPE_CAPABILITY_TYPE = "capability_type";
    public static final String MODEL_DEFINITION_TYPE_RELATIONSHIP_TYPE = "relationship_type";
    public static final String MODEL_DEFINITION_TYPE_DATA_TYPE = "data_type";
    public static final String MODEL_DEFINITION_TYPE_ARTIFACT_TYPE = "artifact_type";
    
    public static final String MODEL_TYPE_DATA_TYPE = "tosca.datatypes.Root";
    public static final String MODEL_TYPE_DATA_TYPE_DYNAMIC = "tosca.datatypes.Dynamic";
    public static final String MODEL_TYPE_NODE_TYPE = "tosca.nodes.Root";
    
    public static final String MODEL_TYPE_NODE_DG = "tosca.nodes.DG";
    public static final String MODEL_TYPE_NODE_COMPONENT = "tosca.nodes.Component";
    public static final String MODEL_TYPE_NODE_COMPONENT_PYTHON = "tosca.nodes.Component.Python";
    public static final String MODEL_TYPE_NODE_VNF = "tosca.nodes.Vnf";
    public static final String MODEL_TYPE_NODE_ARTIFACT = "tosca.nodes.Artifact";
    
    public static final String MODEL_TYPE_ARTIFACT_TEMPLATE = "tosca.artifact.Template";
    public static final String MODEL_TYPE_ARTIFACT_LICENCE = "tosca.artifacts.Licence";
    
    public static final String MODEL_TYPE_RELATIONSHIPS_DEPENDS_ON = "tosca.relationships.DependsOn";
    public static final String MODEL_TYPE_RELATIONSHIPS_HOSTED_ON = "tosca.relationships.HostedOn";
    public static final String MODEL_TYPE_RELATIONSHIPS_CONNECTS_TO = "tosca.relationships.ConnectsTo";
    public static final String MODEL_TYPE_RELATIONSHIPS_ATTACH_TO = "tosca.relationships.AttachesTo";
    public static final String MODEL_TYPE_RELATIONSHIPS_ROUTES_TO = "tosca.relationships.RoutesTo";
    
    public static final String NODE_TEMPLATE_TYPE_ARTIFACT_CONFIG_TEMPLATE = "artifact-config-template";
    
    public static final String CAPABILITY_PROPERTY_MAPPING = "mapping";
    public static final String CAPABILITY_PROPERTY_CONTENT = "content";
    
    public static final String SOURCE_INPUT = "input";
    public static final String SOURCE_DEFAULT = "default";
    public static final String SOURCE_MDSAL = "mdsal";
    public static final String SOURCE_DB = "db";
    public static final String SOURCE_COMPONENT = "component";
    
    public static final String PROPERTY_STATUS = "status";
    public static final String PROPERTY_ERROR_CODE = "error-code";
    public static final String PROPERTY_ERROR_MESSAGE = "error-message";
    
    public static final String PROPERTY_REQUEST_INPUT = "request-input";
    public static final String PROPERTY_REQUEST_HEADER = "request-header";
    public static final String PROPERTY_REQUEST_PAYLOAD = "request-payload";
    
    public static final String PROPERTY_RESPONSE_PAYLOAD = "response-payload";
    public static final String PROPERTY_RESPONSE_JSON_NODE = "response-json-node";
    
    public static final String PROPERTY_PAYLOAD = "payload";
    public static final String PROPERTY_INPUTS = "inputs";
    public static final String PROPERTY_ORGINATOR_ID = "originator-id";
    public static final String PROPERTY_API_VERSION = "api-ver";
    public static final String PROPERTY_REQUEST_ID = "request-id";
    public static final String PROPERTY_SUB_REQUEST_ID = "sub-request-id";
    public static final String PROPERTY_REQUEST = "request";
    public static final String PROPERTY_RECIPE = "action";
    
    public static final String PROPERTY_SELECTOR = "prifix";
    public static final String PROPERTY_NODE_TEMPLATES = "node_templates";
    public static final String PROPERTY_NODE_TYPES = "node_types";
    public static final String PROPERTY_DATA_TYPES = "data_types";
    public static final String PROPERTY_ACTION_NAME = "action-name";
    public static final String PROPERTY_ACTION_PREFIX = "action-prefix";
    public static final String PROPERTY_TEMPLATE_NAME = "template-name";
    
    public static final String PROPERTY_CURRENT_INTERFACE = "current-interface";
    public static final String PROPERTY_CURRENT_OPERATION = "current-operation";
    public static final String PROPERTY_CURRENT_IMPLEMENTATION = "current-implementation";
    public static final String PROPERTY_CURRENT_NODETYPE_DERIVED_FROM = "current-node-type-derived-from";
    public static final String PROPERTY_CURRENT_RESOURCE_ASSIGNMENT = "current-resource-assignment";
    public static final String PROPERTY_CURRENT_DICTIONARY_DEFINITION = "current-dictionary-definition";
    public static final String PROPERTY_RESOURCE_ASSIGNMENTS_DATA = "resource-assignments-data";
    
    public static final String PROPERTY_RECIPE_NAMES = "action-names";
    public static final String PROPERTY_DICTIONARIES = "dictionaries";
    public static final String PROPERTY_RESOURCE_ASSIGNMENTS = "resource-assignments";
    public static final String PROPERTY_RESOURCE_KEY = "resource-key";
    public static final String PROPERTY_RESOURCE_NAME = "resource-name";
    public static final String PROPERTY_RESERVATION_ID = "reservation-id";
    
    public static final String PROPERTY_NODE_TEMPLATES_DOT = "node_templates.";
    public static final String PROPERTY_NODE_TYPES_DOT = "node_types.";
    public static final String PROPERTY_DATA_TYPES_DOT = "data_types.";
    public static final String PROPERTY_INPUTS_DOT = "inputs.";
    public static final String PROPERTY_ARTIFACTS_DOT = "artifacts.";
    public static final String PROPERTY_DICTIONARY_KEY_DOT = "dictionary-key.";
    public static final String PROPERTY_TEMPLATE_KEY_DOT = "template-key.";
    public static final String PROPERTY_RECIPE_KEY_DOT = "recipe-key.";
    
    public static final String PROPERTY_DOT_STATUS = ".status";
    public static final String PROPERTY_DOT_ERROR_MESSAGE = ".error-message";
    
    public static final String EXPRESSION_GET_INPUT = "get_input";
    public static final String EXPRESSION_GET_ATTRIBUTE = "get_attribute";
    public static final String EXPRESSION_SET_VALUE = "set_value";
    
    public static final String CONFIG_PROPERTY_MAP_KEY_PREFIX = "org.onap.ccsdk.sli.adaptors.";
    
}
