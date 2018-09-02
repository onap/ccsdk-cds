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

import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-h2db.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QueryExecutorDaoTest {
    private static EELFLogger logger = EELFManager.getInstance().getLogger(QueryExecutorDaoTest.class);
    
    @Autowired
    private QueryExecutorDao queryExecutorDao;
    
    @Before
    public void initialise() {
        
    }
    
    @Test
    public void testInsertQueryExecution() throws Exception {
        
        String sql = "INSERT INTO CONFIG_RESOURCE"
                + "(config_resource_id, resource_id, resource_type, template_name, recipe_name, request_id, resource_data, mask_data, created_date, updated_by) "
                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        Object[] data =
                new Object[] {"12345", "vUSP - vDBE-IPX HUB", "1234567", "activate-action", "vrr-service-template",
                        "resource-data", "mask-data", null, new Date(System.currentTimeMillis()), "ab1234"};
        int result = queryExecutorDao.update(sql, data);
        logger.info("Updated successfully rows :" + result);
        Assert.assertNotNull("Failed to get Query Result", result);
    }
    
    @Test
    public void testUpdateQueryExecution() throws Exception {
        
        String sql = "UPDATE CONFIG_RESOURCE set recipe_name=? where config_resource_id=?";
        Object[] data = new Object[] {"vce-service-template", "12345"};
        int result = queryExecutorDao.update(sql, data);
        logger.info("Updated successfully rows :" + result);
        Assert.assertNotNull("Failed to get Query Result", result);
    }
    
    @Test
    public void testDeleteQueryExecution() throws Exception {
        
        String sql = "DELETE FROM CONFIG_RESOURCE where config_resource_id=?";
        Object[] data = new Object[] {"12345"};
        int result = queryExecutorDao.update(sql, data);
        logger.info("Updated successfully rows :" + result);
        Assert.assertNotNull("Failed to get Query Result", result);
    }
    
}
