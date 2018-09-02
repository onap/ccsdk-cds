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
import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface NamedQueryExecutorDao {
    /**
     * Return NamedParameterJdbcTemplate object.
     */
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() throws SvcLogicException;
    
    /**
     * Query given SQL to create a prepared statement from SQL and a list of arguments to bind to the
     * query, expecting a result list.
     * <p>
     * The results will be mapped to a List (one entry for each row) of Maps (one entry for each column,
     * using the column name as the key).
     *
     * @param sql SQL query to execute
     * @param param map of parameters to bind to the query (leaving it to the PreparedStatement to guess
     *        the corresponding SQL type)
     * @return a List that contains a Map per row
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<Map<String, Object>> query(String sql, Map<String, Object> parameters) throws SvcLogicException;
    
    /**
     * Issue an update via a prepared statement, binding the given arguments.
     *
     * @param sql SQL containing named parameters
     * @param param map of parameters to bind to the query (leaving it to the PreparedStatement to guess
     *        the corresponding SQL type)
     * @return the number of rows affected
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the update
     */
    public int update(String sql, Map<String, Object> parameters) throws SvcLogicException;
    
}
