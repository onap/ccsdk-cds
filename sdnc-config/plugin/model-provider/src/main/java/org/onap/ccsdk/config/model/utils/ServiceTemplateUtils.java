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

package org.onap.ccsdk.config.model.utils;

import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.InterfaceAssignment;
import org.onap.ccsdk.config.model.data.InterfaceDefinition;
import org.onap.ccsdk.config.model.data.NodeTemplate;
import org.onap.ccsdk.config.model.data.NodeType;
import org.onap.ccsdk.config.model.data.OperationAssignment;
import org.onap.ccsdk.config.model.data.OperationDefinition;
import org.onap.ccsdk.config.model.data.ServiceTemplate;
import org.onap.ccsdk.config.model.data.TopologyTemplate;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ServiceTemplateUtils {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ServiceTemplateUtils.class);
    
    public Map<String, String> convertServiceTemplate2Properties(String serviceTemplateContent,
            final Map<String, String> context) {
        if (StringUtils.isNotBlank(serviceTemplateContent)) {
            ServiceTemplate serviceTemplate =
                    TransformationUtils.readValue(serviceTemplateContent, ServiceTemplate.class);
            convertServiceTemplate2Properties(serviceTemplate, context);
        }
        return context;
    }
    
    public Map<String, String> convertServiceTemplate2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) {
        if (serviceTemplate != null) {
            convertServiceTemplateMetadata2Properties(serviceTemplate, context);
            convertServiceTemplateInputs2Properties(serviceTemplate, context);
            convertDataTypes2Properties(serviceTemplate, context);
            convertNode2Properties(serviceTemplate, context);
        }
        return context;
    }
    
    public Map<String, String> convertServiceTemplateMetadata2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) {
        
        if (serviceTemplate != null && serviceTemplate.getMetadata() != null) {
            serviceTemplate.getMetadata().forEach((metaDataKey, metadata) -> {
                context.put(metaDataKey, metadata);
            });
        }
        return context;
    }
    
    public Map<String, String> convertServiceTemplateInputs2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) {
        
        if (serviceTemplate != null && serviceTemplate.getTopologyTemplate() != null
                && serviceTemplate.getTopologyTemplate().getInputs() != null) {
            
            serviceTemplate.getTopologyTemplate().getInputs().forEach((paramKey, parameterDefinition) -> {
                if (parameterDefinition != null) {
                    context.put(ConfigModelConstant.PROPERTY_INPUTS_DOT + paramKey + ".type",
                            parameterDefinition.getType());
                    if (parameterDefinition.getRequired()) {
                        context.put(ConfigModelConstant.PROPERTY_INPUTS_DOT + paramKey + ".required",
                                String.valueOf(parameterDefinition.getRequired()));
                    }
                    if (parameterDefinition.getDefaultValue() != null) {
                        context.put(ConfigModelConstant.PROPERTY_INPUTS_DOT + paramKey + ".default",
                                String.valueOf(parameterDefinition.getDefaultValue()));
                    }
                }
            });
        }
        return context;
    }
    
    public Map<String, String> convertDataTypes2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) {
        if (serviceTemplate != null && serviceTemplate.getDataTypes() != null) {
            serviceTemplate.getDataTypes().forEach((dataTypeKey, dataType) -> {
                logger.trace("Populating Data Type Key : ({})", dataTypeKey);
                String dataTypeContent = TransformationUtils.getJson(dataType);
                context.put("data_types." + dataTypeKey, dataTypeContent);
            });
        }
        return context;
    }
    
    public Map<String, String> convertNode2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) {
        
        if (serviceTemplate != null && serviceTemplate.getNodeTypes() != null
                && serviceTemplate.getTopologyTemplate() != null
                && serviceTemplate.getTopologyTemplate().getNodeTemplates() != null) {
            
            serviceTemplate.getTopologyTemplate().getNodeTemplates().forEach((nodeTemplateKey, nodeTemplate) -> {
                if (nodeTemplate != null && StringUtils.isNotBlank(nodeTemplate.getType())) {
                    String nodeTypeKey = nodeTemplate.getType();
                    logger.trace("Populating Node Type Key : ({}) for Node Template : ({})", nodeTypeKey,
                            nodeTemplateKey);
                    String nodeTemplateContent = TransformationUtils.getJson(nodeTemplate);
                    context.put("node_templates." + nodeTemplateKey, nodeTemplateContent);
                    if (serviceTemplate.getNodeTypes().containsKey(nodeTypeKey)) {
                        NodeType nodeType = serviceTemplate.getNodeTypes().get(nodeTypeKey);
                        String nodeTypeContent = TransformationUtils.getJson(nodeType);
                        context.put("node_types." + nodeTypeKey, nodeTypeContent);
                        String nodeDerivedFrom = nodeType.getDerivedFrom();
                        if (ConfigModelConstant.MODEL_TYPE_NODE_DG.equalsIgnoreCase(nodeDerivedFrom)) {
                            populateDGNodeProperties(nodeTemplateKey, nodeTemplate, context, nodeDerivedFrom);
                        }
                    }
                    // Populate the Artifact Definitions
                    populateNodeTemplateArtifacts(nodeTemplateKey, nodeTemplate, context);
                }
            });
        }
        return context;
    }
    
    @SuppressWarnings("squid:S1172")
    public Map<String, String> populateVnfNodeProperties(String nodeTemplateKey, NodeTemplate nodeTemplate,
            final Map<String, String> context, String nodeDerivedFrom) {
        if (nodeTemplate != null && nodeTemplate.getCapabilities() != null) {
            nodeTemplate.getCapabilities().forEach((capabilityKey, capability) -> {
                capability.getProperties().forEach((propertyKey, property) -> {
                    context.put(nodeTemplateKey + "." + capabilityKey + "." + propertyKey, String.valueOf(property));
                });
            });
        }
        
        return context;
    }
    
    public Map<String, String> populateNodeTemplateArtifacts(String nodeTemplateKey, NodeTemplate nodeTemplate,
            final Map<String, String> context) {
        if (MapUtils.isNotEmpty(nodeTemplate.getArtifacts())) {
            nodeTemplate.getArtifacts().forEach((artifactName, artifact) -> {
                if (StringUtils.isNotBlank(artifactName) && artifact != null) {
                    logger.trace("Populating Node Template Artifacts ({}) for Node Template ({})", artifactName,
                            nodeTemplateKey);
                    String fileKeyName = ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + nodeTemplateKey + "."
                            + ConfigModelConstant.PROPERTY_ARTIFACTS_DOT + artifactName + ".file";
                    String deployKeyName = ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + nodeTemplateKey + "."
                            + ConfigModelConstant.PROPERTY_ARTIFACTS_DOT + artifactName + ".deploy_path";
                    String contentKeyName = ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + nodeTemplateKey + "."
                            + ConfigModelConstant.PROPERTY_ARTIFACTS_DOT + artifactName + ".content";
                    context.put(fileKeyName, artifact.getFile());
                    context.put(deployKeyName, artifact.getDeployPath());
                    context.put(contentKeyName, artifact.getContent());
                }
            });
        }
        return context;
    }
    
    public Map<String, String> populateDGNodeProperties(String nodeTemplateKey, NodeTemplate nodeTemplate,
            final Map<String, String> context, String nodeDerivedFrom) {
        if (nodeTemplate != null && nodeTemplate.getInterfaces() != null) {
            
            if (nodeTemplate.getProperties() != null) {
                nodeTemplate.getProperties().forEach((propKey, propValue) -> {
                    if (propKey != null && propValue != null) {
                        context.put("dg." + nodeTemplateKey + "." + propKey, String.valueOf(propValue));
                    }
                });
            }
            
            nodeTemplate.getInterfaces().forEach((interfaceKey, interfaceDefinition) -> {
                
                interfaceDefinition.getOperations().forEach((operationKey, operation) -> {
                    if (ConfigModelConstant.MODEL_TYPE_NODE_DG.equalsIgnoreCase(nodeDerivedFrom)) {
                        context.put("dg." + nodeTemplateKey + ".module", interfaceKey);
                        context.put("dg." + nodeTemplateKey + ".flow", operationKey);
                    }
                });
            });
        }
        return context;
    }
    
    public SvcLogicContext getInterfaceOpertationDefinition(ServiceTemplate serviceTemplate,
            final SvcLogicContext context) {
        
        if (serviceTemplate != null && serviceTemplate.getNodeTypes() != null
                && serviceTemplate.getTopologyTemplate() != null
                && serviceTemplate.getTopologyTemplate().getNodeTemplates() != null) {
            
            TopologyTemplate topologyTemplates = serviceTemplate.getTopologyTemplate();
            // Copy Definition to Template
            copyNodeType2Template(serviceTemplate.getNodeTypes(), topologyTemplates.getNodeTemplates());
            
            topologyTemplates.getNodeTemplates().forEach((templateKey, nodeTemplate) -> {
                
                if (StringUtils.isNotBlank(templateKey) && nodeTemplate != null
                        && nodeTemplate.getInterfaces() != null) {
                    
                    nodeTemplate.getInterfaces().forEach((interfaceKey, interfaceDefinition) -> {
                        if (StringUtils.isNotBlank(interfaceKey) && interfaceDefinition != null) {
                            interfaceDefinition.getOperations().forEach((operationKey, operation) -> {
                                String definitionKey = interfaceKey + "." + operationKey;
                                String definitionContent = TransformationUtils.getJson(operation);
                                context.setAttribute(definitionKey, definitionContent);
                                // Set the Operation & Method Params
                            });
                        }
                    });
                }
            });
        }
        return context;
    }
    
    public void copyNodeType2Template(Map<String, NodeType> nodeTypes, Map<String, NodeTemplate> nodeTemplates) {
        if (nodeTypes != null && nodeTemplates != null) {
            
            nodeTemplates.forEach((templateKey, nodeTemplate) -> {
                if (StringUtils.isNotBlank(templateKey) && nodeTemplate != null) {
                    String type = nodeTemplate.getType();
                    // Check the Node Template Type is Present
                    if (StringUtils.isNotBlank(type) && nodeTypes.containsKey(type)) {
                        NodeType nodeType = nodeTypes.get(type);
                        logger.trace("Checking Node Type Content : ({})", TransformationUtils.getJson(nodeType));
                        copyNodeTypeInterface2Template(nodeType.getInterfaces(), nodeTemplate.getInterfaces());
                    }
                }
            });
        }
    }
    
    public void copyNodeTypeInterface2Template(Map<String, InterfaceDefinition> nodeTypeInterfaces,
            Map<String, InterfaceAssignment> nodeTemplateInterfaces) {
        if (nodeTypeInterfaces != null && nodeTemplateInterfaces != null) {
            
            nodeTemplateInterfaces.forEach((interfaceKey, nodeTemplateinterface) -> {
                InterfaceDefinition nodeTypeInterface = nodeTypeInterfaces.get(interfaceKey);
                logger.trace("Checking Interface Type Content : ({})", TransformationUtils.getJson(nodeTypeInterface));
                if (nodeTypeInterface != null && nodeTemplateinterface != null) {
                    copyNodeTypeOperation2Template(nodeTypeInterface.getOperations(),
                            nodeTemplateinterface.getOperations());
                }
            });
        }
    }
    
    public void copyNodeTypeOperation2Template(Map<String, OperationDefinition> nodeTypeOperations,
            Map<String, OperationAssignment> nodeTemplateOperations) {
        if (nodeTypeOperations != null && nodeTemplateOperations != null) {
            
            nodeTemplateOperations.forEach((operationKey, nodeTemplateOperation) -> {
                OperationDefinition nodeTypeInterfaceOperation = nodeTypeOperations.get(operationKey);
                if (nodeTypeInterfaceOperation != null && nodeTemplateOperation != null) {
                    logger.info("Checking Operation Type Content : " + operationKey + " : "
                            + TransformationUtils.getJson(nodeTypeInterfaceOperation));
                }
            });
        }
    }
    
}
