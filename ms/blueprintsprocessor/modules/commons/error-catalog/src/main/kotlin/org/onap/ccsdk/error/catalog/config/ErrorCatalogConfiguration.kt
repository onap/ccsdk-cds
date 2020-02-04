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

package org.onap.ccsdk.error.catalog.config

import org.onap.ccsdk.error.catalog.ErrorCatalogProperties
import org.onap.ccsdk.error.catalog.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.service.ErrorCatalogDatabaseHandler
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.onap.ccsdk.error.catalog.service.ErrorMessagesLibDBService
import org.onap.ccsdk.error.catalog.service.ErrorMessagesLibPropertyService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Bean

@Configuration
@EnableConfigurationProperties(ErrorCatalogProperties::class)
open class ErrorCatalogConfiguration {
    @Autowired
    lateinit var errorCatalogProperties: ErrorCatalogProperties

    @Bean
    @ConditionalOnMissingBean
    open fun errorCatalogService(): ErrorCatalogService {
        return if (errorCatalogProperties.type == ErrorMessageLibConstants.ERROR_CATALOG_TYPE_DB) {
            val errorCatalogDatabaseHandler = errorCatalogDatabaseHandler()
            ErrorCatalogService(ErrorMessagesLibDBService(errorCatalogProperties, errorCatalogDatabaseHandler))
        } else {
            ErrorCatalogService(ErrorMessagesLibPropertyService(errorCatalogProperties))
        }
    }

    @Bean("errorCatalogDatabaseHandler")
    @ConditionalOnProperty(name = [ErrorMessageLibConstants.ERROR_CATALOG_TYPE],
            havingValue = ErrorMessageLibConstants.ERROR_CATALOG_TYPE_DB)
    open fun errorCatalogDatabaseHandler(): ErrorCatalogDatabaseHandler {
        return ErrorCatalogDatabaseHandler()
    }
}
