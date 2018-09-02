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

package org.onap.ccsdk.config.rest.adaptor.service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.net.ssl.SSLContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorConstants;
import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.data.RestResponse;
import org.onap.ccsdk.config.rest.adaptor.utils.BasicAuthorizationInterceptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

abstract class AbstractConfigRestClientAdapter implements ConfigRestClientServiceAdapter {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(AbstractConfigRestClientAdapter.class);
    private static final String MS_INIT_FAIL = "Failed to initialise microservice client restTemplate.";
    
    protected boolean isRestClientServiceAdapaterEnabled = false;
    protected boolean isSSLServiceAdapaterEnabled = true;
    
    protected Map<String, String> properties = new ConcurrentHashMap<>();
    protected String serviceSelector;
    
    protected RestTemplate restTemplate;
    
    protected AbstractConfigRestClientAdapter(Map<String, String> properties, String serviceSelector) {
        this.properties = properties;
        this.serviceSelector = serviceSelector;
        setRestClientServiceAdapaterEnabled();
    }
    
    private void setRestClientServiceAdapaterEnabled() {
        String isEnabledProperty = ConfigRestAdaptorConstants.REST_ADAPTOR_BASE_PROPERTY + serviceSelector
                + ConfigRestAdaptorConstants.SERVICE_EANABLED_PROPERTY;
        String isRestClientServiceAdapaterEnabledStr = properties.get(isEnabledProperty);
        logger.info("Service selector ({}) enable status ({}) ", serviceSelector,
                isRestClientServiceAdapaterEnabledStr);
        if (StringUtils.isNotBlank(isRestClientServiceAdapaterEnabledStr)
                && Boolean.parseBoolean(isRestClientServiceAdapaterEnabledStr)) {
            isRestClientServiceAdapaterEnabled = true;
        }
    }
    
