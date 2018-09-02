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

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.utils.ResourceAssignmentUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class InputResourceProcessor implements ComponentNode {
    
    public InputResourceProcessor(ConfigResourceService configResourceService) {}
    
    @Override
    public Boolean preCondition(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        return Boolean.TRUE;
    }
    
    @Override
    public void preProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        // Auto-generated method stub
    }
    
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        // Auto-generated method stub
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void process(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        try {
            List<ResourceAssignment> batchResourceAssignment =
                    (List<ResourceAssignment>) componentContext.get(ConfigModelConstant.PROPERTY_RESOURCE_ASSIGNMENTS);
            if (batchResourceAssignment != null && !batchResourceAssignment.isEmpty()) {
                for (ResourceAssignment resourceAssignment : batchResourceAssignment) {
                    processResourceAssignment(ctx, componentContext, resourceAssignment);
                }
            }
        } catch (Exception e) {
            throw new SvcLogicException(String.format("InputResourceProcessor Exception : (%s)", e), e);
        }
    }
    
    private void processResourceAssignment(SvcLogicContext ctx, Map<String, Object> componentContext,
            ResourceAssignment resourceAssignment) throws ConfigModelException, SvcLogicException {
        try {
            if (StringUtils.isNotBlank(resourceAssignment.getName())) {
                String value = ctx.getAttribute(resourceAssignment.getName());
                // if value is null don't call setResourceDataValue to populate the value
                if (StringUtils.isNotBlank(value)) {
                    ResourceAssignmentUtils.setResourceDataValue(componentContext, resourceAssignment, value);
                }
            }
            
            // Check the value has populated for mandatory case
            ResourceAssignmentUtils.assertTemplateKeyValueNotNull(componentContext, resourceAssignment);
        } catch (Exception e) {
            ResourceAssignmentUtils.setFailedResourceDataValue(componentContext, resourceAssignment, e.getMessage());
            throw new SvcLogicException(
                    String.format("Failed in template key (%s) assignments with : (%s)", resourceAssignment, e), e);
        }
    }
    
    @Override
    public void postProcess(Map<String, String> inParams, SvcLogicContext ctx, Map<String, Object> componentContext)
            throws SvcLogicException {
        // Auto-generated method stub
    }
    
}
