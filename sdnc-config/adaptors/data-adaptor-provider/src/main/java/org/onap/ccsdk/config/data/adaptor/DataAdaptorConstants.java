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

package org.onap.ccsdk.config.data.adaptor;

@SuppressWarnings("squid:S1118")
public class DataAdaptorConstants {
    
    public static final String OUTPUT_STATUS_SUCCESS = "success";
    public static final String OUTPUT_STATUS_FAILURE = "failure";
    
    public static final String PROPERTY_ENV_TYPE = "org.onap.ccsdk.config.rest.adaptors.envtype";
    public static final String PROPERTY_ENV_PROD = "field";
    public static final String PROPERTY_ENV_SOLO = "solo";
    
    public static final String INPUT_PARAM_TAGS = "tags";
    public static final String INPUT_PARAM_CAPABILITY_NAME = "capabilityName";
    public static final String INPUT_PARAM_MESSAGE = "message";
    public static final String INPUT_PARAM_MESSAGE_TYPE = "messageType";
    public static final String OUTPUT_PARAM_STATUS = "status";
    public static final String OUTPUT_PARAM_ERROR_MESSAGE = "error-message";
    public static final String INPUT_PARAM_FILE_CATEGORY = "fileCategory";
    public static final String INPUT_PARAM_VM_INSTANCE = "vmInstance";
    public static final String INPUT_PARAM_ASDC_ARTIFACT_IND = "asdcArtifactInd";
    public static final Object INPUT_PARAM_VNF_ID = "vnfId";
    public static final Object INPUT_PARAM_VM_NAME = "vmName";
    
    public static final Object INPUT_PARAM_CONFIG_CONTENT_TYPE = "configContentType";
    
    public static final Object INPUT_PARAM_CONFIG_CONTENT = "configContent";
    public static final Object INPUT_PARAM_CONFIG_INDICATOR = "configIndicator";
    
    public static final Object CONFIG_CONTENT_TYPE_CONFIGURATION = "Configuration";
    public static final Object CONFIG_CONTENT_TYPE_PARAMETERS = "Parameters";
    public static final Object CONFIG_INDICATOR_PREPARE = "Prepare";
    public static final Object CONFIG_INDICATOR_ACTIVE = "Active";
    
    public static final String INPUT_PARAM_RESPONSE_PRIFIX = "response-prefix";
    public static final String INPUT_PARAM_VNF_TYPE = "vnfType";
    public static final String INPUT_PARAM_VNFC_TYPE = "vnfcType";
    public static final String INPUT_PARAM_ACTION = "action";
    public static final String INPUT_PARAM_API_VERSION = "apiVersion";
    
    public static final String INPUT_PARAM_FILE_ID = "fileId";
    public static final String INPUT_PARAM_UPLOAD_CONFIG_ID = "uploadConfigId";
    public static final String FILE_CATEGORY_SERVICE_TEMPLATE = "service_template";
    
    public static final String ARTIFACT_TYPE_SDNC_MODEL = "SDNC_MODEL";
    
    public static final String LOG_MESSAGE_TYPE_LOG = "Log";
    public static final String LOG_MESSAGE_TYPE_COMPONENT = "Component";
    public static final String LOG_MESSAGE_TYPE_REQUEST = "Request";
    public static final String LOG_MESSAGE_TYPE_MODEL = "Model";
    public static final String LOG_MESSAGE_TYPE_DATA = "Data";
    public static final String LOG_MESSAGE_TYPE_CONFIG = "Config";
    
    public static final String SELF_SERVICE_STATUS_RECEIVED = "Received";
    public static final String SELF_SERVICE_STATUS_ACKNOWLEDGED = "Acknowledged";
    public static final String SELF_SERVICE_STATUS_REPLIED = "Replied";
    public static final String SELF_SERVICE_STATUS_FAILED = "Failed";
    
}
