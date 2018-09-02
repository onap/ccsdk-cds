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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TransactionLogDaoImpl implements TransactionLogDao {
    
    private static EELFLogger logger = EELFManager.getInstance().getLogger(TransactionLogDaoImpl.class);
    
    private JdbcTemplate jdbcTemplate;
    
    public TransactionLogDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public void save(TransactionLog transactionLog) throws SvcLogicException {
        if (transactionLog != null && StringUtils.isNotBlank(transactionLog.getRequestId())) {
            String addSql =
                    "INSERT INTO CONFIG_TRANSACTION_LOG ( config_transaction_log_id, request_id, message_type, message ) VALUES (?, ?, ?, ?) ";
            jdbcTemplate.update(addSql, transactionLog.getUniqueId(), transactionLog.getRequestId(),
                    transactionLog.getMessageType(), transactionLog.getMessage());
            logger.trace("TransactionLog Updated Successfully for message_type {}", transactionLog.getMessageType());
        }
        
    }
    
    @Override
    public List<TransactionLog> getTransactionsByRequestId(String requestId) throws SvcLogicException {
        if (StringUtils.isNotBlank(requestId)) {
            String selectByRequestIdSql =
                    "SELECT * FROM CONFIG_TRANSACTION_LOG WHERE request_id = ? ORDER BY creation_date DESC";
            return this.jdbcTemplate.query(selectByRequestIdSql, new Object[] {requestId}, new TransactionLogMapper());
        } else {
            throw new SvcLogicException("TransactionLog Request id  (" + requestId + ")is missing ");
        }
    }
    
    @Override
    public List<TransactionLog> getTransactionsByRequestId(String requestId, String messageType)
            throws SvcLogicException {
        if (StringUtils.isNotBlank(requestId)) {
            String selectByRequestIdSql =
                    "SELECT * FROM CONFIG_TRANSACTION_LOG WHERE request_id = ? and message_type = ? ORDER BY creation_date DESC";
            return this.jdbcTemplate.query(selectByRequestIdSql, new Object[] {requestId, messageType},
                    new TransactionLogMapper());
        } else {
            throw new SvcLogicException("TransactionLog Request id  (" + requestId + ")is missing ");
        }
    }
    
    private static final class TransactionLogMapper implements RowMapper<TransactionLog> {
        @Override
        public TransactionLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransactionLog transactionLog = new TransactionLog();
            transactionLog.setCreationDate(rs.getDate("creation_date"));
            transactionLog.setMessage(rs.getString("message"));
            transactionLog.setMessageType(rs.getString("message_type"));
            transactionLog.setRequestId(rs.getString("request_id"));
            transactionLog.setTransactionLogId(rs.getString("config_transaction_log_id"));
            return transactionLog;
        }
    }
    
}
