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

import org.onap.ccsdk.config.rest.adaptor.ConfigRestAdaptorException;
import org.onap.ccsdk.config.rest.adaptor.data.RestResponse;

public interface ConfigRestAdaptorService {
    
    /**
     * Retrieve an entity by doing a GET on the specified URL. The response is converted and stored in
     * defined responseType.
     * 
     * @param selectorName the property selector
     * @param path the URI path which will append in baseURL mentioned in selector property
     * @param responseType the type of the return value
     */
    public <T> T getResource(String selectorName, String path, Class<T> responseType) throws ConfigRestAdaptorException;
    
    /**
     * Create a new resource by POSTing the given object to the URI template, and returns the response
     * as defined responseType
     * 
     * @param selectorName the property selector
     * @param path the URI path which will append in baseURL mentioned in selector property
     * @param request the Object to be POSTed, may be {@code null}
     * @param responseType the type of the return value
     */
    public <T> T postResource(String selectorName, String path, Object request, Class<T> responseType)
            throws ConfigRestAdaptorException;
    
    /**
     * Execute the HTTP method to the given URI template, writing the given request entity to the
     * request, and returns the response as defined responseType
     * 
     * @param selectorName the property selector
     * @param path the URI path which will append in baseURL mentioned in selector property
     * @param request the Object to be POSTed, may be {@code null}
     * @param responseType the type of the return value
     * @param method the HTTP method (GET, POST, etc)
     */
    public <T> T exchangeResource(String selectorName, String path, Object request, Class<T> responseType,
            String method) throws ConfigRestAdaptorException;
    
    /**
     * Retrieve an entity by doing a GET on the specified URL. The response is converted and stored in
     * defined responseType.
     * 
     * @param selectorName the property selector
     * @param path the URI path which will append in baseURL mentioned in selector property
     */
    public RestResponse getResource(String selectorName, String path) throws ConfigRestAdaptorException;
    
    /**
     * Create a new resource by POSTing the given object to the URI template, and returns the response
     * as defined responseType
     * 
     * @param selectorName the property selector
     * @param path the URI path which will append in baseURL mentioned in selector property
     * @param request the Object to be POSTed, may be {@code null}
     */
    public RestResponse postResource(String selectorName, String path, Object request)
            throws ConfigRestAdaptorException;
    
    /**
     * Execute the HTTP method to the given URI template, writing the given request entity to the
     * request, and returns the response as defined responseType
     * 
     * @param selectorName the property selector
     * @param path the URI path which will append in baseURL mentioned in selector property
     * @param request the Object to be POSTed, may be {@code null}
     * @param method the HTTP method (GET, POST, etc)
     */
    public RestResponse exchangeResource(String selectorName, String path, Object request, String method)
            throws ConfigRestAdaptorException;
    
}
