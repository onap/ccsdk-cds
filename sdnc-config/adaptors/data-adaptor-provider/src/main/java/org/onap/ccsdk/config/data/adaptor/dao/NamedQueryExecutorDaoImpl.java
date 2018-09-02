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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class NamedQueryExecutorDaoImpl implements NamedQueryExecutorDao {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(QueryExecutorDaoImpl.class);
    private static final String CLASS_NAME = "NamedQueryExecutorDaoImpl";
    
    @Autowired(required = true)
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    public NamedQueryExecutorDaoImpl(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        logger.info("{} Constructor initialised..", CLASS_NAME);
    }
    
    @Override
    public List<Map<String, Object>> query(String sql, Map<String, Object> parameters) throws SvcLogicException {
        logger.debug("Query  ({}) with parameters ({})", sql, parameters);
        return namedParameterJdbcTemplate.queryForList(sql, parameters);
    }
    
    @Override
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return namedParameterJdbcTemplate;
    }
    
    @Override
    public int update(String sql, Map<String, Object> parameters) throws SvcLogicException {
        logger.debug("update ({}) with parameters ({})", sql, parameters);
        return namedParameterJdbcTemplate.update(sql, parameters);
    }
}
