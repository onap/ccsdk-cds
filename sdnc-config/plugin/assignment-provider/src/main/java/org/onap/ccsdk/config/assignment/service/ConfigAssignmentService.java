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

package org.onap.ccsdk.config.assignment.service;

import java.util.List;
import org.onap.ccsdk.config.assignment.data.ResourceAssignmentData;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface ConfigAssignmentService {
    
    public void resolveResources(ResourceAssignmentData resourceAssignmentData) throws SvcLogicException;
    
    public void saveResourceMapping(ResourceAssignmentData resourceAssignmentData, String templateName,
            List<ResourceAssignment> resourceAssignments) throws SvcLogicException;
    
    public ResourceAssignmentData generateTemplateResourceMash(ResourceAssignmentData resourceAssignmentData)
            throws SvcLogicException;
    
}
