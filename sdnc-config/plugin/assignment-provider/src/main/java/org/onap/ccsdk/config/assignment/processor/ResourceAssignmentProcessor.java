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

package org.onap.ccsdk.config.assignment.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.utils.TopologicalSortingUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ResourceAssignmentProcessor {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ResourceAssignmentProcessor.class);
    
    private List<ResourceAssignment> assignments;
    private Map<String, ResourceAssignment> resourceAssignmentMap;
    
    @SuppressWarnings("squid:S1172")
    public ResourceAssignmentProcessor(List<ResourceAssignment> assignments, SvcLogicContext ctx) {
        this.assignments = assignments;
        this.resourceAssignmentMap = new HashMap<>();
    }
    
    @SuppressWarnings("squid:S3776")
    public List<List<ResourceAssignment>> process() {
        List<List<ResourceAssignment>> sequenceBatchResourceAssignment = new ArrayList<>();
        if (this.assignments != null) {
            logger.info("Assignments ({})", this.assignments);
            this.assignments.forEach(resourceMapping -> {
                if (resourceMapping != null) {
                    logger.trace("Processing Key ({})", resourceMapping.getName());
                    resourceAssignmentMap.put(resourceMapping.getName(), resourceMapping);
                }
            });
            
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
            
            List<ResourceAssignment> sequencedResourceAssignments = topologySorting.topSort();
            logger.info("Sorted Sequenced Assignments ({})", sequencedResourceAssignments);
            
            List<ResourceAssignment> batchResourceAssignment = null;
            List<String> batchAssignmentName = null;
            for (int i = 0; i < sequencedResourceAssignments.size(); i++) {
                ResourceAssignment resourceAssignment = sequencedResourceAssignments.get(i);
                ResourceAssignment previousResourceAssignment = null;
                
                if (i > 0) {
                    previousResourceAssignment = sequencedResourceAssignments.get(i - 1);
                }
                if (resourceAssignment != null) {
                    
                    boolean dependencyPresence = false;
                    if (batchAssignmentName != null && resourceAssignment.getDependencies() != null) {
                        dependencyPresence =
                                CollectionUtils.containsAny(batchAssignmentName, resourceAssignment.getDependencies());
                    }
                    
                    logger.trace("({}) -> Checking ({}), with ({}), result ({})", resourceAssignment.getName(),
                            batchAssignmentName, resourceAssignment.getDependencies(), dependencyPresence);
                    
                    if (previousResourceAssignment != null && resourceAssignment.getDictionarySource() != null
                            && resourceAssignment.getDictionarySource()
                                    .equalsIgnoreCase(previousResourceAssignment.getDictionarySource())
                            && !dependencyPresence) {
                        batchResourceAssignment.add(resourceAssignment);
                        batchAssignmentName.add(resourceAssignment.getName());
                    } else {
                        if (batchResourceAssignment != null) {
                            sequenceBatchResourceAssignment.add(batchResourceAssignment);
                            logger.trace("Created old Set ({})", batchAssignmentName);
                        }
                        batchResourceAssignment = new ArrayList<>();
                        batchResourceAssignment.add(resourceAssignment);
                        
                        batchAssignmentName = new ArrayList<>();
                        batchAssignmentName.add(resourceAssignment.getName());
                    }
                }
                
                if (i == (sequencedResourceAssignments.size() - 1)) {
                    logger.trace("Created old Set ({})", batchAssignmentName);
                    sequenceBatchResourceAssignment.add(batchResourceAssignment);
                }
            }
            logger.info("Batched Sequence : ({})", sequenceBatchResourceAssignment);
        }
        return sequenceBatchResourceAssignment;
    }
    
}
