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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.data.CapabilityAssignment;
import org.onap.ccsdk.config.model.data.CapabilityDefinition;
import org.onap.ccsdk.config.model.data.InterfaceAssignment;
import org.onap.ccsdk.config.model.data.InterfaceDefinition;
import org.onap.ccsdk.config.model.data.NodeTemplate;
import org.onap.ccsdk.config.model.data.NodeType;
import org.onap.ccsdk.config.model.data.OperationAssignment;
import org.onap.ccsdk.config.model.data.OperationDefinition;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class NodePropertyUtils {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(NodePropertyUtils.class);
    private final SvcLogicContext context;
    private final Map<String, String> inParams;
    private ExpressionUtils jsonExpressionUtils;
    
    public NodePropertyUtils(final SvcLogicContext context, final Map<String, String> inParams) {
        this.context = context;
        this.inParams = inParams;
        jsonExpressionUtils = new ExpressionUtils(this.context, this.inParams);
    }
    
    @SuppressWarnings("squid:S3776")
    public SvcLogicContext assignInParamsFromModel(NodeType nodetype, NodeTemplate nodeTemplate) throws IOException {
        if (nodeTemplate != null) {
            Map<String, Object> inputs = null;
            // Populate the Type inputs
            Map<String, PropertyDefinition> nodeTypeinputs = new HashMap<>();
            for (Map.Entry<String, InterfaceDefinition> nodeTypeInterface : nodetype.getInterfaces().entrySet()) {
                if (nodeTypeInterface != null && nodeTypeInterface.getValue() != null) {
                    for (Map.Entry<String, OperationDefinition> nodeTypeOperation : nodeTypeInterface.getValue()
                            .getOperations().entrySet()) {
                        nodeTypeinputs = nodeTypeOperation.getValue().getInputs();
                        logger.trace("Populated node type input ({}).", nodeTypeinputs);
                    }
                }
            }
            
            if (!nodeTypeinputs.isEmpty()) {
                // Populate the Template inputs
                for (Map.Entry<String, InterfaceAssignment> nodeTemplateInterface : nodeTemplate.getInterfaces()
                        .entrySet()) {
                    if (nodeTemplateInterface != null && nodeTemplateInterface.getValue() != null) {
                        this.inParams.put(ConfigModelConstant.PROPERTY_CURRENT_INTERFACE,
                                nodeTemplateInterface.getKey());
                        for (Map.Entry<String, OperationAssignment> nodeTemplateOperation : nodeTemplateInterface
                                .getValue().getOperations().entrySet()) {
                            if (nodeTemplateOperation != null) {
                                this.inParams.put(ConfigModelConstant.PROPERTY_CURRENT_OPERATION,
                                        nodeTemplateOperation.getKey());
                                if (StringUtils.isNotBlank(nodeTemplateOperation.getValue().getImplementation())) {
                                    this.inParams.put(ConfigModelConstant.PROPERTY_CURRENT_IMPLEMENTATION,
                                            nodeTemplateOperation.getValue().getImplementation());
                                }
                                inputs = nodeTemplateOperation.getValue().getInputs();
                                logger.trace("Populated node template input({}).", inputs);
                                // Assign Default Values & Types
                                jsonExpressionUtils.populatePropertAssignments(nodeTypeinputs, inputs);
                            }
                        }
                    }
                }
            }
            
            if (nodeTemplate.getRequirements() != null && !nodeTemplate.getRequirements().isEmpty()) {
                assignInParamsFromRequirementModel(nodeTemplate, context, inParams);
            }
        }
        return context;
    }
    
    @SuppressWarnings("squid:S3776")
    public SvcLogicContext assignOutParamsFromModel(NodeType nodetype, NodeTemplate nodeTemplate) throws IOException {
        
        if (nodeTemplate != null) {
            Map<String, Object> outputs = null;
            // Populate the Type Outputs
            Map<String, PropertyDefinition> nodeTypeOutputs = new HashMap<>();
            
            for (Map.Entry<String, InterfaceDefinition> nodeTypeInterface : nodetype.getInterfaces().entrySet()) {
                if (nodeTypeInterface != null && nodeTypeInterface.getValue() != null) {
                    for (Map.Entry<String, OperationDefinition> nodeTypeOperation : nodeTypeInterface.getValue()
                            .getOperations().entrySet()) {
                        nodeTypeOutputs = nodeTypeOperation.getValue().getOutputs();
                        logger.info("Populated node type output ({}).", nodeTypeOutputs);
                    }
                }
            }
            
            if (!nodeTypeOutputs.isEmpty()) {
                // Populate the Template Outputs
                for (Map.Entry<String, InterfaceAssignment> nodeTemplateInterface : nodeTemplate.getInterfaces()
                        .entrySet()) {
                    if (nodeTemplateInterface != null && nodeTemplateInterface.getValue() != null) {
                        for (Map.Entry<String, OperationAssignment> nodeTemplateOperation : nodeTemplateInterface
                                .getValue().getOperations().entrySet()) {
                            outputs = nodeTemplateOperation.getValue().getOutputs();
                            logger.info("Populated node template output ({}).", outputs);
                            // Assign Default Values & Types
                            jsonExpressionUtils.populateOutPropertAssignments(nodeTypeOutputs, outputs);
                            // TO DO
                        }
                    }
                }
            }
        }
        return context;
    }
    
    @SuppressWarnings({"squid:S3776", "squid:S00112"})
    public SvcLogicContext assignInParamsFromRequirementModel(NodeTemplate nodeTemplate, final SvcLogicContext context,
            final Map<String, String> inParams) {
        if (nodeTemplate != null && nodeTemplate.getRequirements() != null && context != null && inParams != null) {
            nodeTemplate.getRequirements().forEach((requrementKey, requirement) -> {
                if (requirement != null && StringUtils.isNotBlank(requirement.getNode())
                        && StringUtils.isNotBlank(requirement.getCapability())) {
                    String requirementNodeTemplateContent = context
                            .getAttribute(ConfigModelConstant.PROPERTY_NODE_TEMPLATES_DOT + requirement.getNode());
                    logger.info("Processing requirement node ({}) template content ({}) ", requirement.getNode(),
                            requirementNodeTemplateContent);
                    NodeTemplate requirementNodeTemplate =
                            TransformationUtils.readValue(requirementNodeTemplateContent, NodeTemplate.class);
                    if (requirementNodeTemplate != null && requirementNodeTemplate.getCapabilities() != null) {
                        String nodeTypeContent = context.getAttribute(
                                ConfigModelConstant.PROPERTY_NODE_TYPES_DOT + requirementNodeTemplate.getType());
                        NodeType nodeType = TransformationUtils.readValue(nodeTypeContent, NodeType.class);
                        if (nodeType != null && nodeType.getCapabilities() != null
                                && nodeType.getCapabilities().containsKey(requirement.getCapability())) {
                            CapabilityDefinition capabilityDefinition =
                                    nodeType.getCapabilities().get(requirement.getCapability());
                            if (capabilityDefinition != null) {
                                CapabilityAssignment capabilityAssignment =
                                        requirementNodeTemplate.getCapabilities().get(requirement.getCapability());
                                try {
                                    assignInParamsFromCapabilityModel(requrementKey, capabilityDefinition,
                                            capabilityAssignment, context);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    }
                }
            });
        }
        return context;
    }
    
    public SvcLogicContext assignInParamsFromCapabilityModel(String requirementName,
            CapabilityDefinition capabilityDefinition, CapabilityAssignment capabilityAssignment,
            final SvcLogicContext context) throws IOException {
        if (capabilityAssignment != null && capabilityAssignment.getProperties() != null) {
            jsonExpressionUtils.populatePropertAssignmentsWithPrefix(requirementName,
                    capabilityDefinition.getProperties(), capabilityAssignment.getProperties());
        }
        return context;
    }
    
}
