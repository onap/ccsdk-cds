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

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
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
