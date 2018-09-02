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
import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.generator.service.ConfigGeneratorService;
import org.onap.ccsdk.config.model.data.ResourceAssignment;
import org.onap.ccsdk.config.model.service.ComponentNodeService;
import org.onap.ccsdk.config.model.service.ConfigModelService;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigAssignmentServiceImpl implements ConfigAssignmentService {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigAssignmentServiceImpl.class);
    
    private ComponentNodeService componentNodeService;
    private ConfigResourceService configResourceService;
    private ConfigModelService configModelService;
    private ConfigRestAdaptorService configRestAdaptorService;
    private ConfigGeneratorService configGeneratorService;
    
    private static final String CLASS_NAME = "ConfigAssignmentServiceImpl";
    
    public ConfigAssignmentServiceImpl(ConfigResourceService configResourceService,
            ConfigRestAdaptorService configRestAdaptorService, ConfigModelService configModelService,
            ComponentNodeService componentNodeService, ConfigGeneratorService configGeneratorService) {
        logger.info("{} Constuctor Initated...", CLASS_NAME);
        this.componentNodeService = componentNodeService;
        this.configResourceService = configResourceService;
        this.configModelService = configModelService;
        this.configRestAdaptorService = configRestAdaptorService;
        this.configGeneratorService = configGeneratorService;
    }
    
    @Override
    public void resolveResources(ResourceAssignmentData resourceAssignmentData) throws SvcLogicException {
        ConfigAssignmentProcessService configAssignmentProcessService =
                new ConfigAssignmentProcessService(configResourceService, configRestAdaptorService, configModelService,
                        componentNodeService, configGeneratorService);
        configAssignmentProcessService.resolveResources(resourceAssignmentData);
    }
    
    @Override
    public void saveResourceMapping(ResourceAssignmentData resourceAssignmentData, String templateName,
            List<ResourceAssignment> resourceAssignments) throws SvcLogicException {
        ConfigAssignmentPersistService configAssignmentPersistService =
                new ConfigAssignmentPersistService(configResourceService);
        configAssignmentPersistService.saveResourceMapping(resourceAssignmentData, templateName, resourceAssignments);
    }
    
    @Override
    public ResourceAssignmentData generateTemplateResourceMash(ResourceAssignmentData resourceAssignmentData)
            throws SvcLogicException {
        ConfigPreviewService configPreviewService =
                new ConfigPreviewService(configResourceService, configModelService, configGeneratorService);
        return configPreviewService.generateTemplateResourceMash(resourceAssignmentData);
    }
    
}
