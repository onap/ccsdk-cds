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
import org.onap.ccsdk.config.model.ConfigModelException;
import org.onap.ccsdk.config.model.ValidTypes;
import org.onap.ccsdk.config.model.data.DataType;
import org.onap.ccsdk.config.model.data.PropertyDefinition;
import org.onap.ccsdk.config.model.data.ServiceTemplate;

/**
 * DataTypeValidator.java Purpose: Provide Configuration Generator DataTypeValidator
 *
 * @version 1.0
 */
public class DataTypeValidator {
    
    private StringBuilder message;
    private Map<String, DataType> stDataTypes;
    private ServiceTemplate serviceTemplate;
    private PropertyDefinitionValidator propertyDefinitionValidator;
    
    /**
     * This is a DataTypeValidator
     *
     * @param serviceTemplate
     * @throws ConfigModelException
     */
    public DataTypeValidator(ServiceTemplate serviceTemplate, StringBuilder message) throws ConfigModelException {
        this.serviceTemplate = serviceTemplate;
        this.message = message;
        propertyDefinitionValidator = new PropertyDefinitionValidator(this.message);
        stDataTypes = new HashMap<>();
        loadInitial();
        
    }
    
    private void loadInitial() {
        if (serviceTemplate != null && serviceTemplate.getDataTypes() != null) {
            message.append("\n DataTypes" + serviceTemplate.getDataTypes());
            serviceTemplate.getDataTypes().forEach((dataTypeKey, dataType) -> {
                stDataTypes.put(dataTypeKey, dataType);
                message.append("\n Data Type (" + dataTypeKey + ")  loaded successfully.");
            });
        }
    }
    
    /**
     * This is a validateDataTypes
     *
     * @return boolean
     * @throws ConfigModelException
     */
    @SuppressWarnings("squid:S00112")
    public boolean validateDataTypes() {
        if (serviceTemplate != null && serviceTemplate.getDataTypes() != null) {
            
            serviceTemplate.getDataTypes().forEach((dataTypeKey, dataType) -> {
                if (dataType != null) {
                    try {
                        String derivedFrom = dataType.getDerivedFrom();
                        checkValidDerivedType(dataTypeKey, derivedFrom);
                        checkValidProperties(dataTypeKey, dataType.getProperties());
                    } catch (ConfigModelException e) {
                        throw new RuntimeException(e);
                    }
                    
                }
                
            });
        }
        return true;
    }
    
    private boolean checkValidDerivedType(String dataTypeName, String derivedFrom) throws ConfigModelException {
        
        if (StringUtils.isBlank(derivedFrom) || !ValidTypes.getValidDataTypeDerivedFrom().contains(derivedFrom)) {
            throw new ConfigModelException(derivedFrom + " is not a valid derived type for Data type " + dataTypeName);
        }
        return true;
    }
    
    private boolean checkValidProperties(String dataTypeName, Map<String, PropertyDefinition> properties) {
        if (properties != null) {
            message.append("\n validation Data Type (" + dataTypeName + ") Property.");
            propertyDefinitionValidator.validatePropertyDefinition(stDataTypes, properties);
        }
        return true;
    }
    
}
