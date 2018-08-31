package org.onap.ccsdk.config.rest.adaptor.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.data.RestResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigRestAdaptorServiceImpl implements ConfigRestAdaptorService {

    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigRestAdaptorServiceImpl.class);
    private Map<String, String> restProperties = new ConcurrentHashMap<>();

    public ConfigRestAdaptorServiceImpl(String propertyPath) {
        initializeProperties(propertyPath);
        try {
            String envType = restProperties.get(ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY
                    + ConfigRestAdaptorConstants.REST_ADAPTOR_ENV_TYPE);

            if (!(ConfigRestAdaptorConstants.PROPERTY_ENV_PROD.equalsIgnoreCase(envType)
                    || ConfigRestAdaptorConstants.PROPERTY_ENV_SOLO.equalsIgnoreCase(envType))) {
                ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
                Runnable task = () -> {
                    initializeProperties(propertyPath);
                };
                executor.scheduleWithFixedDelay(task, 60, 15, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void initializeProperties(String propertyPath) {
        logger.trace("Initialising Config rest adaptor Service with property directory ({})", propertyPath);
        try {
            if (StringUtils.isBlank(propertyPath)) {
                propertyPath = System.getProperty(ConfigRestAdaptorConstants.SDNC_ROOT_DIR_ENV_VAR_KEY);
            }

            if (StringUtils.isBlank(propertyPath)) {
                throw new ConfigRestAdaptorException(
                        String.format("Failed to get the property directory (%s)", propertyPath));
            }

            // Loading Default config-rest-adaptor.properties
            String propertyFile =
                    propertyPath + File.separator + ConfigRestAdaptorConstants.REST_ADAPTOR_PROPERTIES_FILE_NAME;

            Properties properties = new Properties();
            properties.load(new FileInputStream(propertyFile));

            logger.trace("Initializing properties details for property file ({}) properties ({})", propertyFile,
                    properties);
            restProperties.putAll(properties.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString())));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public <T> T getResource(String serviceSelector, String path, Class<T> responseType)
            throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).getResource(path, responseType);
    }

    @Override
    public <T> T postResource(String serviceSelector, String path, Object request, Class<T> responseType)
            throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).postResource(path, request, responseType);
    }

    @Override
    public <T> T exchangeResource(String serviceSelector, String path, Object request, Class<T> responseType,
            String method) throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).exchangeResource(path, request, responseType,
                method);
    }

    @Override
    public RestResponse getResource(String serviceSelector, String path) throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).getResource(path);
    }

    @Override
    public RestResponse postResource(String serviceSelector, String path, Object request)
            throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).postResource(path, request);
    }

    @Override
    public RestResponse exchangeResource(String serviceSelector, String path, Object request, String method)
            throws ConfigRestAdaptorException {
        return getRestClientAdapterBySelectorName(serviceSelector).exchangeResource(path, request, method);
    }

    private ConfigRestClientServiceAdapter getRestClientAdapterBySelectorName(String serviceSelector)
            throws ConfigRestAdaptorException {
        String adoptorType = restProperties.get(ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                + ConfigRestAdaptorConstants.SERVICE_TYPE_PROPERTY);
        if (StringUtils.isNotBlank(adoptorType)) {
            if (ConfigRestAdaptorConstants.REST_ADAPTOR_TYPE_GENERIC.equalsIgnoreCase(adoptorType)) {
                return new GenericRestClientAdapterImpl(restProperties, serviceSelector);
            } else if (ConfigRestAdaptorConstants.REST_ADAPTOR_TYPE_SSL.equalsIgnoreCase(adoptorType)) {
                return new SSLRestClientAdapterImpl(restProperties, serviceSelector);
            } else {
                throw new ConfigRestAdaptorException(
                        String.format("no implementation for rest adoptor type (%s) for the selector (%s).",
                                adoptorType, serviceSelector));
            }
        } else {
            throw new ConfigRestAdaptorException(
                    String.format("couldn't get rest adoptor type for the selector (%s)", serviceSelector));
        }
    }

}
