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

package org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.apps.blueprintsprocessor.core.api.data.ExecutionServiceInput
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.mock.MockBluePrintCatalogService
import org.onap.ccsdk.apps.blueprintsprocessor.selfservice.api.mock.MockBlueprintDGExecutionService
import org.onap.ccsdk.apps.controllerblueprints.core.utils.BluePrintMetadataUtils
import org.onap.ccsdk.apps.controllerblueprints.core.utils.JacksonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [MockBluePrintCatalogService::class,
    MockBlueprintDGExecutionService::class, ExecutionServiceHandler::class])
class ExecutionServiceHandlerTest {

    @Autowired
    lateinit var executionServiceHandler: ExecutionServiceHandler

    @Test
    fun testProcess() {
        val bluePrintRuntimeService = BluePrintMetadataUtils.getBluePrintRuntime("1234",
                "./../../../../../components/model-catalog/blueprint-model/starter-blueprint/baseconfiguration")

        val executionServiceInput = JacksonUtils.readValueFromClassPathFile("execution-input/default-input.json", ExecutionServiceInput::class.java)!!
        executionServiceHandler.process(executionServiceInput)

    }

}


