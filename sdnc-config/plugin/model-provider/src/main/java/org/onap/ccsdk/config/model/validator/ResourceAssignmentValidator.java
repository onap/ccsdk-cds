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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.data.CapabilityAssignment;
import org.onap.ccsdk.config.model.data.NodeTemplate;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.utils.TopologicalSortingUtils;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ResourceAssignmentValidator {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceAssignmentValidator.class);
    private List<ResourceAssignment> assignments;
    private Map<String, ResourceAssignment> resourceAssignmentMap = new HashMap<>();
    private StringBuilder validationMessage = new StringBuilder();
    
    public ResourceAssignmentValidator(NodeTemplate nodeTemplate) throws ConfigModelException {
        
        if (nodeTemplate != null && nodeTemplate.getCapabilities() != null) {
            CapabilityAssignment capabilityAssignment =
                    nodeTemplate.getCapabilities().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);
            if (capabilityAssignment != null && capabilityAssignment.getProperties() != null) {
                Object mappingObject =
                        capabilityAssignment.getProperties().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);
                if (mappingObject != null) {
                    String mappingContent = TransformationUtils.getJson(mappingObject);
                    if (StringUtils.isNotBlank(mappingContent)) {
                        this.assignments =
                                TransformationUtils.getListfromJson(mappingContent, ResourceAssignment.class);
                    } else {
                        validationMessage
                                .append(String.format("Failed to transform Mapping Content (%s) ", mappingContent));
                        throw new ConfigModelException(
                                String.format("Failed to transform Mapping Content (%s) ", mappingContent));
                    }
                }
            }
        }
    }
    
    public ResourceAssignmentValidator(List<ResourceAssignment> assignments) {
        this.assignments = assignments;
    }
    
    /**
     * This is a validateResourceAssignment to validate the Topology Template
     *
     * @return boolean
     * @throws ConfigModelException
     */
    public boolean validateResourceAssignment() throws ConfigModelException {
        if (assignments != null && !assignments.isEmpty()) {
            validateDuplicateDictionaryKeys();
            validateCyclicDependencty();
            if (validationMessage.length() > 0) {
                logger.error("Resourece Assignment Validation : {}", validationMessage);
                throw new ConfigModelException("Resourece Assignment Validation :" + validationMessage.toString());
            }
        }
        return true;
    }
    
    @SuppressWarnings("squid:S3776")
    private void validateDuplicateDictionaryKeys() {
        this.assignments.forEach(resourceMapping -> {
            if (resourceMapping != null) {
                if (!resourceAssignmentMap.containsKey(resourceMapping.getName())) {
                    resourceAssignmentMap.put(resourceMapping.getName(), resourceMapping);
                } else {
                    validationMessage.append(String.format("Duplicate Assignment Template Key (%s) is Present",
                            resourceMapping.getName()));
                }
            }
        });
        
        if (!assignments.isEmpty()) {
            Set<String> uniqueSet = new HashSet<>();
            for (ResourceAssignment resourceAssignment : assignments) {
                if (resourceAssignment != null) {
                    boolean added = uniqueSet.add(resourceAssignment.getDictionaryName());
                    if (!added) {
                        validationMessage.append(
                                String.format("Duplicate Assignment Dictionary Key (%s) present with Template Key (%s)",
                                        resourceAssignment.getDictionaryName(), resourceAssignment.getName()));
                    }
                }
            }
        }
    }
    
    private void validateCyclicDependencty() {
        TopologicalSortingUtils<ResourceAssignment> topologySorting = new TopologicalSortingUtils<>();
        this.resourceAssignmentMap.forEach((mappingKey, mapping) -> {
            if (mapping != null) {
                if (mapping.getDependencies() != null && !mapping.getDependencies().isEmpty()) {
                    for (String dependency : mapping.getDependencies()) {
                        topologySorting.add(resourceAssignmentMap.get(dependency), mapping);
                    }
                } else {
                    topologySorting.add(null, mapping);
                }
            }
        });
        
        if (!topologySorting.isDag()) {
            String graph = getTopologicalGraph(topologySorting);
            validationMessage.append("Cyclic Dependency :" + graph);
        }
    }
    
    public String getTopologicalGraph(TopologicalSortingUtils<ResourceAssignment> topologySorting) {
        StringBuilder s = new StringBuilder();
        if (topologySorting != null) {
            Map<ResourceAssignment, List<ResourceAssignment>> neighbors = topologySorting.getNeighbors();
            
            neighbors.forEach((v, vs) -> {
                if (v == null) {
                    s.append("\n    * -> [");
                    List<ResourceAssignment> links = vs;
                    for (ResourceAssignment resourceAssignment : links) {
                        s.append("(" + resourceAssignment.getDictionaryName() + ":" + resourceAssignment.getName()
                                + "),");
                    }
                    s.append("]");
                } else {
                    s.append("\n    (" + v.getDictionaryName() + ":" + v.getName() + ") -> [");
                    List<ResourceAssignment> links = vs;
                    for (ResourceAssignment resourceAssignment : links) {
                        s.append("(" + resourceAssignment.getDictionaryName() + ":" + resourceAssignment.getName()
                                + "),");
                    }
                    s.append("]");
                }
            });
        }
        return s.toString();
    }
}
