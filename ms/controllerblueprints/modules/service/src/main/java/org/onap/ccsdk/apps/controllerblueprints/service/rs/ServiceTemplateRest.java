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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@inheritDoc}
 */
@RestController
@RequestMapping(value = "/api/v1/service-template")
public class ServiceTemplateRest {

    private ServiceTemplateService serviceTemplateService;

    /**
     * This is a ServiceTemplateRest constructor
     *
     * @param serviceTemplateService Service Template Service
     */
    public ServiceTemplateRest(ServiceTemplateService serviceTemplateService) {
        this.serviceTemplateService = serviceTemplateService;
    }

    @PostMapping(path = "/enrich", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ServiceTemplate enrichServiceTemplate(@RequestBody ServiceTemplate serviceTemplate) throws BluePrintException {
        try {
            return serviceTemplateService.enrichServiceTemplate(serviceTemplate);
        } catch (Exception e) {
            throw new BluePrintException(3500, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/validate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    ServiceTemplate validateServiceTemplate(@RequestBody ServiceTemplate serviceTemplate) throws BluePrintException {
        try {
            return serviceTemplateService.validateServiceTemplate(serviceTemplate);
        } catch (Exception e) {
            throw new BluePrintException(3501, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/resource-assignment/auto-map", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    AutoMapResponse autoMap(@RequestBody List<ResourceAssignment> resourceAssignments) throws BluePrintException {
        try {
            return serviceTemplateService.autoMap(resourceAssignments);
        } catch (Exception e) {
            throw new BluePrintException(3502, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/resource-assignment/validate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ResourceAssignment> validateResourceAssignments(@RequestBody List<ResourceAssignment> resourceAssignments)
            throws BluePrintException {
        try {
            return serviceTemplateService.validateResourceAssignments(resourceAssignments);
        } catch (Exception e) {
            throw new BluePrintException(3503, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/resource-assignment/generate", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<ResourceAssignment> generateResourceAssignments(@RequestBody ConfigModelContent templateContent)
            throws BluePrintException {
        try {
            return serviceTemplateService.generateResourceAssignments(templateContent);
        } catch (Exception e) {
            throw new BluePrintException(3504, e.getMessage(), e);
        }
    }

}
