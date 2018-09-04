/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.service.rs;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ConfigModel;
import org.onap.ccsdk.apps.controllerblueprints.service.utils.ConfigModelUtils;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConfigModelRestTest {

    private static EELFLogger log = EELFManager.getInstance().getLogger(ConfigModelRestTest.class);

    @Autowired
    ConfigModelRest configModelRest;

    ConfigModel configModel;

    String name = "vrr-test";
    String version = "1.0.0";

    @Before
    public void setUp() {

    }


    @After
    public void tearDown() {
    }


    @Test
    public void test01getInitialConfigModel() throws Exception {
        log.info("** test01getInitialConfigModel  *****************");

        String name = "default_netconf";
        ConfigModel configModel = configModelRest.getInitialConfigModel(name);
        Assert.assertNotNull("Failed to get Initial Config Model , Return object is Null", configModel);
        Assert.assertNotNull("Failed to get Service Template Content ", configModel.getConfigModelContents());
    }


    @Test
    public void test02SaveServiceTemplate() throws Exception {
        log.info("************************ test02SaveServiceTemplate  ******************");


        configModel = ConfigModelUtils.getConfigModel("load/blueprints/vrr-test");

        configModel = configModelRest.saveConfigModel(configModel);
        Assert.assertNotNull("Failed to ConfigModel, Return object is Null", configModel);
        Assert.assertNotNull("Failed to ConfigModel Id , Return ID object is Null", configModel.getId());
        Assert.assertNotNull("Failed to ConfigModel Content, Return object is Null",
                configModel.getConfigModelContents());
        Assert.assertEquals("Failed in validation of ConfigModel Content count,", 3,
                configModel.getConfigModelContents().size());

        ConfigModel dbconfigModel = configModelRest.getConfigModel(configModel.getId());

        log.info("************************ test02SaveServiceTemplate-2  ******************");

        dbconfigModel.getConfigModelContents().remove(2);
        dbconfigModel = configModelRest.saveConfigModel(dbconfigModel);
        log.info("Saved Config Model " + configModel.getId());
        Assert.assertNotNull("Failed to ConfigModel, Return object is Null", dbconfigModel);
        Assert.assertNotNull("Failed to ConfigModel Id ", dbconfigModel.getId());
        Assert.assertNotNull("Failed to ConfigModel Content",
                dbconfigModel.getConfigModelContents());
        Assert.assertEquals("Failed to Remove the ConfigModel Content,", 2,
                dbconfigModel.getConfigModelContents().size());


    }


    @Test
    public void test03PublishServiceTemplate() throws Exception {
        log.info("** test03PublishServiceTemplate  *****************");

        ConfigModel configModel = configModelRest.getConfigModelByNameAndVersion(name, version);
        log.info("Publishing Config Model " + configModel.getId());
        configModel = configModelRest.publishConfigModel(configModel.getId());
        Assert.assertNotNull("Failed to ConfigModel, Return object is Null", configModel);
        Assert.assertNotNull("Failed to ConfigModel Id ", configModel.getId());
        Assert.assertNotNull("Failed to ConfigModel Content", configModel.getConfigModelContents());
        Assert.assertEquals("Failed to update the publish indicator", "Y", configModel.getPublished());
    }


    @Test
    public void test04GetConfigModel() throws Exception {
        log.info("** test04GetConfigModel  *****************");

        ConfigModel configModel = configModelRest.getConfigModelByNameAndVersion(name, version);
        Assert.assertNotNull("Failed to get ConfigModel for the Name (" + configModel.getArtifactName() + ") and ("
                + configModel.getArtifactVersion() + ")", configModel);
        Assert.assertNotNull("Failed to get ConfigModel Id", configModel.getId());

        configModel = configModelRest.getConfigModel(configModel.getId());
        Assert.assertNotNull("Failed to get ConfigModel for the Id (" + configModel.getId() + ") ", configModel);

    }

    @Test
    public void test05GetCloneConfigModel() throws Exception {
        log.info("** test05GetCloneConfigModel  *****************");

        ConfigModel configModel = configModelRest.getConfigModelByNameAndVersion(name, version);

        Assert.assertNotNull("Failed to get ConfigModel for the Name (" + configModel.getArtifactName() + ") and ("
                + configModel.getArtifactVersion() + ")", configModel);
        Assert.assertNotNull("Failed to get ConfigModel Id", configModel.getId());

        configModel = configModelRest.getCloneConfigModel(configModel.getId());
        Assert.assertNotNull("Failed to get ConfigModel for the Id (" + configModel.getId() + ") ", configModel);
    }


    @Test
    public void test07SearchConfigModels() throws Exception {
        log.info("** test07SearchConfigModels  *****************");

        List<ConfigModel> configModels = configModelRest.searchConfigModels("vrr-test");
        Assert.assertNotNull("Failed to search ConfigModel", configModels);
        Assert.assertTrue("Failed to search ConfigModel with count", configModels.size() > 0);
        // update the ServiceModelContent
    }


    @Test
    public void test08DeleteConfigModels() throws Exception {
        log.info("** test08DeleteConfigModels  *****************");

        ConfigModel configModel = configModelRest.getConfigModelByNameAndVersion(name, version);
        configModelRest.deleteConfigModel(configModel.getId());

    }


}
