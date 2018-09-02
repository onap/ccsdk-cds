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

package org.onap.ccsdk.config.model.validator;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.CapabilityDefinition;
import org.onap.ccsdk.config.model.data.DataType;
import org.onap.ccsdk.config.model.data.InterfaceDefinition;
import org.onap.ccsdk.config.model.data.NodeType;
import org.onap.ccsdk.config.model.data.OperationDefinition;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.config.model.data.RequirementDefinition;
import org.onap.ccsdk.config.model.data.ServiceTemplate;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * NodeTypeValidator.java Purpose: Provide Configuration Generator NodeTypeValidator
 *
 * @version 1.0
 */
public class NodeTypeValidator {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(NodeTypeValidator.class);
    private StringBuilder message;
    private Map<String, DataType> stDataTypes;
    private Map<String, NodeType> stNodeTypes;
    private ServiceTemplate serviceTemplate;
    private PropertyDefinitionValidator propertyDefinitionValidator;
    
    /**
     * This is a NodeTypeValidator
     *
     * @param serviceTemplate
     * @throws ConfigModelException
     */
    public NodeTypeValidator(ServiceTemplate serviceTemplate, StringBuilder message) throws ConfigModelException {
        this.serviceTemplate = serviceTemplate;
        this.message = message;
        propertyDefinitionValidator = new PropertyDefinitionValidator(this.message);
        stDataTypes = new HashMap<>();
        stNodeTypes = new HashMap<>();
        loadInitial();
        
    }
    
    private void loadInitial() {
        if (serviceTemplate != null) {
            
            if (serviceTemplate.getDataTypes() != null) {
                serviceTemplate.getDataTypes().forEach((dataTypeKey, dataType) -> {
                    stDataTypes.put(dataTypeKey, dataType);
                    logger.trace("Data Type ({}) loaded successfully.", dataTypeKey);
                });
            }
            
            if (serviceTemplate.getNodeTypes() != null) {
                serviceTemplate.getNodeTypes().forEach((nodeTypeKey, nodeType) -> {
                    stNodeTypes.put(nodeTypeKey, nodeType);
                    logger.trace("NodeType Type ({}) loaded successfully.", nodeTypeKey);
                });
            }
            
        }
        
    }
    
    /**
     * This is a validateNodeTypes to validate the Node Type
     *
     * @return boolean
     * @throws ConfigModelException
     */
    @SuppressWarnings({"squid:S00112", "squid:S3776"})
    public boolean validateNodeTypes() {
        if (serviceTemplate != null && serviceTemplate.getNodeTypes() != null) {
            serviceTemplate.getNodeTypes().forEach((nodeTypeKey, nodeType) -> {
                if (nodeType != null) {
                    message.append("\n ***** Validation Node Type  (" + nodeTypeKey + "), derived from ("
                            + nodeType.getDerivedFrom() + ")");
                    try {
                        validateNodeType(ConfigModelConstant.MODEL_DEFINITION_TYPE_NODE_TYPE,
                                nodeType.getDerivedFrom());
                        
                        if (nodeType.getProperties() != null) {
                            checkValidProperties(nodeType.getProperties());
                        }
                        
                        if (nodeType.getCapabilities() != null) {
                            validateNodeTypeCapabilities(nodeType.getCapabilities());
                        }
                        
                        if (nodeType.getInterfaces() != null) {
                            validateNodeTypeInterface(nodeType.getInterfaces());
                        }
                        
                        if (nodeType.getRequirements() != null) {
                            validateNodeTypeRequirement(nodeType.getRequirements());
                        }
                        
                    } catch (ConfigModelException e) {
                        logger.error(e.getMessage());
                        throw new RuntimeException(e.getMessage());
                    }
                    
                }
            });
        }
        return true;
    }
    
