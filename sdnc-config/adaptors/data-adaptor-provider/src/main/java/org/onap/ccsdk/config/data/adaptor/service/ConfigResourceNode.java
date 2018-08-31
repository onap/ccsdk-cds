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
