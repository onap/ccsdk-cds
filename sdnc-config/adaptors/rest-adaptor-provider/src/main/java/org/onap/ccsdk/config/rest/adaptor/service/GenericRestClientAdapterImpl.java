package org.onap.ccsdk.config.rest.adaptor.service;

import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.data.RestResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class GenericRestClientAdapterImpl extends AbstractConfigRestClientAdapter {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(GenericRestClientAdapterImpl.class);
    private String baseUrl = "";

    public GenericRestClientAdapterImpl(Map<String, String> properties, String serviceSelector)
            throws ConfigRestAdaptorException {
        super(properties, serviceSelector);
        init(serviceSelector);
    }

    private void init(String serviceSelector) throws ConfigRestAdaptorException {
        try {
            if (isRestClientServiceAdapaterEnabled) {
                String baseUrlProperty = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SERVICE_BASEURL_PROPERTY;
                String userProperty = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SERVICE_USER_PROPERTY;
                String passProperty = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SERVICE_PSSWD_PROPERTY;

                baseUrl = properties.get(baseUrlProperty);
                String userId = properties.get(userProperty);
                String pass = properties.get(passProperty);

                initialise(userId, pass);
                logger.info("Initialised restconf adaptor service for selector ({})", serviceSelector);
                if (restTemplate == null) {
                    throw new ConfigRestAdaptorException("couldn't initialise rest selector (" + serviceSelector + ")");
                }
            } else {
                throw new ConfigRestAdaptorException("rest selector (" + serviceSelector + ") is not enabled");
            }

        } catch (Exception e) {
            throw new ConfigRestAdaptorException("GenericRestClientServiceAdapter : " + e.getMessage(), e);
        }
    }

    @Override
    public <T> T getResource(String path, Class<T> responseType) throws ConfigRestAdaptorException {
        return super.getResource(formHttpHeaders(), constructUrl(baseUrl, path), responseType);
    }

    @Override
    public <T> T postResource(String path, Object request, Class<T> responseType) throws ConfigRestAdaptorException {
        return super.postResource(formHttpHeaders(), constructUrl(baseUrl, path), request, responseType);
    }

    @Override
    public <T> T exchangeResource(String path, Object request, Class<T> responseType, String method)
            throws ConfigRestAdaptorException {
        return super.exchangeResource(formHttpHeaders(), constructUrl(baseUrl, path), request, responseType, method);
    }

    @Override
    public RestResponse getResource(String path) throws ConfigRestAdaptorException {
        return super.getResource(formHttpHeaders(), constructUrl(baseUrl, path));
    }

    @Override
    public RestResponse postResource(String path, Object request) throws ConfigRestAdaptorException {
        return super.postResource(formHttpHeaders(), constructUrl(baseUrl, path), request);
    }

    @Override
    public RestResponse exchangeResource(String path, Object request, String method) throws ConfigRestAdaptorException {
        return super.exchangeResource(formHttpHeaders(), constructUrl(baseUrl, path), request, method);
    }

    private HttpHeaders formHttpHeaders() {

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("X-TransactionId", generateUUID());
        headers.add("X-ECOMP-RequestID", headers.getFirst("X-TransactionId"));

        String appIDPath = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                + ConfigRestAdaptorConstants.SERVICE_APPID_PROPERTY;
        String environmentPath = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                + ConfigRestAdaptorConstants.SERVICE_ENV_PROPERTY;
        String clientAuthPath = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                + ConfigRestAdaptorConstants.SERVICE_CLIENTAUTH_PROPERTY;
        String authorizationPath = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                + ConfigRestAdaptorConstants.SERVICE_AUTHORIZATION_PROPERTY;

        if (StringUtils.isNotBlank(properties.get(appIDPath))) {
            headers.add("X-FromAppId", properties.get(appIDPath));
        }
        if (StringUtils.isNotBlank(properties.get(clientAuthPath))) {
            headers.add("ClientAuth", properties.get(clientAuthPath));
        }
        if (StringUtils.isNotBlank(properties.get(authorizationPath))) {
            headers.add("Authorization", properties.get(authorizationPath));
        }
        if (StringUtils.isNotBlank(properties.get(environmentPath))) {
            headers.add("Environment", properties.get(environmentPath));
        }

        return headers;
    }

    private synchronized String generateUUID() {
        return UUID.randomUUID().toString();
    }


}
