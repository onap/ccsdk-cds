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

package org.onap.ccsdk.apps.controllerblueprints.service;

import com.google.common.base.Preconditions;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.DataType;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.common.SwaggerGenerator;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.HashMap;
import java.util.Map;

/**
 * SchemaGeneratorService.java Purpose: Provide Service to generate service template input schema definition and Sample
 * Json generation.
 *
 * @author Brinda Santh
 * @version 1.0
 */

public class SchemaGeneratorService {
    private static EELFLogger log = EELFManager.getInstance().getLogger(SchemaGeneratorService.class);

    private Map<String, DataType> dataTypes;

    /**
     * This is a SchemaGeneratorService constructor
     */
    public SchemaGeneratorService() {
        dataTypes = new HashMap<>();
    }

    /**
     * This is a generateSchema
     *
     * @param serviceTemplateContent service template content
     * @return String
     * @throws BluePrintException Blueprint Exception
     */
    public String generateSchema(String serviceTemplateContent) throws BluePrintException {
        if (StringUtils.isNotBlank(serviceTemplateContent)) {
            ServiceTemplate serviceTemplate = JacksonUtils.readValue(serviceTemplateContent,
                    ServiceTemplate.class);
            return generateSchema(serviceTemplate);
        } else {
            throw new BluePrintException(
                    "Service Template Content is  (" + serviceTemplateContent + ") not Defined.");
        }
    }

    /**
     * This is a generateSchema
     *
     * @param serviceTemplate service template content
     * @return String
     * @throws BluePrintException Blueprint Exception
     */
    public String generateSchema(ServiceTemplate serviceTemplate) throws BluePrintException {
        String schemaContent = null;
        Preconditions.checkNotNull(serviceTemplate, "Service Template is not defined.");
        try {
            if (serviceTemplate.getTopologyTemplate() != null
                    && serviceTemplate.getTopologyTemplate().getInputs() != null) {
                SwaggerGenerator swaggerGenerator = new SwaggerGenerator(serviceTemplate);
                schemaContent = swaggerGenerator.generateSwagger();
            }
        } catch (Exception e) {
            throw new BluePrintException(e.getMessage(), e);
        }

        return schemaContent;
    }

    private void manageServiceTemplateActions(ServiceTemplate serviceTemplate, String actionName) {
        if (serviceTemplate != null && serviceTemplate.getTopologyTemplate() != null
                && StringUtils.isNotBlank(actionName)) {

            if (MapUtils.isNotEmpty(serviceTemplate.getTopologyTemplate().getInputs())) {

                serviceTemplate.getTopologyTemplate().getInputs().entrySet().removeIf(entity -> {
                    String keyName = entity.getKey();
                    String replacedAction = actionName.replace("-action", "-request");
                    log.debug("Key name : " + keyName + ", actionName "
                            + actionName + ", replacedAction :" + replacedAction);
                    if (keyName.endsWith("-request") && !keyName.equals(replacedAction)) {
                        log.info("deleting input property {} ", keyName);
                        return true;
                    }
                    return false;
                });
            }

        }
    }

}
