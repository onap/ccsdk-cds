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

package org.onap.ccsdk.config.data.adaptor.service;

import java.util.List;
import java.util.Map;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public interface ConfigResourceService {
    
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
    public List<Map<String, Object>> query(String sql, Map<String, Object> param) throws SvcLogicException;
    
    /**
     * Issue an update via a prepared statement, binding the given arguments.
     *
     * @param sql SQL containing named parameters
     * @param param map of parameters to bind to the query (leaving it to the PreparedStatement to guess
     *        the corresponding SQL type)
     * @return the number of rows affected
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the update
     */
    public int update(String sql, Map<String, Object> param) throws SvcLogicException;
    
    /**
     * Query given SQL to create a prepared statement from SQL and a list of arguments to bind to the
     * query, expecting a result list.
     * <p>
     * The results will be mapped to a List (one entry for each row) of Maps (one entry for each column,
     * using the column name as the key).
     *
     * @param sql SQL query to execute
     * @param data arguments to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @return a List that contains a Map per row
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<Map<String, Object>> query(String sql, Object[] data) throws SvcLogicException;
    
    /**
     * Issue a single SQL update operation (such as an insert, update or delete statement) via a
     * prepared statement, binding the given arguments.
     *
     * @param sql SQL containing bind parameters
     * @param data arguments to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @return the number of rows affected
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the update
     */
    public int update(String sql, Object[] data) throws SvcLogicException;
    
    /**
     * Issue a single SQL Insert operation for CONFIG_TRANSACTION_LOG table via a prepared statement,
     * binding the given arguments.
     *
     * @param transactionLog arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public void save(TransactionLog transactionLog) throws SvcLogicException;
    
    /**
     * Query CONFIG_TRANSACTION_LOG table for given request_id, mapping each row to a Java object via a
     * TransactionLog RowMapper.
     *
     * @param requestId argument to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<TransactionLog> getTransactionsByRequestId(String requestId) throws SvcLogicException;
    
    /**
     * Query CONFIG_RESOURCE table for given input param to create a prepared statement to bind to the
     * query, mapping each row to a Java object via a ConfigResource RowMapper.
     *
     * @param configResource argument to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<ConfigResource> getConfigResource(ConfigResource configResource) throws SvcLogicException;
    
    /**
     * Issue a single SQL update operation (insert or update statement) for CONFIG_RESOURCE table via a
     * prepared statement, binding the given arguments.
     *
     * @param configResource arguments to bind to the query (mapping it to the PreparedStatement to the
     *        corresponding SQL type)
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if there is any problem issuing the insert
     */
    public ConfigResource saveConfigResource(ConfigResource configResource) throws SvcLogicException;
    
    /**
     * Query ConcurrentHashMap having CONFIG_PROPERTY_MAP table data for given key.
     *
     * @param key key mapped to a value
     * @return the result string, containing mapped string value
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if fails
     */
    public String getConfigPropertyByKey(String key) throws SvcLogicException;
    
    /**
     * Query CONFIG_TRANSACTION_LOG table for given request_id, mapping each row to a Java object via a
     * TransactionLog RowMapper.
     *
     * @param requestId argument to bind to the query (leaving it to the PreparedStatement to guess the
     *        corresponding SQL type)
     * @param messageType argument to bind to the query (leaving it to the PreparedStatement to guess
     *        the corresponding SQL type)
     * @return the result List, containing mapped objects
     * @throws org.onap.ccsdk.sli.core.sli.SvcLogicException if the query fails
     */
    public List<TransactionLog> getTransactionsByRequestId(String requestId, String messageType)
            throws SvcLogicException;
    
}
