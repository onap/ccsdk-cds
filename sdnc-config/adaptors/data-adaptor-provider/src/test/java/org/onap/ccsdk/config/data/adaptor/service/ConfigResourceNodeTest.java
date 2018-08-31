package org.onap.ccsdk.config.data.adaptor.service;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.config.data.adaptor.DataAdaptorConstants;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigPropertyMapDao;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigResourceDao;
import org.onap.ccsdk.config.data.adaptor.dao.NamedQueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.QueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.TransactionLogDao;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-h2db.xml"})
public class ConfigResourceNodeTest {

    ConfigResourceNode configResourceNode;

    ConfigResourceService configResourceService;

    @Autowired
    TransactionLogDao transactionLogDao;

    @Autowired
    ConfigResourceDao configResourceDao;

    @Autowired
    QueryExecutorDao queryExecutorDao;

    @Autowired
    NamedQueryExecutorDao namedQueryExecutorDao;

    @Autowired
    ConfigPropertyMapDao configPropertyMapDao;

    @Before
    public void before() {
        configResourceService = new ConfigResourceServiceImpl(transactionLogDao, configResourceDao, queryExecutorDao,
                namedQueryExecutorDao, configPropertyMapDao);
        configResourceNode = new ConfigResourceNode(configResourceService);
    }

    @Test
    public void testSaveConfigTransactionLog() throws Exception {
        Map<String, String> inParams = new HashMap<>();
        inParams.put(DataAdaptorConstants.INPUT_PARAM_MESSAGE_TYPE, "messageType");
        inParams.put(DataAdaptorConstants.INPUT_PARAM_MESSAGE, "message");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("request-id", "requestId12345");

        configResourceNode.saveConfigTransactionLog(inParams, ctx);

        Assert.assertTrue(!transactionLogDao.getTransactionsByRequestId("requestId12345").isEmpty());
    }

    @Test(expected = SvcLogicException.class)
    public void testSaveConfigTransactionLogException() throws Exception {
        configResourceNode = new ConfigResourceNode(null);
        configResourceNode.saveConfigTransactionLog(new HashMap<>(), new SvcLogicContext());
    }
}
