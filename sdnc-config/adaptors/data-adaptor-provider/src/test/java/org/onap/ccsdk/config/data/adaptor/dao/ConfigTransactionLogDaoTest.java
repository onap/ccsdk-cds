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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.config.data.adaptor.DataAdaptorConstants;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-h2db.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigTransactionLogDaoTest {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigTransactionLogDaoTest.class);
    
    @Autowired
    private TransactionLogDao transactionLogDao;
    
    @Autowired
    private NamedQueryExecutorDao namedQueryExecutorDao;
    
    @Before
    public void initialise() {
        
    }
    
    @Test
    public void testQueryExecution() throws Exception {
        String requestId = "12345";
        
        transactionLogDao
                .save(new TransactionLog(requestId, DataAdaptorConstants.LOG_MESSAGE_TYPE_LOG, "Received Request"));
        
        List<TransactionLog> result = transactionLogDao.getTransactionsByRequestId(requestId);
        logger.info("DB ArtifactReference :" + result);
        Assert.assertNotNull("Failed to get Query Result", result);
        
        List<TransactionLog> result2 =
                transactionLogDao.getTransactionsByRequestId(requestId, DataAdaptorConstants.LOG_MESSAGE_TYPE_LOG);
        logger.info("DB ArtifactReference :" + result2);
        Assert.assertNotNull("Failed to get Query Result", result2);
        
        String namedsql = "SELECT * FROM CONFIG_TRANSACTION_LOG WHERE request_id = :request_id";
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("request_id", "12345");
        List<Map<String, Object>> namedresult = namedQueryExecutorDao.query(namedsql, parameters);
        logger.info("DB ArtifactReference :" + namedresult);
        Assert.assertNotNull("Failed to get Query Result", namedresult);
        
    }
    
}
