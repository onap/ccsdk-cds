/*
 * Copyright © 2018 IBM.
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

package org.onap.ccsdk.apps.controllerblueprints.service.validator;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.apps.controllerblueprints.core.BluePrintException;
import org.onap.ccsdk.apps.controllerblueprints.service.domain.ModelType;

public class ModelTypeValidatorTest {

    @Before
    public void setup(){
        ModelTypeValidator modelTypeValidator;
    }

    @Test
    public void testGetValidModelDefinitionType_definitionContentNULL() throws Exception{
        String definitionType=null;
        JsonNode definitionContent=null;
        boolean valid= ModelTypeValidator.validateModelTypeDefinition(definitionType, definitionContent);
        Assert.assertTrue(valid);

    }

    @Test(expected=BluePrintException.class)
    public void testvalidateModelType() throws Exception{
        ModelType modelType = new ModelType();
        modelType.setDefinitionType("");
        modelType.setDerivedFrom("");
        modelType.setDescription("");
        JsonNode definitionContent=null;
        modelType.setDefinition(definitionContent);
        modelType.setModelName("");
        modelType.setVersion("");
        modelType.setTags("");
        modelType.setUpdatedBy("");
        ModelTypeValidator.validateModelType(modelType);
    }
}
