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

package org.onap.ccsdk.config.data.adaptor.service;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.DataAdaptorConstants;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class ConfigResourceNode implements SvcLogicJavaPlugin {
    
    private ConfigResourceService configResourceService;
    
    public ConfigResourceNode(ConfigResourceService configResourceService) {
        this.configResourceService = configResourceService;
    }
    
    public void saveConfigTransactionLog(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String responsePrefix = inParams.get(DataAdaptorConstants.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            
            String messageType = inParams.get(DataAdaptorConstants.INPUT_PARAM_MESSAGE_TYPE);
            String message = inParams.get(DataAdaptorConstants.INPUT_PARAM_MESSAGE);
            String requestId = ctx.getAttribute("request-id");
            
            TransactionLog transactionLog = new TransactionLog();
            
            transactionLog.setMessage(message);
            transactionLog.setMessageType(messageType);
            transactionLog.setRequestId(requestId);
            
            configResourceService.save(transactionLog);
            
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + DataAdaptorConstants.OUTPUT_PARAM_STATUS,
                    DataAdaptorConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + DataAdaptorConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            throw new SvcLogicException("Failed in saveConfigTransactionLog :" + e.getMessage());
        }
    }
    
}
