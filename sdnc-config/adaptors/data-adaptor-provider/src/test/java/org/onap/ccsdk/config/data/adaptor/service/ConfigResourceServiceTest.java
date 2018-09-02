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

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigPropertyMapDao;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigResourceDao;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigResourceDaoTest;
import org.onap.ccsdk.config.data.adaptor.dao.NamedQueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.QueryExecutorDao;
import org.onap.ccsdk.config.data.adaptor.dao.TransactionLogDao;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.ResourceAssignmentData;
import org.onap.ccsdk.config.data.adaptor.domain.TransactionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-h2db.xml"})
public class ConfigResourceServiceTest {
    
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
    }
    
    @Test
    public void testUpdate() throws Exception {
        String sql = "INSERT INTO CONFIG_RESOURCE"
                + "(config_resource_id, resource_id, resource_type, template_name, recipe_name, request_id, resource_data, mask_data, created_date, updated_by) "
                + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";
        Object[] data =
                new Object[] {"54321", "vUSP - vDBE-IPX HUB", "1234567", "activate-action", "vrr-service-template",
                        "resource-data", "mask-data", null, new Date(System.currentTimeMillis()), "ab1234"};
        int result = configResourceService.update(sql, data);
        Assert.assertTrue(result == 1);
        
        sql = "SELECT * FROM CONFIG_RESOURCE WHERE config_resource_id = ?";
        data = new Object[] {"54321"};
        List<Map<String, Object>> queryResult = configResourceService.query(sql, data);
        Assert.assertTrue(queryResult.size() == 1);
    }
    
    @Test
    public void testSaveAndGetConfigResource() throws Exception {
        ConfigResource configResource = new ConfigResource();
        String resourceData = IOUtils.toString(
                ConfigResourceDaoTest.class.getClassLoader().getResourceAsStream("reference/resource_data.json"),
                Charset.defaultCharset());
        
        configResource.setResourceData(resourceData);
        configResource.setServiceTemplateName("sample-name");
        configResource.setServiceTemplateVersion("1.0.0");
        configResource.setResourceId("123456");
        configResource.setResourceType("vUSP - vDBE-IPX HUB");
        configResource.setRequestId("123456");
        configResource.setRecipeName("activate-action");
        configResource.setTemplateName("vrr-service-template");
        configResource.setMaskData(null);
        configResource.setStatus("success");
        configResource.setCreatedDate(new Date(System.currentTimeMillis()));
        configResource.setUpdatedBy("an188a");
        
        List<ResourceAssignmentData> resourceAssignments = new ArrayList<>();
        ResourceAssignmentData resourceAssignmentData = new ResourceAssignmentData();
        resourceAssignmentData.setDataType("string");
        resourceAssignmentData.setStatus("success");
        resourceAssignmentData.setMessage("success");
        resourceAssignmentData.setTemplateKeyName("sample");
        resourceAssignmentData.setResourceName("sample");
        // resourceAssignmentData.setResourceValue("sample123");
        resourceAssignmentData.setSource("input");
        resourceAssignments.add(resourceAssignmentData);
        configResource.setResourceAssignments(resourceAssignments);
        
        // save
        ConfigResource dbConfigResource = configResourceService.saveConfigResource(configResource);
        Assert.assertNotNull("ConfigResource is null", dbConfigResource);
        Assert.assertNotNull("Resource Assignment Data is null", dbConfigResource.getResourceAssignments());
        Assert.assertEquals("Resource Assignment Data count missmatch", true,
                dbConfigResource.getResourceAssignments().size() > 0);
        Assert.assertEquals(configResource.getServiceTemplateVersion(), dbConfigResource.getServiceTemplateVersion());
        
        // update
        configResource.setServiceTemplateVersion("1.0.1");
        dbConfigResource = configResourceService.saveConfigResource(configResource);
        Assert.assertNotNull("ConfigResource is null", dbConfigResource);
        Assert.assertNotNull("Resource Assignment Data is null", dbConfigResource.getResourceAssignments());
        Assert.assertEquals("Resource Assignment Data count missmatch", true,
                dbConfigResource.getResourceAssignments().size() > 0);
        Assert.assertEquals(configResource.getServiceTemplateVersion(), dbConfigResource.getServiceTemplateVersion());
        
        // find
        ConfigResource configResourceInput = new ConfigResource();
        configResourceInput.setResourceId(configResource.getResourceId());
        configResourceInput.setTemplateName(configResource.getTemplateName());
        configResourceInput.setServiceTemplateName(configResource.getServiceTemplateName());
        configResourceInput.setServiceTemplateVersion(configResource.getServiceTemplateVersion());
        configResourceInput.setRequestId(configResource.getRequestId());
        configResourceInput.setRecipeName(configResource.getRecipeName());
        configResourceInput.setResourceType(configResource.getResourceType());
        List<ConfigResource> dbConfigResources = configResourceService.getConfigResource(configResourceInput);
        Assert.assertNotNull("ConfigResources is null", dbConfigResources);
        Assert.assertEquals("ConfigResources size missmatch", true, dbConfigResources.size() > 0);
        
        for (ConfigResource dbConfigResouce : dbConfigResources) {
            Assert.assertNotNull("ConfigResources Assignments is null", dbConfigResouce.getResourceAssignments());
            Assert.assertTrue("ConfigResources Assignments size miss mathch ",
                    dbConfigResouce.getResourceAssignments().size() > 0);
        }
    }
    
    @Test
    public void testSaveAndGetTransactionLog() throws Exception {
        TransactionLog transactionLog = new TransactionLog();
        transactionLog.setMessage("message");
        transactionLog.setMessageType("messageType");
        transactionLog.setRequestId("requestId");
        
        configResourceService.save(transactionLog);
        
        List<TransactionLog> transactions =
                configResourceService.getTransactionsByRequestId(transactionLog.getRequestId());
        Assert.assertTrue(transactions.size() == 1);
        transactions = configResourceService.getTransactionsByRequestId(transactionLog.getRequestId(),
                transactionLog.getMessageType());
        Assert.assertTrue(transactions.size() == 1);
    }
    
    @Test
    public void testNamedQueryExecutorUpdateNQuery() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("config_transaction_log_id", UUID.randomUUID().toString());
        parameters.put("request_id", "requestId123");
        parameters.put("message_type", "messageType");
        parameters.put("message", "message");
        configResourceService.update(
                "INSERT INTO CONFIG_TRANSACTION_LOG ( config_transaction_log_id, request_id, message_type, message ) VALUES (:config_transaction_log_id, :request_id, :message_type, :message) ",
                parameters);
        
        List<Map<String, Object>> result = configResourceService
                .query("SELECT * FROM CONFIG_TRANSACTION_LOG WHERE request_id = :request_id", parameters);
        
        Assert.assertTrue(!result.isEmpty());
        Assert.assertNotNull(configResourceService.getNamedParameterJdbcTemplate());
    }
}
