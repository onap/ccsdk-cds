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

import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionComponent
import org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.ResourceResolutionService
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.ErrorCatalogManagerImpl
import org.onap.ccsdk.error.catalog.data.ErrorCatalogProperties
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.onap.ccsdk.error.catalog.service.ErrorMessagesLibPropertyService
import org.springframework.test.context.ContextConfiguration

@ContextConfiguration(classes = [MockErrorCatalogConfiguration::class])
class MockResourceResolutionComponent(resourceResolutionService: ResourceResolutionService) : ResourceResolutionComponent(resourceResolutionService) {
    init {
        val errorCatalogProperties = ErrorCatalogProperties()
        errorCatalogProperties.type = "properties"
        errorCatalogProperties.applicationId = "CDS"
        errorManager = ErrorCatalogManagerImpl(ErrorCatalogService(ErrorMessagesLibPropertyService(errorCatalogProperties)))
    }
}
