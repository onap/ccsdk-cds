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
import org.onap.ccsdk.config.data.adaptor.dao.ConfigPropertyMapDao;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigResourceDao;
import org.onap.ccsdk.config.data.adaptor.dao.NamedQueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.QueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.TransactionLogDao;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ConfigResourceServiceImpl implements ConfigResourceService {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigResourceServiceImpl.class);
    private static final String CLASS_NAME = "ConfigResourceServiceImpl";
    
    private TransactionLogDao transactionLogDao;
    private ConfigResourceDao configResourceDao;
    private QueryExecutorDao queryExecutorDao;
    private NamedQueryExecutorDao namedQueryExecutorDao;
    private ConfigPropertyMapDao configPropertyMapDao;
    
    @SuppressWarnings("squid:S00107")
    public ConfigResourceServiceImpl(TransactionLogDao transactionLogDao, ConfigResourceDao configResourceDao,
            QueryExecutorDao queryExecutorDao, NamedQueryExecutorDao namedQueryExecutorDao,
            ConfigPropertyMapDao configPropertyMapDao) {
        
        logger.info("{} Constuctor Initated...", CLASS_NAME);
        this.transactionLogDao = transactionLogDao;
        this.configResourceDao = configResourceDao;
        this.queryExecutorDao = queryExecutorDao;
        this.namedQueryExecutorDao = namedQueryExecutorDao;
        this.configPropertyMapDao = configPropertyMapDao;
    }
    
    @Override
    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() throws SvcLogicException {
        return namedQueryExecutorDao.getNamedParameterJdbcTemplate();
    }
    
    @Override
    public List<Map<String, Object>> query(String sql, Map<String, Object> parameters) throws SvcLogicException {
        return namedQueryExecutorDao.query(sql, parameters);
    }
    
    @Override
    public int update(String sql, Map<String, Object> parameters) throws SvcLogicException {
        return namedQueryExecutorDao.update(sql, parameters);
    }
    
    @Override
    public List<Map<String, Object>> query(String sql, Object[] data) throws SvcLogicException {
        return queryExecutorDao.query(sql, data);
    }
    
    @Override
    public int update(String sql, Object[] data) throws SvcLogicException {
        return queryExecutorDao.update(sql, data);
    }
    
    @Override
    public void save(TransactionLog transactionLog) throws SvcLogicException {
        transactionLogDao.save(transactionLog);
    }
    
    @Override
    public List<TransactionLog> getTransactionsByRequestId(String requestId) throws SvcLogicException {
        return transactionLogDao.getTransactionsByRequestId(requestId);
    }
    
    @Override
    public List<TransactionLog> getTransactionsByRequestId(String requestId, String messageType)
            throws SvcLogicException {
        return transactionLogDao.getTransactionsByRequestId(requestId, messageType);
    }
    
    @Override
    public List<ConfigResource> getConfigResource(ConfigResource configResource) throws SvcLogicException {
        return configResourceDao.findByConfigResource(configResource);
    }
    
    @Override
    public ConfigResource saveConfigResource(ConfigResource configResource) throws SvcLogicException {
        return configResourceDao.save(configResource);
    }
    
    @Override
    public String getConfigPropertyByKey(String key) throws SvcLogicException {
        return configPropertyMapDao.getConfigPropertyByKey(key);
    }
    
}
