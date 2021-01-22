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

package org.onap.ccsdk.cds.blueprintsprocessor.designer.api.enhancer

import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.DesignerApiTestConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load.ModelTypeLoadService
import org.onap.ccsdk.cds.blueprintsprocessor.designer.api.load.ResourceDictionaryLoadService
import org.onap.ccsdk.cds.controllerblueprints.core.deleteDir
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintEnhancerService
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BlueprintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.normalizedPathName
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [DesignerApiTestConfiguration::class]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintEnhancerServiceImplTest {

    @Autowired
    lateinit var modelTypeLoadService: ModelTypeLoadService

    @Autowired
    lateinit var resourceDictionaryLoadService: ResourceDictionaryLoadService

    @Autowired
    lateinit var bluePrintEnhancerService: BlueprintEnhancerService

    @Autowired
    lateinit var bluePrintValidatorService: BlueprintValidatorService

    @Test
    @Throws(Exception::class)
    fun testEnhancementAndValidation() {

        runBlocking {
            modelTypeLoadService.loadPathModelType("./../../../../../components/model-catalog/definition-type/starter-type")

            val dictPaths: MutableList<String> = arrayListOf()
            dictPaths.add("./../../../../../components/model-catalog/resource-dictionary/starter-dictionary")
            dictPaths.add("./../../../../../components/model-catalog/resource-dictionary/test-dictionary")
            resourceDictionaryLoadService.loadPathsResourceDictionary(dictPaths)

            testBaseConfigEnhancementAndValidation()
            testGoldenEnhancementAndValidation()
            testRemoteScriptsEnhancementAndValidation()
            testCapabilityCliEnhancementAndValidation()
        }
    }

    private fun testBaseConfigEnhancementAndValidation() {
        val basePath = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
        testComponentInvokeEnhancementAndValidation(basePath, "base-enhance")
    }

    private fun testGoldenEnhancementAndValidation() {
        val basePath = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/golden"
        testComponentInvokeEnhancementAndValidation(basePath, "golden-enhance")
    }

    private fun testRemoteScriptsEnhancementAndValidation() {
        val basePath = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/remote_scripts"
        testComponentInvokeEnhancementAndValidation(basePath, "remote_scripts-enhance")
    }

    private fun testCapabilityCliEnhancementAndValidation() {
        val basePath = "./../../../../../components/model-catalog/blueprint-model/test-blueprint/capability_cli"
        testComponentInvokeEnhancementAndValidation(basePath, "capability_cli-enhance")
    }

    private fun testComponentInvokeEnhancementAndValidation(basePath: String, targetDirName: String) {
        runBlocking {
            val targetPath = normalizedPathName("target/blueprints/enrichment", targetDirName)

            deleteDir(targetPath)

            val bluePrintContext = bluePrintEnhancerService.enhance(basePath, targetPath)
            Assert.assertNotNull("failed to get blueprintContext ", bluePrintContext)

            // Validate the Generated Blueprints
            val valid = bluePrintValidatorService.validateBlueprints(targetPath)
            Assert.assertTrue("blueprint($basePath) validation failed ", valid)

            // Enable this to get the enhanced zip file
            // val compressFile = normalizedFile("target/blueprints/enrichment", "$targetDirName.zip")
            // normalizedFile(targetPath).compress(compressFile)

            deleteDir(targetPath)
        }
    }
}
