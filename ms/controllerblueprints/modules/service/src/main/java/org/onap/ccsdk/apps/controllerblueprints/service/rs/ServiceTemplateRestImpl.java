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

package org.onap.ccsdk.apps.controllerblueprints.service.rs;


import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.ResourceAssignment;
import org.onap.ccsdk.apps.controllerblueprints.service.ServiceTemplateService;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModelContent;
import org.onap.ccsdk.apps.controllerblueprints.service.model.AutoMapResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@inheritDoc}
 */
@Service
public class ServiceTemplateRestImpl implements ServiceTemplateRest {

    private ServiceTemplateService serviceTemplateService;

    /**
     * This is a ServiceTemplateRestImpl constructor
     *
     * @param serviceTemplateService Service Template Service
     */
    public ServiceTemplateRestImpl(ServiceTemplateService serviceTemplateService) {
        this.serviceTemplateService = serviceTemplateService;
    }

    @Override
    public ServiceTemplate enrichServiceTemplate(ServiceTemplate serviceTemplate) throws BluePrintException {
        try {
            return serviceTemplateService.enrichServiceTemplate(serviceTemplate);
        } catch (Exception e) {
            throw new BluePrintException(3500, e.getMessage(), e);
        }
    }

    @Override
    public ServiceTemplate validateServiceTemplate(ServiceTemplate serviceTemplate) throws BluePrintException {
        try {
            return serviceTemplateService.validateServiceTemplate(serviceTemplate);
        } catch (Exception e) {
            throw new BluePrintException(3501, e.getMessage(), e);
        }
    }

    @Override
    public AutoMapResponse autoMap(List<ResourceAssignment> resourceAssignments) throws BluePrintException {
        try {
            return serviceTemplateService.autoMap(resourceAssignments);
        } catch (Exception e) {
            throw new BluePrintException(3502, e.getMessage(), e);
        }
    }

    @Override
    public List<ResourceAssignment> validateResourceAssignments(List<ResourceAssignment> resourceAssignments)
            throws BluePrintException {
        try {
            return serviceTemplateService.validateResourceAssignments(resourceAssignments);
        } catch (Exception e) {
            throw new BluePrintException(3503, e.getMessage(), e);
        }
    }

    @Override
    public List<ResourceAssignment> generateResourceAssignments(ConfigModelContent templateContent)
            throws BluePrintException {
        try {
            return serviceTemplateService.generateResourceAssignments(templateContent);
        } catch (Exception e) {
            throw new BluePrintException(3504, e.getMessage(), e);
        }
    }

}
