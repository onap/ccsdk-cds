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

package org.onap.ccsdk.config.model.service;

import java.util.List;
import java.util.Map;
import org.onap.ccsdk.config.model.data.ServiceTemplate;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface ConfigModelService {
    
    public Boolean validateServiceTemplate(ServiceTemplate serviceTemplate) throws SvcLogicException;
    
    public Map<String, String> prepareContext(Map<String, String> context, String input, String serviceTemplateName,
            String serviceTemplateVersion) throws SvcLogicException;
    
    public Map<String, String> prepareContext(Map<String, String> context, String input, String serviceTemplateContent)
            throws SvcLogicException;
    
    public Map<String, String> convertJson2properties(Map<String, String> context, String jsonContent,
            List<String> blockKeys) throws SvcLogicException;
    
    public Map<String, String> convertServiceTemplate2Properties(String serviceTemplateContent,
            final Map<String, String> context) throws SvcLogicException;
    
    public Map<String, String> convertServiceTemplate2Properties(ServiceTemplate serviceTemplate,
            final Map<String, String> context) throws SvcLogicException;
    
    public SvcLogicContext assignInParamsFromModel(final SvcLogicContext context, final Map<String, String> inParams)
            throws SvcLogicException;
    
    public SvcLogicContext assignOutParamsFromModel(final SvcLogicContext context, final Map<String, String> inParams)
            throws SvcLogicException;
    
    public String getNodeTemplateContent(final SvcLogicContext context, String templateName) throws SvcLogicException;
    
    public String getNodeTemplateMapping(final SvcLogicContext context, String templateName) throws SvcLogicException;
    
}
