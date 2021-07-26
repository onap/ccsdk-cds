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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.validation

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api.mock.MockResourceSource
import org.onap.ccsdk.cds.controllerblueprints.validation.BluePrintValidationConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        BluePrintRuntimeValidatorService::class,
        BluePrintValidationConfiguration::class, MockResourceSource::class
    ]
)
class BluePrintRuntimeValidatorServiceTest {

    @Autowired
    lateinit var bluePrintRuntimeValidatorService: BluePrintRuntimeValidatorService

    @Test
    fun testBlueprintRuntimeValidation() {
        runBlocking {
            val blueprintBasePath =
                "./../../../../../components/model-catalog/blueprint-model/test-blueprint/baseconfiguration"
            assertNotNull(bluePrintRuntimeValidatorService, " failed to initilize bluePrintRuntimeValidatorService")

            bluePrintRuntimeValidatorService.validateBluePrints(blueprintBasePath)
        }
    }
}
