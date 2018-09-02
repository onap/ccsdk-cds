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

package org.onap.ccsdk.config.model.validator;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.data.ServiceTemplate;
import org.onap.ccsdk.config.model.utils.TransformationUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * ServiceTemplateValidator.java Purpose: Provide Configuration Generator ServiceTemplateValidator
 *
 * @version 1.0
 */

public class ServiceTemplateValidator {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ServiceTemplateValidator.class);
    
    private Map<String, String> metaData = new HashMap<>();
    
    StringBuilder message = new StringBuilder();
    
    /**
     * This is a validateServiceTemplate
     *
     * @param serviceTemplateContent
     * @return boolean
     * @throws ConfigModelException
     */
    @SuppressWarnings("squid:S00112")
    public boolean validateServiceTemplate(String serviceTemplateContent) throws ConfigModelException {
        if (StringUtils.isNotBlank(serviceTemplateContent)) {
            ServiceTemplate serviceTemplate =
                    TransformationUtils.readValue(serviceTemplateContent, ServiceTemplate.class);
            return validateServiceTemplate(serviceTemplate);
        } else {
            throw new ConfigModelException(
                    "Service Template Content is  (" + serviceTemplateContent + ") not Defined.");
        }
    }
    
    /**
     * This is a validateServiceTemplate
     *
     * @param serviceTemplate
     * @return boolean
     * @throws ConfigModelException
     */
    @SuppressWarnings("squid:S00112")
    public boolean validateServiceTemplate(ServiceTemplate serviceTemplate) throws ConfigModelException {
        if (serviceTemplate != null) {
            try {
                validateMetaData(serviceTemplate);
                (new DataTypeValidator(serviceTemplate, message)).validateDataTypes();
                (new NodeTypeValidator(serviceTemplate, message)).validateNodeTypes();
                (new TopologyTemplateValidator(serviceTemplate, message)).validateTopologyTemplate();
                logger.debug("Validation Message : {}", message);
            } catch (Exception e) {
                throw new ConfigModelException(
                        "Validation Failed " + e.toString() + ",Message Trace : \n" + message.toString());
            }
            
        } else {
            throw new ConfigModelException("Service Template is not defined.");
        }
        return true;
    }
    
    /**
     * This is a getMetaData to get the key information during the
     *
     * @return Map<String , String>
     */
    public Map<String, String> getMetaData() {
        return metaData;
    }
    
    private void validateMetaData(ServiceTemplate serviceTemplate) throws ConfigModelException {
        if (serviceTemplate.getMetadata() != null) {
            this.metaData.putAll(serviceTemplate.getMetadata());
            
            String author = serviceTemplate.getMetadata().get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_AUTHOR);
            String serviceTemplateName =
                    serviceTemplate.getMetadata().get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_NAME);
            String serviceTemplateVersion =
                    serviceTemplate.getMetadata().get(ConfigModelConstant.SERVICE_TEMPLATE_KEY_ARTIFACT_VERSION);
            
            if (StringUtils.isBlank(author)) {
                throw new ConfigModelException("Service Template Metadata (author) Information is missing.");
            }
            
            if (StringUtils.isBlank(serviceTemplateName)) {
                throw new ConfigModelException(
                        "Service Template Metadata (service-template-name) Information is missing.");
            }
            
            if (StringUtils.isBlank(serviceTemplateVersion)) {
                throw new ConfigModelException(
                        "Service Template Metadata (service-template-version) Information is missing.");
            }
            
        } else {
            throw new ConfigModelException("Service Template Metadata Information is missing.");
        }
        
    }
    
}
