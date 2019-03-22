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

package org.onap.ccsdk.apps.controllerblueprints.service.repository;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.apps.controllerblueprints.TestApplication;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintConstants;
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

/**
 * ModelTypeReactRepositoryTest.
 *
 * @author Brinda Santh
 */

@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {TestApplication.class})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModelTypeReactRepositoryTest {

    @Autowired
    private ModelTypeReactRepository modelTypeReactRepository;

    String modelName = "test-datatype";

    @Test
    @Commit
    public void test01Save() {
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

        ModelType dbModelType = modelTypeReactRepository.save(modelType).block();
        Assert.assertNotNull("Failed to get Saved ModelType", dbModelType);
    }

    @Test
    public void test02Finds() {
        ModelType dbFindByModelName = modelTypeReactRepository.findByModelName(modelName).block();
        Assert.assertNotNull("Failed to findByModelName ", dbFindByModelName);

        List<ModelType> dbFindByDefinitionType =
                modelTypeReactRepository.findByDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE).collectList().block();
        Assert.assertNotNull("Failed to findByDefinitionType ", dbFindByDefinitionType);
        Assert.assertTrue("Failed to findByDefinitionType count", dbFindByDefinitionType.size() > 0);

        List<ModelType> dbFindByDerivedFrom =
                modelTypeReactRepository.findByDerivedFrom(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT).collectList().block();
        Assert.assertNotNull("Failed to find findByDerivedFrom", dbFindByDerivedFrom);
        Assert.assertTrue("Failed to find findByDerivedFrom by count", dbFindByDerivedFrom.size() > 0);

        List<ModelType> dbFindByModelNameIn =
                modelTypeReactRepository.findByModelNameIn(Arrays.asList(modelName)).collectList().block();
        Assert.assertNotNull("Failed to findByModelNameIn ", dbFindByModelNameIn);
        Assert.assertTrue("Failed to findByModelNameIn by count", dbFindByModelNameIn.size() > 0);

        List<ModelType> dbFindByDefinitionTypeIn =
                modelTypeReactRepository.findByDefinitionTypeIn(Arrays.asList(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)).collectList().block();
        Assert.assertNotNull("Failed to findByDefinitionTypeIn", dbFindByDefinitionTypeIn);
        Assert.assertTrue("Failed to findByDefinitionTypeIn by count", dbFindByDefinitionTypeIn.size() > 0);

        List<ModelType> dbFindByDerivedFromIn =
                modelTypeReactRepository.findByDerivedFromIn(Arrays.asList(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT)).collectList().block();
        Assert.assertNotNull("Failed to find findByDerivedFromIn", dbFindByDerivedFromIn);
        Assert.assertTrue("Failed to find findByDerivedFromIn by count", dbFindByDerivedFromIn.size() > 0);
    }

    @Test
    @Commit
    public void test03Delete() {
        modelTypeReactRepository.deleteByModelName(modelName).block();
    }

}
