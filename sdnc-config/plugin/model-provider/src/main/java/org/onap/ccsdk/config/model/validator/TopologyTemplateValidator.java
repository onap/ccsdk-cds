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
import org.onap.ccsdk.config.model.data.DataType;
import org.onap.ccsdk.config.model.data.NodeTemplate;
import org.onap.ccsdk.config.model.data.NodeType;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.config.model.data.RequirementAssignment;
import org.onap.ccsdk.config.model.data.ServiceTemplate;
import org.onap.ccsdk.config.model.data.TopologyTemplate;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * TopologyTemplateValidator.java Purpose: Provide Configuration Generator TopologyTemplateValidator
 *
 * @version 1.0
 */
public class TopologyTemplateValidator {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(TopologyTemplateValidator.class);
    private StringBuilder message;
    private Map<String, DataType> stDataTypes;
    private Map<String, NodeType> stNodeTypes;
    private Map<String, NodeTemplate> stNodeTemplates;
    private ServiceTemplate serviceTemplate;
    private PropertyDefinitionValidator propertyDefinitionValidator;
    
    /**
     * This is a TopologyTemplateValidator
     *
     * @param serviceTemplate
     * @throws ConfigModelException
     */
    public TopologyTemplateValidator(ServiceTemplate serviceTemplate, StringBuilder message) {
        this.serviceTemplate = serviceTemplate;
        this.message = message;
        propertyDefinitionValidator = new PropertyDefinitionValidator(this.message);
        stDataTypes = new HashMap<>();
        stNodeTypes = new HashMap<>();
        stNodeTemplates = new HashMap<>();
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
                    logger.trace("Node Type ({}) loaded successfully.", nodeTypeKey);
                });
            }
            
            if (serviceTemplate.getTopologyTemplate() != null) {
                TopologyTemplate topologyTemplate = serviceTemplate.getTopologyTemplate();
                
                if (topologyTemplate.getNodeTemplates() != null) {
                    topologyTemplate.getNodeTemplates().forEach((nodeTemplateKey, nodeTemplate) -> {
                        stNodeTemplates.put(nodeTemplateKey, nodeTemplate);
                        logger.trace("Node Template ({}) Type loaded successfully.", nodeTemplateKey);
                    });
                }
            }
        }
        
    }
    
    /**
     * This is a validateTopologyTemplate to validate the Topology Template
     *
     * @return boolean
     * @throws ConfigModelException
     */
    public boolean validateTopologyTemplate() {
        if (serviceTemplate != null && serviceTemplate.getTopologyTemplate() != null) {
            
            checkValidInputProperties(serviceTemplate.getTopologyTemplate().getInputs());
            
            validateNodeTemplates(serviceTemplate.getTopologyTemplate().getNodeTemplates());
        }
        return true;
    }
    
    private boolean checkValidInputProperties(Map<String, PropertyDefinition> properties) {
        if (properties != null) {
            message.append("\n Validation topology template input properties :");
            propertyDefinitionValidator.validatePropertyDefinition(stDataTypes, properties);
        }
        return true;
    }
    
    @SuppressWarnings({"squid:S00112", "squid:S3776"})
    private boolean validateNodeTemplates(Map<String, NodeTemplate> nodeTemplates) {
        if (nodeTemplates != null) {
            nodeTemplates.forEach((nodeTemplateKey, nodeTemplate) -> {
                if (nodeTemplate != null) {
                    message.append("\n ##### Validation Node Template  (" + nodeTemplateKey + "), of type ("
                            + nodeTemplate.getType() + ")");
                    
                    String nodeTypeName = nodeTemplate.getType();
                    if (!stNodeTypes.containsKey(nodeTypeName)) {
                        throw new RuntimeException("Node Type (" + nodeTypeName + ")not Defined.");
                    }
                    
                    if (nodeTemplate.getRequirements() != null) {
                        validateNodeTemplateRequirement(nodeTemplate.getRequirements());
                    }
                    
                    // Validate Resource Assignments
                    NodeType nodeType = stNodeTypes.get(nodeTypeName);
                    if (nodeType != null
                            && ConfigModelConstant.MODEL_TYPE_NODE_ARTIFACT.equals(nodeType.getDerivedFrom())) {
                        logger.info("Validating Resource Assignment NodeTemplate ({}).", nodeTemplateKey);
                        ResourceAssignmentValidator resourceAssignmentValidator;
                        try {
                            resourceAssignmentValidator = new ResourceAssignmentValidator(nodeTemplate);
                            resourceAssignmentValidator.validateResourceAssignment();
                        } catch (ConfigModelException e) {
                            throw new RuntimeException(e);
                        }
                        
                    }
                }
            });
        }
        return true;
    }
    
    @SuppressWarnings("squid:S00112")
    private boolean validateNodeTemplateRequirement(Map<String, RequirementAssignment> requirements) {
        if (requirements != null) {
            requirements.forEach((requirementKey, requirement) -> {
                if (requirement != null) {
                    String requirementnodeTypeName = requirement.getNode();
                    String capabilityName = requirement.getCapability();
                    try {
                        checkCapabilityPresentInNodeTemplate(requirementnodeTypeName, capabilityName);
                    } catch (ConfigModelException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        return true;
    }
    
    private boolean checkCapabilityPresentInNodeTemplate(String nodeTemplateName, String capabilityName)
            throws ConfigModelException {
        if (StringUtils.isNotBlank(nodeTemplateName) && StringUtils.isNotBlank(capabilityName)) {
            
            if (!stNodeTemplates.containsKey(nodeTemplateName)) {
                throw new ConfigModelException(nodeTemplateName + " Node Template not Defined.");
            } else {
                message.append("\n Node Template (" + nodeTemplateName + ") Defined.");
            }
            
            NodeTemplate relationalNodeType = stNodeTemplates.get(nodeTemplateName);
            
            if (relationalNodeType.getCapabilities() == null) {
                throw new ConfigModelException(
                        "Node Template (" + nodeTemplateName + "), doesn't have Capability Definitions.");
            }
            
            if (!relationalNodeType.getCapabilities().containsKey(capabilityName)) {
                throw new ConfigModelException("Node Type (" + nodeTemplateName + ") doesn't have  (" + capabilityName
                        + ") Capability Definitions.");
            } else {
                message.append("\n Node Template (" + nodeTemplateName + ") has (" + capabilityName
                        + ") Capability Definitions.");
            }
            
        }
        return true;
    }
}
