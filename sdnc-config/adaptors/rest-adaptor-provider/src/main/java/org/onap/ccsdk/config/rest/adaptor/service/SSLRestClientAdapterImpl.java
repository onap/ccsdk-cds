package org.onap.ccsdk.config.rest.adaptor.service;

import java.util.Map;
import java.util.UUID;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.data.RestResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class SSLRestClientAdapterImpl extends AbstractConfigRestClientAdapter {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(SSLRestClientAdapterImpl.class);
    private String baseUrl = "";
    private String application = "";

    public SSLRestClientAdapterImpl(Map<String, String> properties, String serviceSelector)
            throws ConfigRestAdaptorException {
        super(properties, serviceSelector);
        init(serviceSelector);
    }

    private void init(String serviceSelector) throws ConfigRestAdaptorException {
        try {
            if (isSSLServiceAdapaterEnabled) {

                logger.info("Initializing SSL client for selector ({}), properties ({})", serviceSelector, properties);

                String baseUrlProp = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SSL_SERVICE_BASEURL;
                String applicationProp = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SSL_SERVICE_APP;
                String keyStorePathProp = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SSL_SERVICE_KEY;
                String keyStorePassProp = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SSL_SERVICE_KEY_PSSWD;
                String trustStorePathProp = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SSL_SERVICE_TRUST;
                String trustStorePassProp = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                        + ConfigRestAdaptorConstants.SSL_SERVICE_TRUST_PSSWD;

                baseUrl = properties.get(baseUrlProp);
                application = properties.get(applicationProp);

                String keyStorePath = properties.get(keyStorePathProp);
                String trustStorePath = properties.get(trustStorePathProp);
                String keyStorePass = properties.get(keyStorePassProp);
                String trustStorePass = properties.get(trustStorePassProp);

                initialiseSSL(keyStorePath, trustStorePath, keyStorePass, trustStorePass);
                logger.info("Initialised SSL Client Service adaptor service for selector ({})", serviceSelector);
                if (restTemplate == null) {
                    throw new ConfigRestAdaptorException(
                            "couldn't initialise SSL Client selector (" + serviceSelector + ")");
                }
            } else {
                throw new ConfigRestAdaptorException("SSL Client selector (" + serviceSelector + ") is not enabled");
            }

        } catch (Exception e) {
            throw new ConfigRestAdaptorException("SSLRestClientAdapterImpl : " + e.getMessage(), e);
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
        headers.add("X-FromAppId", application);
        headers.add("X-TransactionId", generateUUID());

        return headers;
    }

    private synchronized String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
