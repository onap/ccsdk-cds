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

package org.onap.ccsdk.cds.controllerblueprints.service.enhancer

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.TestApplication
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.onap.ccsdk.cds.controllerblueprints.service.load.ModelTypeLoadService
import org.onap.ccsdk.cds.controllerblueprints.service.load.ResourceDictionaryLoadService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = arrayOf(TestApplication::class))
@TestPropertySource(locations = arrayOf("classpath:application.properties"))
class BluePrintEnhancerServiceImplTest {

    @Autowired
    lateinit var modelTypeLoadService: ModelTypeLoadService

    @Autowired
    lateinit var resourceDictionaryLoadService: ResourceDictionaryLoadService

    @Autowired
    lateinit var bluePrintEnhancerService: BluePrintEnhancerService

    @Autowired
    lateinit var bluePrintValidatorService: BluePrintValidatorService

    @Before
    fun init() {
        runBlocking {
            modelTypeLoadService.loadPathModelType("./../../../../components/model-catalog/definition-type/starter-type")
            resourceDictionaryLoadService.loadPathResourceDictionary("./../../../../components/model-catalog/resource-dictionary/starter-dictionary")
            resourceDictionaryLoadService.loadPathResourceDictionary("./../../../../components/model-catalog/resource-dictionary/test-dictionary")
        }
    }

    @Test
    @Throws(Exception::class)
    fun testEnhancementAndValidation() {

        val basePath = "./../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
        testComponentInvokeEnhancementAndValidation(basePath, "base-enhance")
    }

    @Test
    @Throws(Exception::class)
    fun testComponentInvokeEnhancementAndValidation() {
        val basePath = "./../../../../components/model-catalog/blueprint-model/test-blueprint/component_invoke"
        testComponentInvokeEnhancementAndValidation(basePath, "component-enhance")
    }

    @Test
    @Throws(Exception::class)
    fun testGoldenEnhancementAndValidation() {
        val basePath = "./../../../../components/model-catalog/blueprint-model/test-blueprint/golden"
        testComponentInvokeEnhancementAndValidation(basePath, "golden-enhance")
    }

    @Test
    @Throws(Exception::class)
    fun testCapabilityRestconfEnhancementAndValidation() {
        val basePath = "./../../../../components/model-catalog/blueprint-model/test-blueprint/capability_restconf"
        testComponentInvokeEnhancementAndValidation(basePath, "capability_restconf-enhance")

    }

    private fun testComponentInvokeEnhancementAndValidation(basePath: String, targetDirName: String) {
        runBlocking {
            val targetPath = normalizedPathName("target/blueprints/enrichment", targetDirName)

            deleteDir(targetPath)

            val bluePrintContext = bluePrintEnhancerService.enhance(basePath, targetPath)
            Assert.assertNotNull("failed to get blueprintContext ", bluePrintContext)

            // Validate the Generated BluePrints
            val valid = bluePrintValidatorService.validateBluePrints(targetPath)
            Assert.assertTrue("blueprint($basePath) validation failed ", valid)

            deleteDir(targetPath)
        }
    }


}