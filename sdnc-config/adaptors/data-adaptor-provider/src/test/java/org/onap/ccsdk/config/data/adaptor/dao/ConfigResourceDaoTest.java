package org.onap.ccsdk.config.data.adaptor.dao;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.config.data.adaptor.dao.ConfigResourceDao;
import org.onap.ccsdk.config.data.adaptor.domain.ConfigResource;
import org.onap.ccsdk.config.data.adaptor.domain.ResourceAssignmentData;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-h2db.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigResourceDaoTest {


    private static EELFLogger logger = EELFManager.getInstance().getLogger(ConfigResourceDaoTest.class);

    @Autowired
    private ConfigResourceDao configResourceDao;

    @Before
    public void initialise() {

    }

    @Test
    public void testAssignmentResourceData() throws Exception {
        ConfigResource configResource = new ConfigResource();
        String resourceData = IOUtils.toString(
                ConfigResourceDaoTest.class.getClassLoader().getResourceAsStream("reference/resource_data.json"),
                Charset.defaultCharset());

        logger.trace("resourceData = " + resourceData);
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

        ConfigResource dbConfigResource = configResourceDao.save(configResource);
        logger.info("Saved sucessfully : " + dbConfigResource.toString());
        Assert.assertNotNull("ConfigResource is null", dbConfigResource);
        Assert.assertNotNull("Resource Assignment Data is null", dbConfigResource.getResourceAssignments());
        Assert.assertEquals("Resource Assignment Data count missmatch", true,
                dbConfigResource.getResourceAssignments().size() > 0);
    }

    @Test
    public void testConfigResourcesData() throws Exception {
        ConfigResource configResourceInput = new ConfigResource();
        configResourceInput.setResourceId("123456");
        List<ConfigResource> dbConfigResources = configResourceDao.findByConfigResource(configResourceInput);
        Assert.assertNotNull("ConfigResources is null", dbConfigResources);
        Assert.assertEquals("ConfigResources size missmatch", true, dbConfigResources.size() > 0);

        for (ConfigResource configResource : dbConfigResources) {
            Assert.assertNotNull("ConfigResources Assignments is null", configResource.getResourceAssignments());
            Assert.assertTrue("ConfigResources Assignments size miss mathch ",
                    configResource.getResourceAssignments().size() > 0);
            logger.trace("ResourceAssignments = " + configResource.getResourceAssignments());
        }
    }

    @Test
    public void testDeleteByConfigResource() throws Exception {
        ConfigResource configResourceInput = new ConfigResource();
        configResourceInput.setResourceId("123456");

        List<ConfigResource> dbConfigResources = configResourceDao.findByConfigResource(configResourceInput);
        Assert.assertTrue("ConfigResources is null", !dbConfigResources.isEmpty());

        configResourceInput.setConfigResourceId(dbConfigResources.get(0).getConfigResourceId());
        configResourceDao.deleteByConfigResource(configResourceInput);

        dbConfigResources = configResourceDao.findByConfigResource(configResourceInput);
        Assert.assertTrue("ConfigResources is not null", dbConfigResources.isEmpty());
    }
}
