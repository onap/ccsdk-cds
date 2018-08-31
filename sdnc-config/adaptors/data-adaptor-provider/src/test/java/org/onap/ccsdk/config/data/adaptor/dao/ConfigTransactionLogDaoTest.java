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
import org.onap.ccsdk.config.data.adaptor.dao.NamedQueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.TransactionLogDao;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
