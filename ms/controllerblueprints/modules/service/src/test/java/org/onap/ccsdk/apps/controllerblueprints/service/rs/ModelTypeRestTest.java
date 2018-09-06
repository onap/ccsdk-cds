/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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

import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTypeRestTest {
    private static EELFLogger log = EELFManager.getInstance().getLogger(ModelTypeRestTest.class);
    @Autowired
    ModelTypeRest modelTypeRest;

    String modelName = "test-datatype";

    @Before
    public void setUp() {

    }


    @After
    public void tearDown() {
    }

    @Test
    public void test01SaveModelType() throws Exception {
        log.info("**************** test01SaveModelType  ********************");

        String content = JacksonUtils.getClassPathFileContent("model_type/data_type/datatype-property.json");
        ModelType modelType = new ModelType();
        modelType.setDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        modelType.setDerivedFrom(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT);
        modelType.setDescription("Definition for Sample Datatype ");
        modelType.setDefinition(JacksonUtils.jsonNode(content));
        modelType.setModelName(modelName);
        modelType.setVersion("1.0.0");
        modelType.setTags("test-datatype ," + BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT + ","
                + BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        modelType.setUpdatedBy("xxxxxx@xxx.com");
        modelType = modelTypeRest.saveModelType(modelType);
        log.info("Saved Mode {}", modelType.toString());
        Assert.assertNotNull("Failed to get Saved ModelType", modelType);
        Assert.assertNotNull("Failed to get Saved ModelType, Id", modelType.getModelName());

        ModelType dbModelType = modelTypeRest.getModelTypeByName(modelType.getModelName());
        Assert.assertNotNull("Failed to query ResourceMapping for ID (" + dbModelType.getModelName() + ")",
                dbModelType);

        // Model Update
        modelType.setUpdatedBy("bs2796@xxx.com");
        modelType = modelTypeRest.saveModelType(modelType);
        Assert.assertNotNull("Failed to get Saved ModelType", modelType);
        Assert.assertEquals("Failed to get Saved getUpdatedBy ", "bs2796@xxx.com", modelType.getUpdatedBy());

    }

    @Test
    public void test02SearchModelTypes() throws Exception {
        log.info("*********************** test02SearchModelTypes  ***************************");

        String tags = "test-datatype";

        List<ModelType> dbModelTypes = modelTypeRest.searchModelTypes(tags);
        Assert.assertNotNull("Failed to search ResourceMapping by tags", dbModelTypes);
        Assert.assertTrue("Failed to search ResourceMapping by tags count", dbModelTypes.size() > 0);

    }

    @Test
    public void test03GetModelType() throws Exception {
        log.info("************************* test03GetModelType  *********************************");
        ModelType dbModelType = modelTypeRest.getModelTypeByName(modelName);
        Assert.assertNotNull("Failed to get response for api call getModelByName ", dbModelType);
        Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbModelType.getModelName());

        List<ModelType> dbDatatypeModelTypes =
                modelTypeRest.getModelTypeByDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        Assert.assertNotNull("Failed to find getModelTypeByDefinitionType by tags", dbDatatypeModelTypes);
        Assert.assertTrue("Failed to find getModelTypeByDefinitionType by count", dbDatatypeModelTypes.size() > 0);
    }

    @Test
    public void test04DeleteModelType() throws Exception {
        log.info(
                "************************ test03DeleteModelType  ***********************");
        ModelType dbResourceMapping = modelTypeRest.getModelTypeByName(modelName);
        Assert.assertNotNull("Failed to get response for api call getModelByName ", dbResourceMapping);
        Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbResourceMapping.getModelName());

        modelTypeRest.deleteModelTypeByName(dbResourceMapping.getModelName());
    }


}
