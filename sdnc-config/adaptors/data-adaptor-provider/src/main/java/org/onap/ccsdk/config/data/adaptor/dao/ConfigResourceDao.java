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

import java.util.List;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public interface ConfigResourceDao {
    
    /**
     * Issue a single SQL Insert operation for CONFIG_RESOURCE table via a prepared statement, binding
     * the given arguments.
     *
     * @param transactionLog arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public ConfigResource save(ConfigResource configResourceInput) throws SvcLogicException;
    
    /**
     * Issue a single SQL Delete operation for CONFIG_RESOURCE table via a prepared statement, binding
     * the given arguments.
     *
     * @param configResource arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public void deleteByConfigResource(ConfigResource configResourceInput) throws SvcLogicException;
    
    /**
     * Query CONFIG_RESOURCE table for given input param to create a prepared statement to bind to the
     * query, mapping each row to a Java object via a ConfigResource RowMapper.
     *
     * @param configResource argument to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<ConfigResource> findByConfigResource(ConfigResource configResourceInput) throws SvcLogicException;
    
    public ConfigResource getConfigResource(ConfigResource configResource) throws SvcLogicException;
}
