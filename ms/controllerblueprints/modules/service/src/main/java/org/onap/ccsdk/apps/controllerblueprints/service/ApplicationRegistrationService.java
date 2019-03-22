/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *  Modifications Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.service;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.collections.CollectionUtils;
import org.onap.ccsdk.apps.controllerblueprints.resource.dict.factory.ResourceSourceMappingFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
@SuppressWarnings("unused")
public class ApplicationRegistrationService {
    private static EELFLogger log = EELFManager.getInstance().getLogger(ApplicationRegistrationService.class);

    @Value("#{'${resourceSourceMappings}'.split(',')}")
    private List<String> resourceSourceMappings;

    @PostConstruct
    public void register() {
        registerDictionarySources();
    }

    public void registerDictionarySources() {
        log.info("Registering Dictionary Sources : {}", resourceSourceMappings);
        if (CollectionUtils.isNotEmpty(resourceSourceMappings)) {
            resourceSourceMappings.forEach(resourceSourceMapping -> {
                String[] mappingKeyValue = resourceSourceMapping.split("=");
                if (mappingKeyValue != null && mappingKeyValue.length == 2) {
                    ResourceSourceMappingFactory.INSTANCE.registerSourceMapping(mappingKeyValue[0].trim(), mappingKeyValue[1].trim());
                } else {
                    log.warn("failed to get resource source mapping {}", resourceSourceMapping);
                }
            });
        }
    }
}
