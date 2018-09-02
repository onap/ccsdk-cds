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

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.DataType;
import org.onap.ccsdk.config.model.data.PropertyDefinition;

/**
 * PropertyDefinitionValidator.java Purpose: Provide Configuration Generator
 * PropertyDefinitionValidator
 *
 * @version 1.0
 */
public class PropertyDefinitionValidator {
    
    StringBuilder message = new StringBuilder();
    
    public PropertyDefinitionValidator(StringBuilder message) {
        this.message = message;
        
    }
    
    /**
     * This is a validatePropertyDefinition stored in database
     *
     * @param stDataTypes
     * @param properties
     * @return boolean
     * @throws ConfigModelException
     */
    @SuppressWarnings({"squid:S00112", "squid:S3776", "squid:S1192"})
    public boolean validatePropertyDefinition(Map<String, DataType> stDataTypes,
            Map<String, PropertyDefinition> properties) {
        
        if (stDataTypes != null && properties != null) {
            properties.forEach((propertyKey, prop) -> {
                if (propertyKey != null && prop != null) {
                    try {
                        String propertType = prop.getType();
                        message.append("\n Validating (" + propertyKey + ") " + prop);
                        
                        if (!ValidTypes.getValidPropertType().contains(propertType)
                                && !stDataTypes.containsKey(propertType)) {
                            throw new ConfigModelException("Data Type (" + propertyKey + ") -> type(" + propertType
                                    + ") is not a valid type.");
                        } else if (ValidTypes.getListPropertType().contains(propertType)) {
                            if (prop.getEntrySchema() == null || StringUtils.isBlank(prop.getEntrySchema().getType())) {
                                throw new ConfigModelException("Data Type (" + propertyKey + ") -> type (" + propertType
                                        + ") Entity Schema is not defined.");
                            }
                            
                            String entitySchemaType = prop.getEntrySchema().getType();
                            
                            if (!ValidTypes.getValidPropertType().contains(entitySchemaType)
                                    && !stDataTypes.containsKey(entitySchemaType)) {
                                message.append("\n Present Data Type " + stDataTypes);
                                throw new ConfigModelException("Data Type (" + propertyKey + ") -> type(" + propertType
                                        + ") -> entitySchema(" + entitySchemaType + ") is not defined.");
                            }
                        }
                    } catch (ConfigModelException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        return true;
    }
    
}
