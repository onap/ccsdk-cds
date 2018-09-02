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

import org.onap.ccsdk.config.data.adaptor.service.ConfigResourceService;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.service.ComponentNode;
import org.onap.ccsdk.config.model.service.ComponentNodeService;
import org.onap.ccsdk.config.rest.adaptor.service.ConfigRestAdaptorService;

public class ProcessorFactory {
    
    private ConfigResourceService configResourceService;
    private ConfigRestAdaptorService configRestAdaptorService;
    private ComponentNodeService componentNodeService;
    
    public ProcessorFactory(ConfigResourceService configResourceService,
            ConfigRestAdaptorService configRestAdaptorService, ComponentNodeService componentNodeService) {
        this.componentNodeService = componentNodeService;
        this.configResourceService = configResourceService;
        this.configRestAdaptorService = configRestAdaptorService;
    }
    
    public ComponentNode getInstance(String source) {
        
        if (ConfigModelConstant.SOURCE_DEFAULT.equalsIgnoreCase(source)) {
            return new DefaultResourceProcessor(configResourceService);
        } else if (ConfigModelConstant.SOURCE_DB.equalsIgnoreCase(source)) {
            return new DBResourceProcessor(configResourceService);
        } else if (ConfigModelConstant.SOURCE_MDSAL.equalsIgnoreCase(source)) {
            return new MdsalResourceProcessor(configRestAdaptorService);
        }
        // Default
        return new InputResourceProcessor(configResourceService);
    }
    
}
