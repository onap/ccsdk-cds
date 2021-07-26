/*
 *  Copyright © 2019 IBM.
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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.repository

import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiTestConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedFile
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Commit
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import java.nio.charset.Charset
import java.util.Arrays

/**
 * ModelTypeReactRepositoryTest.
 *
 * @author Brinda Santh
 */

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ModelTypeReactRepositoryTest {

    @Autowired
    private val modelTypeReactRepository: ModelTypeReactRepository? = null

    internal var modelName = "test-datatype"

    @Test
    @Commit
    fun test01Save() {
        val content = normalizedFile("./src/test/resources/model_type/data_type/datatype-property.json")
            .readText(Charset.defaultCharset())
        val modelType = ModelType()
        modelType.definitionType = BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
        modelType.derivedFrom = BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT
        modelType.description = "Definition for Sample Datatype "
        modelType.definition = JacksonUtils.jsonNode(content)
        modelType.modelName = modelName
        modelType.version = "1.0.0"
        modelType.tags = (
            "test-datatype ," + BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT + "," +
                BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
            )
        modelType.updatedBy = "xxxxxx@xxx.com"

        val dbModelType = modelTypeReactRepository!!.save(modelType).block()
        Assert.assertNotNull("Failed to get Saved ModelType", dbModelType)
    }

    @Test
    fun test02Finds() {
        val dbFindByModelName = modelTypeReactRepository!!.findByModelName(modelName).block()
        Assert.assertNotNull("Failed to findByModelName ", dbFindByModelName)

        val dbFindByDefinitionType =
            modelTypeReactRepository.findByDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE).collectList().block()
        Assert.assertNotNull("Failed to findByDefinitionType ", dbFindByDefinitionType)
        Assert.assertTrue("Failed to findByDefinitionType count", dbFindByDefinitionType!!.size > 0)

        val dbFindByDerivedFrom = modelTypeReactRepository.findByDerivedFrom(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT).collectList().block()
        Assert.assertNotNull("Failed to find findByDerivedFrom", dbFindByDerivedFrom)
        Assert.assertTrue("Failed to find findByDerivedFrom by count", dbFindByDerivedFrom!!.size > 0)

        val dbFindByModelNameIn = modelTypeReactRepository.findByModelNameIn(Arrays.asList(modelName)).collectList().block()
        Assert.assertNotNull("Failed to findByModelNameIn ", dbFindByModelNameIn)
        Assert.assertTrue("Failed to findByModelNameIn by count", dbFindByModelNameIn!!.size > 0)

        val dbFindByDefinitionTypeIn =
            modelTypeReactRepository.findByDefinitionTypeIn(Arrays.asList(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)).collectList().block()
        Assert.assertNotNull("Failed to findByDefinitionTypeIn", dbFindByDefinitionTypeIn)
        Assert.assertTrue("Failed to findByDefinitionTypeIn by count", dbFindByDefinitionTypeIn!!.size > 0)

        val dbFindByDerivedFromIn =
            modelTypeReactRepository.findByDerivedFromIn(Arrays.asList(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT)).collectList().block()
        Assert.assertNotNull("Failed to find findByDerivedFromIn", dbFindByDerivedFromIn)
        Assert.assertTrue("Failed to find findByDerivedFromIn by count", dbFindByDerivedFromIn!!.size > 0)
    }

    @Test
    @Commit
    fun test03Delete() {
        modelTypeReactRepository!!.deleteByModelName(modelName).block()
    }
}
