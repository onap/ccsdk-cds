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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.controller.ModelTypeController;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTypeRestTest {
    private static EELFLogger log = EELFManager.getInstance().getLogger(ModelTypeRestTest.class);
    @Autowired
    ModelTypeController modelTypeController;

    String modelName = "test-datatype";

    @Test
    @Commit
    public void test01SaveModelType() throws Exception {
        log.info("**************** test01SaveModelType  ********************");

        String content = JacksonUtils.Companion.getClassPathFileContent("model_type/data_type/datatype-property.json");
        ModelType modelType = new ModelType();
        modelType.setDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        modelType.setDerivedFrom(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT);
        modelType.setDescription("Definition for Sample Datatype ");
        modelType.setDefinition(JacksonUtils.Companion.jsonNode(content));
        modelType.setModelName(modelName);
        modelType.setVersion("1.0.0");
        modelType.setTags("test-datatype ," + BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT + ","
                + BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        modelType.setUpdatedBy("xxxxxx@xxx.com");
        modelType = modelTypeController.saveModelType(modelType);
        log.info("Saved Mode {}", modelType.toString());
        Assert.assertNotNull("Failed to get Saved ModelType", modelType);
        Assert.assertNotNull("Failed to get Saved ModelType, Id", modelType.getModelName());

        ModelType dbModelType = modelTypeController.getModelTypeByName(modelType.getModelName());
        Assert.assertNotNull("Failed to query ResourceMapping for ID (" + dbModelType.getModelName() + ")",
                dbModelType);

        // Model Update
        modelType.setUpdatedBy("bs2796@xxx.com");
        modelType = modelTypeController.saveModelType(modelType);
        Assert.assertNotNull("Failed to get Saved ModelType", modelType);
        Assert.assertEquals("Failed to get Saved getUpdatedBy ", "bs2796@xxx.com", modelType.getUpdatedBy());

    }

    @Test
    public void test02SearchModelTypes() throws Exception {
        log.info("*********************** test02SearchModelTypes  ***************************");

        String tags = "test-datatype";

        List<ModelType> dbModelTypes = modelTypeController.searchModelTypes(tags);
        Assert.assertNotNull("Failed to search ResourceMapping by tags", dbModelTypes);
        Assert.assertTrue("Failed to search ResourceMapping by tags count", dbModelTypes.size() > 0);

    }

    @Test
    public void test03GetModelType() throws Exception {
        log.info("************************* test03GetModelType  *********************************");
        ModelType dbModelType = modelTypeController.getModelTypeByName(modelName);
        Assert.assertNotNull("Failed to get response for api call getModelByName " + modelName, dbModelType);
        Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbModelType.getModelName());

        List<ModelType> dbDatatypeModelTypes =
                modelTypeController.getModelTypeByDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        Assert.assertNotNull("Failed to find getModelTypeByDefinitionType by tags", dbDatatypeModelTypes);
        Assert.assertTrue("Failed to find getModelTypeByDefinitionType by count", dbDatatypeModelTypes.size() > 0);
    }

    @Test
    @Commit
    public void test04DeleteModelType() throws Exception {
        log.info(
                "************************ test03DeleteModelType  ***********************");
        ModelType dbResourceMapping = modelTypeController.getModelTypeByName(modelName);
        Assert.assertNotNull("Failed to get response for api call getModelByName ", dbResourceMapping);
        Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbResourceMapping.getModelName());

        modelTypeController.deleteModelTypeByName(dbResourceMapping.getModelName());
    }


}
