/*
 *  Copyright © 2018 IBM.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.service;

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
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.onap.ccsdk.apps.controllerblueprints.service.handler.ModelTypeHandler;
import org.onap.ccsdk.apps.controllerblueprints.service.rs.ModelTypeRestTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringRunner.class)
@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTypeServiceTest {
    private static EELFLogger log = EELFManager.getInstance().getLogger(ModelTypeRestTest.class);
    @Autowired
    private ModelTypeHandler modelTypeHandler;

    String modelName = "test-datatype";

    @Test
    @Commit
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
        modelType = modelTypeHandler.saveModel(modelType);
        log.info("Saved Mode {}", modelType.toString());
        Assert.assertNotNull("Failed to get Saved ModelType", modelType);
        Assert.assertNotNull("Failed to get Saved ModelType, Id", modelType.getModelName());

        ModelType dbModelType = modelTypeHandler.getModelTypeByName(modelType.getModelName());
        Assert.assertNotNull("Failed to query ResourceMapping for ID (" + dbModelType.getModelName() + ")",
                dbModelType);

        // Model Update
        modelType.setUpdatedBy("bs2796@xxx.com");
        modelType = modelTypeHandler.saveModel(modelType);
        Assert.assertNotNull("Failed to get Saved ModelType", modelType);
        Assert.assertEquals("Failed to get Saved getUpdatedBy ", "bs2796@xxx.com", modelType.getUpdatedBy());

    }

    @Test
    public void test02SearchModelTypes() throws Exception {
        log.info("*********************** test02SearchModelTypes  ***************************");

        String tags = "test-datatype";

        List<ModelType> dbModelTypes = modelTypeHandler.searchModelTypes(tags);
        Assert.assertNotNull("Failed to search ResourceMapping by tags", dbModelTypes);
        Assert.assertTrue("Failed to search ResourceMapping by tags count", dbModelTypes.size() > 0);

    }

    @Test
    public void test03GetModelType() throws Exception {
        log.info("************************* test03GetModelType  *********************************");
        ModelType dbModelType = modelTypeHandler.getModelTypeByName(modelName);
        Assert.assertNotNull("Failed to get response for api call getModelByName ", dbModelType);
        Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbModelType.getModelName());

        List<ModelType> dbDatatypeModelTypes =
                modelTypeHandler.getModelTypeByDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE);
        Assert.assertNotNull("Failed to find getModelTypeByDefinitionType by tags", dbDatatypeModelTypes);
        Assert.assertTrue("Failed to find getModelTypeByDefinitionType by count", dbDatatypeModelTypes.size() > 0);

        List<ModelType> dbModelTypeByDerivedFroms =
                modelTypeHandler.getModelTypeByDerivedFrom(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT);
        Assert.assertNotNull("Failed to find getModelTypeByDerivedFrom by tags", dbModelTypeByDerivedFroms);
        Assert.assertTrue("Failed to find getModelTypeByDerivedFrom by count", dbModelTypeByDerivedFroms.size() > 0);

    }

    @Test
    public void test04DeleteModelType() throws Exception {
        log.info(
                "************************ test03DeleteModelType  ***********************");
        ModelType dbResourceMapping = modelTypeHandler.getModelTypeByName(modelName);
        Assert.assertNotNull("Failed to get response for api call getModelByName ", dbResourceMapping);
        Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbResourceMapping.getModelName());

        modelTypeHandler.deleteByModelName(dbResourceMapping.getModelName());
    }
}