    private boolean validateNodeType(String definitionType, String derivedFrom) throws ConfigModelException {
        boolean valid = true;
        if (!ConfigModelConstant.MODEL_DEFINITION_TYPE_DATA_TYPE.equalsIgnoreCase(definitionType)
                && !ValidTypes.getValidNodeTypes().contains(derivedFrom)) {
            throw new ConfigModelException("Not Valid Model Type (" + derivedFrom + ")");
        }
        return valid;
    }
    
    private boolean checkValidProperties(Map<String, PropertyDefinition> properties) {
        if (properties != null) {
            propertyDefinitionValidator.validatePropertyDefinition(stDataTypes, properties);
        }
        return true;
    }
    
    @SuppressWarnings("squid:S00112")
    private boolean validateNodeTypeCapabilities(Map<String, CapabilityDefinition> capabilities) {
        if (capabilities != null) {
            capabilities.forEach((capabilityKey, capability) -> {
                if (capability != null) {
                    Map<String, PropertyDefinition> properties = capability.getProperties();
                    message.append("\n Validation Capability (" + capabilityKey + ") properties :");
                    propertyDefinitionValidator.validatePropertyDefinition(stDataTypes, properties);
                }
            });
        }
        return true;
    }
    
    @SuppressWarnings("squid:S00112")
    private boolean validateNodeTypeInterface(Map<String, InterfaceDefinition> interfaces) {
        if (interfaces != null) {
            interfaces.forEach((interfaceKey, interfaceDefinition) -> {
                if (interfaceDefinition != null && interfaceDefinition.getOperations() != null) {
                    validateNodeTypeInterfaceOperation(interfaceDefinition.getOperations());
                }
            });
        }
        return true;
    }
    
    @SuppressWarnings("squid:S00112")
    private boolean validateNodeTypeInterfaceOperation(Map<String, OperationDefinition> operations) {
        if (operations != null) {
            operations.forEach((operationKey, operation) -> {
                if (operation != null) {
                    Map<String, PropertyDefinition> inputs = operation.getInputs();
                    message.append("\n Validation Operation (" + operationKey + ") Inputs :");
                    propertyDefinitionValidator.validatePropertyDefinition(stDataTypes, inputs);
                    message.append("\n Validation Operation (" + operationKey + ") output :");
                    Map<String, PropertyDefinition> outputs = operation.getOutputs();
                    propertyDefinitionValidator.validatePropertyDefinition(stDataTypes, outputs);
                }
            });
        }
        return true;
    }
    
    @SuppressWarnings("squid:S00112")
    private boolean validateNodeTypeRequirement(Map<String, RequirementDefinition> requirements) {
        if (requirements != null) {
            requirements.forEach((requirementKey, requirement) -> {
                if (requirement != null) {
                    String nodeTypeName = requirement.getNode();
                    String capabilityName = requirement.getCapability();
                    try {
                        checkCapabilityPresentInNodeType(nodeTypeName, capabilityName);
                    } catch (ConfigModelException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        return true;
    }
    
    private boolean checkCapabilityPresentInNodeType(String nodeTypeName, String capabilityName)
            throws ConfigModelException {
        if (StringUtils.isNotBlank(nodeTypeName) && StringUtils.isNotBlank(capabilityName)) {
            
            if (!stNodeTypes.containsKey(nodeTypeName)) {
                throw new ConfigModelException(nodeTypeName + " Node Type not Defined.");
            } else {
                message.append("\n Node Type  (" + nodeTypeName + ") Defined.");
            }
            
            NodeType relationalNodeType = stNodeTypes.get(nodeTypeName);
            
            if (relationalNodeType.getCapabilities() == null) {
                throw new ConfigModelException(
                        "Node Type  (" + nodeTypeName + "), doesn't have Capability Definitions.");
            }
            
            if (!relationalNodeType.getCapabilities().containsKey(capabilityName)) {
                throw new ConfigModelException("Node Type (" + nodeTypeName + ") doesn't have  (" + capabilityName
                        + ") Capability Definitions.");
            } else {
                message.append(
                        "\n Node Type (" + nodeTypeName + ") has (" + capabilityName + ") Capability Definitions.");
            }
            
        }
        return true;
    }
    
}
