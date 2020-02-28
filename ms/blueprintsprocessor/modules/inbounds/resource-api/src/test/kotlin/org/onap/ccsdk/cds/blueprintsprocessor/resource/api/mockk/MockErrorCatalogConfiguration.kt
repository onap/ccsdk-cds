/*
 *  Copyright © 2020 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.resource.api.mockk

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.ErrorCatalogManagerImpl
import org.onap.ccsdk.error.catalog.data.ErrorCatalogProperties
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.onap.ccsdk.error.catalog.service.ErrorMessagesLibPropertyService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class MockErrorCatalogConfiguration {

    @Bean
    open fun errorCatalogService(): ErrorCatalogService {
        val errorCatalogProperties = ErrorCatalogProperties()
        errorCatalogProperties.type = "properties"
        errorCatalogProperties.applicationId = "CDS"
        return ErrorCatalogService(ErrorMessagesLibPropertyService(errorCatalogProperties))
    }

    @Bean
    open fun errorCatalogManagerImpl(): ErrorCatalogManagerImpl {
        return ErrorCatalogManagerImpl(errorCatalogService())
    }
}
