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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.domain.ModelType
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.logger
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
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
class ModelTypeControllerTest {

    private val log = logger(ModelTypeControllerTest::class.java)!!

    @Autowired
    lateinit var modelTypeController: ModelTypeController

    private var modelName = "test-datatype"

    @Test
    @Commit
    @Throws(Exception::class)
    fun test01SaveModelType() {
        runBlocking {
            log.info("**************** test01SaveModelType  ********************")

            val content = JacksonUtils.getClassPathFileContent("model_type/data_type/datatype-property.json")
            var modelType = ModelType()
            modelType.definitionType = BlueprintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
            modelType.derivedFrom = BlueprintConstants.MODEL_TYPE_DATATYPES_ROOT
            modelType.description = "Definition for Sample Datatype "
            modelType.definition = JacksonUtils.jsonNode(content)
            modelType.modelName = modelName
            modelType.version = "1.0.0"
            modelType.tags = (
                "test-datatype ," + BlueprintConstants.MODEL_TYPE_DATATYPES_ROOT + "," +
                    BlueprintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE
                )
            modelType.updatedBy = "xxxxxx@xxx.com"
            modelType = modelTypeController.saveModelType(modelType)
            log.info("Saved Mode {}", modelType.toString())
            Assert.assertNotNull("Failed to get Saved ModelType", modelType)
            Assert.assertNotNull("Failed to get Saved ModelType, Id", modelType.modelName)

            val dbModelType = modelTypeController.getModelTypeByName(modelType.modelName)
            Assert.assertNotNull(
                "Failed to query ResourceMapping for ID (" + dbModelType!!.modelName + ")",
                dbModelType
            )

            // Model Update
            modelType.updatedBy = "bs2796@xxx.com"
            modelType = modelTypeController.saveModelType(modelType)
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
            val dbModelTypes = modelTypeController.searchModelTypes(tags)
            Assert.assertNotNull("Failed to search ResourceMapping by tags", dbModelTypes)
            Assert.assertTrue("Failed to search ResourceMapping by tags count", dbModelTypes.isNotEmpty())
        }
    }

    @Test
    @Throws(Exception::class)
    fun test03GetModelType() {
        runBlocking {
            log.info("************************* test03GetModelType  *********************************")
            val dbModelType = modelTypeController.getModelTypeByName(modelName)
            Assert.assertNotNull("Failed to get response for api call getModelByName $modelName", dbModelType)
            Assert.assertNotNull("Failed to get Id for api call  getModelByName ", dbModelType!!.modelName)

            val dbDatatypeModelTypes =
                modelTypeController.getModelTypeByDefinitionType(BlueprintConstants.MODEL_DEFINITION_TYPE_DATA_TYPE)
            Assert.assertNotNull("Failed to find getModelTypeByDefinitionType by tags", dbDatatypeModelTypes)
            Assert.assertTrue("Failed to find getModelTypeByDefinitionType by count", dbDatatypeModelTypes.isNotEmpty())
        }
    }

    @Test
    @Commit
    @Throws(Exception::class)
    fun test04DeleteModelType() {
        runBlocking {
            log.info("************************ test03DeleteModelType  ***********************")
            val dbResourceMapping = modelTypeController.getModelTypeByName(modelName)
            Assert.assertNotNull("Failed to get response for api call getModelByName ", dbResourceMapping)
            Assert.assertNotNull(
                "Failed to get Id for api call  getModelByName ",
                dbResourceMapping!!.modelName
            )
            modelTypeController.deleteModelTypeByName(dbResourceMapping.modelName)
        }
    }
}
