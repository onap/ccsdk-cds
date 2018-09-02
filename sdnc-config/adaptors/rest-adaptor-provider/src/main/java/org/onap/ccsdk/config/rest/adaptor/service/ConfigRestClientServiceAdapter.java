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

interface ConfigRestClientServiceAdapter {
    
    public <T> T getResource(String path, Class<T> responseType) throws ConfigRestAdaptorException;
    
    public <T> T postResource(String path, Object request, Class<T> responseType) throws ConfigRestAdaptorException;
    
    public <T> T exchangeResource(String path, Object request, Class<T> responseType, String method)
            throws ConfigRestAdaptorException;
    
    public RestResponse getResource(String path) throws ConfigRestAdaptorException;
    
    public RestResponse postResource(String path, Object request) throws ConfigRestAdaptorException;
    
    public RestResponse exchangeResource(String path, Object request, String method) throws ConfigRestAdaptorException;
    
}
