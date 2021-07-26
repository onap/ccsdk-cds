/*
 * Copyright © 2019 IBM, Bell Canada.
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
package org.onap.ccsdk.cds.blueprintsprocessor.db.mock

import io.mockk.coEvery
import io.mockk.mockk
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintValidatorService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintRuntimeService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MockBlueprintProcessorCatalogServiceImpl {

    @Bean(name = ["bluePrintRuntimeValidatorService"])
    open fun bluePrintRuntimeValidatorService(): BluePrintValidatorService {
        val bluePrintValidatorService = mockk<BluePrintValidatorService>()
        coEvery { bluePrintValidatorService.validateBluePrints(any<String>()) } returns true
        coEvery { bluePrintValidatorService.validateBluePrints(any<BluePrintRuntimeService<*>>()) } returns true
        return bluePrintValidatorService
    }
}
