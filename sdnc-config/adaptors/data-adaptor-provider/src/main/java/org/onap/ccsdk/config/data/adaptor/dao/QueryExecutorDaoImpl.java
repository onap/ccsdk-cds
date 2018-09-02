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
import org.springframework.jdbc.core.JdbcTemplate;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class QueryExecutorDaoImpl implements QueryExecutorDao {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(QueryExecutorDaoImpl.class);
    private static final String CLASS_NAME = "QueryExecutorDaoImpl";
    
    @Autowired(required = true)
    private JdbcTemplate jdbcTemplate;
    
    public QueryExecutorDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("{} Constructor initialised..", CLASS_NAME);
    }
    
    @Override
    public List<Map<String, Object>> query(String sql, Object[] data) throws SvcLogicException {
        logger.debug("Query  ({}) with data ({})", sql, data);
        return jdbcTemplate.queryForList(sql, data);
    }
    
    @Override
    public int update(String sql, Object[] data) throws SvcLogicException {
        logger.debug("Query  ({}) with data ({})", sql, data);
        return jdbcTemplate.update(sql, data);
    }
    
}
