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

package org.onap.ccsdk.config.model.utils;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelConstant;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class PrepareContextUtils {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(PrepareContextUtils.class);
    
    public Map<String, String> prepareContext(Map<String, String> context, String input, String serviceTemplateContent)
            throws Exception {
        if (StringUtils.isNotBlank(serviceTemplateContent)) {
            
            if (context == null) {
                context = new HashMap<>();
            }
            if (StringUtils.isNotBlank(input)) {
                TransformationUtils.convertJson2RootProperties(context, input);
            }
            
            String recipeName = context.get(ConfigModelConstant.PROPERTY_ACTION_NAME);
            if (StringUtils.isNotBlank(recipeName)) {
                String recipeInputName =
                        recipeName.replace(ConfigModelConstant.PROPERTY_RECIPE, ConfigModelConstant.PROPERTY_REQUEST);
                String recipeInput = context.get(recipeInputName);
                if (StringUtils.isNotBlank(recipeInput)) {
                    // Un necessary to hold the Recipe Request, It is already in input
                    context.remove(recipeInputName);
                    context.remove(ConfigModelConstant.PROPERTY_PAYLOAD);
                    TransformationUtils.convertJson2RootProperties(context, recipeInput);
                    logger.info("Converted recipe ({}) request inputs to context.", recipeName);
                }
            }
            
            ServiceTemplateUtils serviceTemplateUtils = new ServiceTemplateUtils();
            serviceTemplateUtils.convertServiceTemplate2Properties(serviceTemplateContent, context);
        }
        return context;
        
    }
    
}
