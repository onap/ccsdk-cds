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

package org.onap.ccsdk.config.model;

/**
 * ConfigGeneratorException.java Purpose: Provide Configuration Generator ConfigGeneratorException
 *
 * @version 1.0
 */
public class ConfigModelException extends Exception {
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * This is a ConfigGeneratorException constructor
     *
     * @param message
     */
    public ConfigModelException(String message) {
        super(message);
    }
    
    /**
     * This is a ConfigGeneratorException constructor
     *
     * @param message
     */
    public ConfigModelException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
