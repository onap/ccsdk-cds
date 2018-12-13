/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.apps.controllerblueprints.core.validation

import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.mock.MockBluePrintTypeValidatorService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import kotlin.test.assertTrue

class BluePrintValidatorServiceImplTest {

    val blueprintBasePath: String = ("./../model-catalog/blueprint-model/starter-blueprint/baseconfiguration")


    @Test
    fun testValidateOfType() {
        val bluePrintRuntime = BluePrintMetadataUtils.getBluePrintRuntime("1234", blueprintBasePath)

        val mockBluePrintTypeValidatorService = MockBluePrintTypeValidatorService()

        val defaultBluePrintValidatorService = BluePrintValidatorServiceImpl(mockBluePrintTypeValidatorService)

        val valid = defaultBluePrintValidatorService.validateBluePrints(bluePrintRuntime)

        assertTrue(valid, "failed in blueprint Validation")

    }
}

