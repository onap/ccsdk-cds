package org.onap.ccsdk.config.rest.adaptor.service;

import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.data.RestResponse;

interface ConfigRestClientServiceAdapter {

    public <T> T getResource(String path, Class<T> responseType) throws ConfigRestAdaptorException;

    public <T> T postResource(String path, Object request, Class<T> responseType) throws ConfigRestAdaptorException;

    public <T> T exchangeResource(String path, Object request, Class<T> responseType, String method)
            throws ConfigRestAdaptorException;

    public RestResponse getResource(String path) throws ConfigRestAdaptorException;

    public RestResponse postResource(String path, Object request) throws ConfigRestAdaptorException;

    public RestResponse exchangeResource(String path, Object request, String method) throws ConfigRestAdaptorException;

}
