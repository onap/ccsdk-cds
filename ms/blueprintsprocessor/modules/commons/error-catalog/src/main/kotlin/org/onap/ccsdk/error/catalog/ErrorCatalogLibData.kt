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

package org.onap.ccsdk.error.catalog

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "error.catalog")
open class ErrorCatalogProperties {
    lateinit var type: String
    lateinit var applicationId: String
}

@ConfigurationProperties(prefix = "error.catalog.db")
open class ErrorCatalogDatabaseProperties {
    lateinit var url: String
    lateinit var username: String
    lateinit var password: String
    lateinit var driverClassName: String
    lateinit var hibernateHbm2ddlAuto: String
    lateinit var hibernateDialect: String
}

open class ErrorMessageLibConstants {
    companion object {
        const val ERROR_CATALOG_DOMAIN = "org.onap.ccsdk.error.catalog"
        const val ERROR_CATALOG_TYPE = "error.catalog.type"
        const val ERROR_CATALOG_TYPE_PROPERTIES = "properties"
        const val ERROR_CATALOG_TYPE_DB = "DB"
        const val ERROR_CATALOG_PROPERTIES_FILENAME = "error-messages_en.properties"
        const val ERROR_CATALOG_APPLICATION_ID = "error.catalog.application.id"
        const val ERROR_CATALOG_DATABASE_USERNAME = "error.catalog.db.username"
        const val ERROR_CATALOG_DATABASE_PASSWORD = "error.catalog.db.password"
        const val ERROR_CATALOG_DATABASE_URL = "error.catalog.db.url"
        const val ERROR_CATALOG_MODELS = "org.onap.ccsdk.error.catalog.domain"
        const val ERROR_CATALOG_REPOSITORY = "org.onap.ccsdk.error.catalog.repository"
    }
}
