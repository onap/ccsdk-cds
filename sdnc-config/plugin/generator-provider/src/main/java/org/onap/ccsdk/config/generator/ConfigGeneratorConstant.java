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

package org.onap.ccsdk.config.generator;

public class ConfigGeneratorConstant {
    
    private ConfigGeneratorConstant() {
        
    }
    
    public static final String STRING_ENCODING = "utf-8";
    public static final String Y = "Y";
    public static final String N = "N";
    public static final String DATA_TYPE_TEXT = "TEXT";
    public static final String DATA_TYPE_JSON = "JSON";
    public static final String DATA_TYPE_XML = "XML";
    public static final String DATA_TYPE_SQL = "SQL";
    
    public static final String INPUT_PARAM_REQUEST_ID = "request-id";
    public static final String INPUT_PARAM_RESOURCE_ID = "resource-id";
    public static final String INPUT_PARAM_RESOURCE_TYPE = "resource-type";
    public static final String INPUT_PARAM_ACTION_NAME = "action-name";
    public static final String INPUT_PARAM_TEMPLATE_NAME = "template-name";
    public static final String INPUT_PARAM_TEMPLATE_CONTENT = "template-content";
    public static final String INPUT_PARAM_TEMPLATE_DATA = "template-data";
    
    public static final String OUTPUT_PARAM_GENERATED_CONFIG = "generated-config";
    public static final String OUTPUT_PARAM_MASK_INFO = "mask-info";
    public static final String OUTPUT_PARAM_STATUS = "status";
    public static final String OUTPUT_PARAM_ERROR_MESSAGE = "error-message";
    public static final String OUTPUT_STATUS_SUCCESS = "success";
    public static final String OUTPUT_STATUS_FAILURE = "failure";
    
}
