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
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.validator.ServiceTemplateValidator;
import org.springframework.stereotype.Service;

/**
 * ServiceTemplateValidatorService.java Purpose: Provide Service to Validate Service Model Template
 *
 * @author Brinda Santh
 * @version 1.0
 */
@Deprecated
@Service
public class ConfigModelValidatorService {

    /**
     * This is a validateServiceTemplate
     *
     * @param serviceTemplateContent
     * @return ServiceTemplate
     * @throws BluePrintException
     */
    public ServiceTemplate validateServiceTemplate(String serviceTemplateContent) throws BluePrintException {
        Preconditions.checkArgument(StringUtils.isNotBlank(serviceTemplateContent), "Service Template Content is  (" + serviceTemplateContent + ") not Defined.");
        ServiceTemplate serviceTemplate =
                JacksonUtils.readValue(serviceTemplateContent, ServiceTemplate.class);
        return validateServiceTemplate(serviceTemplate);
    }

    /**
     * This is a enhanceServiceTemplate
     *
     * @param serviceTemplate
     * @return ServiceTemplate
     * @throws BluePrintException
     */
    @SuppressWarnings("squid:S00112")
    public ServiceTemplate validateServiceTemplate(ServiceTemplate serviceTemplate) throws BluePrintException {
        Preconditions.checkNotNull(serviceTemplate, "Service Template is not defined.");
        ServiceTemplateValidator validator = new ServiceTemplateValidator();
        validator.validateServiceTemplate(serviceTemplate);
        return serviceTemplate;
    }


}
