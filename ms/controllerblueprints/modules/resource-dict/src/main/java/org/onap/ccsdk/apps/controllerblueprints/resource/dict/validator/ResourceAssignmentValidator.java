/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.resource.dict.validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrBuilder;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.ConfigModelConstant;
import org.onap.ccsdk.apps.controllerblueprints.core.data.CapabilityAssignment;
import org.onap.ccsdk.apps.controllerblueprints.core.data.NodeTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.TopologicalSortingUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
/**
 *
 * ResourceAssignmentValidator.java Purpose:
 * @author Brinda Santh
 */
public class ResourceAssignmentValidator {
    private static final Logger log = LoggerFactory.getLogger(ResourceAssignmentValidator.class);
    private List<ResourceAssignment> assignments;
    private Map<String, ResourceAssignment> resourceAssignmentMap = new HashMap();
    private StrBuilder validationMessage = new StrBuilder();

    public ResourceAssignmentValidator(List<ResourceAssignment> assignments) {
        this.assignments = assignments;
    }

    public ResourceAssignmentValidator(NodeTemplate nodeTemplate) throws BluePrintException {

        if (nodeTemplate != null && nodeTemplate.getCapabilities() != null) {
            CapabilityAssignment capabilityAssignment =
                    nodeTemplate.getCapabilities().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);
            if (capabilityAssignment != null && capabilityAssignment.getProperties() != null) {
                Object mappingObject =
                        capabilityAssignment.getProperties().get(ConfigModelConstant.CAPABILITY_PROPERTY_MAPPING);
                if (mappingObject != null) {
                    String mappingContent = JacksonUtils.getJson(mappingObject);
                    if (StringUtils.isNotBlank(mappingContent)) {
                        this.assignments =
                                JacksonUtils.getListFromJson(mappingContent, ResourceAssignment.class);
                    } else {
                        validationMessage
                                .appendln(String.format("Failed to transform Mapping Content (%s) ", mappingContent));
                        throw new BluePrintException(
                                String.format("Failed to transform Mapping Content (%s) ", mappingContent));
                    }
                }
            }
        }
    }

    /**
     * This is a validateResourceAssignment to validate the Topology Template
     *
     * @return boolean
     * @throws BluePrintException
     */
    public boolean validateResourceAssignment() throws BluePrintException {
        if (assignments != null && !assignments.isEmpty()) {
            validateDuplicateDictionaryKeys();
            validateCyclicDependency();
            if (validationMessage.length() > 0) {
                throw new BluePrintException("Resource Assignment Validation :" + validationMessage.toString());
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
                    validationMessage.appendln(String.format("Duplicate Assignment Template Key (%s) is Present",
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
                        validationMessage.appendln(
                                String.format("Duplicate Assignment Dictionary Key (%s) present with Template Key (%s)",
                                        resourceAssignment.getDictionaryName(), resourceAssignment.getName()));
                    }
                }
            }
        }
    }

    private void validateCyclicDependency() {
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
            validationMessage.appendln("Cyclic Dependency :" + graph);
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