    private List<HttpMessageConverter<?>> getMessageConverters() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(new StringHttpMessageConverter());
        converters.add(new ResourceHttpMessageConverter());
        converters.add(new SourceHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter());
        return converters;
    }
    
    public void initialise(String user, String pass) {
        logger.trace("Config rest template factory user ({}) ", user);
        
        CloseableHttpClient httpClient =
                HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        
        restTemplate = new RestTemplate(getMessageConverters());
        restTemplate.setRequestFactory(requestFactory);
        if (StringUtils.isNotBlank(user) && StringUtils.isNotBlank(pass)) {
            restTemplate.getInterceptors().add(new BasicAuthorizationInterceptor(user, pass));
        }
    }
    
    public void initialiseSSL(String keyStorePath, String trustStorePath, String keyPass, String trustPass)
            throws ConfigRestAdaptorException {
        logger.trace("SSL rest template factory");
        
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        SSLContext sslContext = null;
        
        try (InputStream keyInput = new FileInputStream(keyStorePath)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(keyInput, keyPass.toCharArray());
            
            logger.info("key loaded successfully");
            sslContext = SSLContextBuilder.create().loadKeyMaterial(keyStore, keyPass.toCharArray()).loadTrustMaterial(
                    ResourceUtils.getFile(trustStorePath), trustPass.toCharArray(), acceptingTrustStrategy).build();
        } catch (Exception e) {
            throw new ConfigRestAdaptorException(e.getMessage());
        }
        
        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        
        restTemplate = new RestTemplate(getMessageConverters());
        restTemplate.setRequestFactory(requestFactory);
    }
    
    public <T> T getResource(HttpHeaders headers, String url, Class<T> responseType) throws ConfigRestAdaptorException {
        ResponseEntity<T> response = exchangeForEntity(headers, url, HttpMethod.GET, null, responseType);
        return processResponse(response, url, HttpMethod.GET);
    }
    
    public <T> T postResource(HttpHeaders headers, String url, Object request, Class<T> responseType)
            throws ConfigRestAdaptorException {
        ResponseEntity<T> response = exchangeForEntity(headers, url, HttpMethod.POST, request, responseType);
        return processResponse(response, url, HttpMethod.POST);
    }
    
    public <T> T exchangeResource(HttpHeaders headers, String url, Object request, Class<T> responseType, String method)
            throws ConfigRestAdaptorException {
        ResponseEntity<T> response = exchangeForEntity(headers, url, HttpMethod.resolve(method), request, responseType);
        return processResponse(response, url, HttpMethod.resolve(method));
    }
    
    public RestResponse getResource(HttpHeaders headers, String url) throws ConfigRestAdaptorException {
        return exchangeForEntity(headers, url, HttpMethod.GET, null);
    }
    
    public RestResponse postResource(HttpHeaders headers, String url, Object request)
            throws ConfigRestAdaptorException {
        return exchangeForEntity(headers, url, HttpMethod.POST, request);
    }
    
    public RestResponse exchangeResource(HttpHeaders headers, String url, Object request, String method)
            throws ConfigRestAdaptorException {
        return exchangeForEntity(headers, url, HttpMethod.resolve(method), request);
    }
    
    private RestResponse exchangeForEntity(HttpHeaders headers, String url, HttpMethod httpMethod, Object request)
            throws ConfigRestAdaptorException {
        RestResponse restResponse = new RestResponse();
        restResponse.setRequestHeaders(headers.toSingleValueMap());
        ResponseEntity<String> response = null;
        
        try {
            if (restTemplate == null) {
                logger.error(MS_INIT_FAIL);
            } else {
                logger.debug("Rest Operation: {}", httpMethod);
                logger.debug("url    : ({})", url);
                logger.debug("headers: ({})", headers);
                logger.debug("request: ({})", request);
                
                if (HttpMethod.GET == httpMethod) {
                    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
                    response = restTemplate.exchange(url, httpMethod, entity, String.class);
                } else {
                    HttpEntity<?> entity = new HttpEntity<>(request, headers);
                    response = restTemplate.exchange(url, httpMethod, entity, String.class);
                }
                logger.debug("response: ({})", response);
                
                if (response != null) {
                    logger.debug("response status code: ({})", response.getStatusCode());
                    restResponse.setBody(response.getBody());
                    restResponse.setStatusCode(response.getStatusCode().toString());
                    restResponse.setResponseHeaders(
                            response.getHeaders() != null ? response.getHeaders().toSingleValueMap() : null);
                    return restResponse;
                }
                throw new ConfigRestAdaptorException("Rest exchangeForEntity failed to perform ");
            }
        } catch (HttpClientErrorException clientError) {
            logger.debug("clientError: ({})", clientError);
            restResponse.setBody(StringUtils.isBlank(clientError.getResponseBodyAsString()) ? clientError.getMessage()
                    : clientError.getResponseBodyAsString());
            restResponse.setStatusCode(clientError.getStatusCode().toString());
        } catch (Exception e) {
            throw new ConfigRestAdaptorException(
                    String.format("httpMethod (%s) for url (%s) resulted in Exception (%s)", httpMethod, url, e));
        }
        return restResponse;
    }
    
    private <T> ResponseEntity<T> exchangeForEntity(HttpHeaders headers, String url, HttpMethod httpMethod,
            Object request, Class<T> responseType) throws ConfigRestAdaptorException {
        ResponseEntity<T> response = null;
        
        try {
            if (restTemplate == null) {
                logger.error(MS_INIT_FAIL);
            } else {
                logger.debug("Rest Operation: {}", httpMethod);
                logger.debug("url    : ({})", url);
                logger.debug("headers: ({})", headers);
                logger.debug("request: ({})", request);
                
                if (HttpMethod.GET == httpMethod) {
                    HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
                    response = restTemplate.exchange(url, httpMethod, entity, responseType);
                } else {
                    HttpEntity<?> entity = new HttpEntity<>(request, headers);
                    response = restTemplate.exchange(url, httpMethod, entity, responseType);
                }
                logger.debug("response: ({})", response);
                
                if (response != null) {
                    logger.debug("response status code: ({})", response.getStatusCode());
                } else {
                    throw new ConfigRestAdaptorException("exchangeForEntity failed to perform ");
                }
            }
        } catch (Exception e) {
            throw new ConfigRestAdaptorException(
                    String.format("httpMethod (%s) for url (%s) resulted in Exception (%s)", httpMethod, url, e));
        }
        return response;
    }
    
    protected synchronized <T> T processResponse(ResponseEntity<T> response, String url, HttpMethod httpMethod)
            throws ConfigRestAdaptorException {
        if (response != null) {
            if ((HttpMethod.DELETE == httpMethod && (response.getStatusCode() == HttpStatus.NO_CONTENT
                    || response.getStatusCode() == HttpStatus.NOT_FOUND))
                    || ((HttpMethod.GET == httpMethod || HttpMethod.PUT == httpMethod || HttpMethod.POST == httpMethod)
                            && (response.getStatusCode() == HttpStatus.OK
                                    || response.getStatusCode() == HttpStatus.CREATED))) {
                return response.getBody();
            }
            throw new ConfigRestAdaptorException(
                    String.format("Rest Operation is failed with response-code (%s) for the URL (%s)",
                            response.getStatusCode(), url));
        }
        throw new ConfigRestAdaptorException(String.format("Rest Operation is failed for the URL (%s)", url));
    }
    
    protected synchronized String constructUrl(String baseUrl, String path) {
        if (StringUtils.isNotBlank(path)) {
            return baseUrl + path;
        } else {
            return baseUrl;
        }
    }
    
}
