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
package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock

import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintPropertiesService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService

class MockBluePrintRestLibPropertyService(bluePrintProperties: BluePrintPropertiesService) :
    BluePrintRestLibPropertyService(bluePrintProperties) {

    private var services = mutableSetOf<MockBlueprintWebClientService>()

    fun mockBlueprintWebClientService(selector: String):
        MockBlueprintWebClientService {
            val prefix = "blueprintsprocessor.restclient.$selector"
            val restClientProperties = restClientProperties(prefix)
            return mockBlueprintWebClientService(restClientProperties)
        }

    private fun mockBlueprintWebClientService(restClientProperties: RestClientProperties):
        MockBlueprintWebClientService {
            val service = MockBlueprintWebClientService(restClientProperties)
            services.add(service)
            return service
        }

    fun tearDown() {
        services.forEach { it.tearDown() }
        services.clear()
    }
}
