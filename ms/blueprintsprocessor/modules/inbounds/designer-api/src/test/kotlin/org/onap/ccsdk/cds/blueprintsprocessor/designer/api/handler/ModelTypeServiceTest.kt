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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.handler

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiTestConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Commit
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ModelTypeServiceTest {

    @Autowired
    private val modelTypeHandler: ModelTypeHandler? = null

    internal var modelName = "test-datatype"

    private val log = LoggerFactory.getLogger(ModelTypeServiceTest::class.java)

    @Test
    @Commit
    @Throws(Exception::class)
    fun test01SaveModelType() {
        runBlocking {
            log.info("**************** test01SaveModelType  ********************")

            val content = JacksonUtils.getClassPathFileContent("model_type/data_type/datatype-property.json")
            var modelType = ModelType()
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
            modelType = modelTypeHandler!!.saveModel(modelType)
            log.info("Saved Mode {}", modelType.toString())
            Assert.assertNotNull("Failed to get Saved ModelType", modelType)
            Assert.assertNotNull("Failed to get Saved ModelType, Id", modelType.modelName)

            val dbModelType = modelTypeHandler.getModelTypeByName(modelType.modelName)
            Assert.assertNotNull(
                "Failed to query ResourceMapping for ID (" + dbModelType!!.modelName + ")",
                dbModelType
            )

            // Model Update
            modelType.updatedBy = "bs2796@xxx.com"
            modelType = modelTypeHandler.saveModel(modelType)
            Assert.assertNotNull("Failed to get Saved ModelType", modelType)
            Assert.assertEquals("Failed to get Saved getUpdatedBy ", "bs2796@xxx.com", modelType.updatedBy)
        }
    }

    @Test
    @Throws(Exception::class)
    fun test02SearchModelTypes() {
        runBlocking {
            log.info("*********************** test02SearchModelTypes  ***************************")

            val tags = "test-datatype"

            val dbModelTypes = modelTypeHandler!!.searchModelTypes(tags)
            Assert.assertNotNull("Failed to search ResourceMapping by tags", dbModelTypes)
            Assert.assertTrue("Failed to search ResourceMapping by tags count", dbModelTypes.size > 0)
        }
    }

    @Test
    @Throws(Exception::class)
    fun test03GetModelType() {
        runBlocking {
            log.info("************************* test03GetModelType  *********************************")
            val dbModelType = modelTypeHandler!!.getModelTypeByName(modelName)
            Assert.assertNotNull("Failed to get response for api call getModelByName ", dbModelType)
            Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbModelType!!.modelName)

            val dbDatatypeModelTypes = modelTypeHandler.getModelTypeByDefinitionType(BluePrintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
            Assert.assertNotNull("Failed to find getModelTypeByDefinitionType by tags", dbDatatypeModelTypes)
            Assert.assertTrue("Failed to find getModelTypeByDefinitionType by count", dbDatatypeModelTypes.size > 0)

            val dbModelTypeByDerivedFroms = modelTypeHandler.getModelTypeByDerivedFrom(BluePrintConstants.MODEL_TYPE_DATATYPES_ROOT)
            Assert.assertNotNull("Failed to find getModelTypeByDerivedFrom by tags", dbModelTypeByDerivedFroms)
            Assert.assertTrue("Failed to find getModelTypeByDerivedFrom by count", dbModelTypeByDerivedFroms.size > 0)
        }
    }

    @Test
    @Throws(Exception::class)
    fun test04DeleteModelType() {
        runBlocking {
            log.info(
                "************************ test03DeleteModelType  ***********************"
            )
            val dbResourceMapping = modelTypeHandler!!.getModelTypeByName(modelName)
            Assert.assertNotNull("Failed to get response for api call getModelByName ", dbResourceMapping)
            Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbResourceMapping!!.modelName)

            modelTypeHandler.deleteByModelName(dbResourceMapping.modelName)
        }
    }
}
