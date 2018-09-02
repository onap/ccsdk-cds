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

package org.onap.ccsdk.config.data.adaptor.dao;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface ConfigPropertyMapDao {
    
    /**
     * Query ConcurrentHashMap having CONFIG_PROPERTY_MAP table data for given key.
     *
     * @param key key mapped to a value
     * @return the result string, containing mapped string value
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if fails
     */
    public String getConfigPropertyByKey(String key) throws SvcLogicException;
    
}
