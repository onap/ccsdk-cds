package org.onap.ccsdk.config.data.adaptor.dao;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigPropertyMapDao;
import org.onap.ccsdk.config.data.adaptor.dao.QueryExecutorDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-h2db.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigPropertyMapDaoTest {

    @Autowired
    private ConfigPropertyMapDao configPropertyMapDao;

    @Autowired
    private QueryExecutorDao queryExecutorDao;

    @Test
    public void testConfigResourcesData() throws Exception {
        String sql = "INSERT INTO CONFIG_PROPERTY_MAP (reference_key, reference_value) VALUES ( ?, ?)";
        Object[] data = new Object[] {"dummy123", "username123"};
        int result = queryExecutorDao.update(sql, data);
        Assert.assertNotNull("Failed to get Query Result", result);

        String propKeyValye = configPropertyMapDao.getConfigPropertyByKey("org.onap.ccsdk.config.rest.adaptors.test");
        Assert.assertNull("propKeyValue is null", propKeyValye);
    }

}
