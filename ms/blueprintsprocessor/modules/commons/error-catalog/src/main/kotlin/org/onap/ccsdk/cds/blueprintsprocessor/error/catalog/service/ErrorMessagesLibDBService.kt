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

package org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.service

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.ErrorCatalogDataBaseProperties
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.ErrorCatalogProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.PropertySource
import org.springframework.context.annotation.PropertySources

/**
 * Representation of Blueprint Error Message lib from database service to load the properties
 */
@PropertySources(PropertySource("classpath:application.properties"))
@ConditionalOnProperty(
        name = ["blueprintprocessor.error.catalog.type"],
        havingValue = "DB"
)
open class ErrorMessagesLibDBService :
        ErrorMessagesLibService {
    @Value("\${blueprintprocessor.error.catalog.type}")
    private val catalogType: String? = null

    @Value("\${blueprintprocessor.error.catalog.application.id}")
    private val applicationId: String? = null

    @Value("\${blueprintprocessor.error.catalog.db.type}")
    private val dbType: String? = null

    @Value("\${blueprintprocessor.error.catalog.db.url}")
    private val url: String? = null

    @Value("\${blueprintprocessor.error.catalog.db.username}")
    private val username: String? = null

    @Value("\${blueprintprocessor.error.catalog.db.password}")
    private val password: String? = null

    /**
     * Static variable for logging.
     */
    companion object {
        var log = LoggerFactory.getLogger(
            ErrorMessagesLibDBService::class.java
        )!!
        const val database = "database name here"
    }

    private val errorMessageInDB: Map<String, String>
    private val errorCatalogProperties: ErrorCatalogProperties

    init {
        errorCatalogProperties = ErrorCatalogDataBaseProperties(catalogType!!, applicationId!!, dbType!!, url!!,
                username!!, password!!)
        errorMessageInDB = getErrorMessagesFromDB(errorCatalogProperties)
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private fun getErrorMessagesFromDB(errorCatalogProperties: ErrorCatalogProperties): Map<String, String> {
        // TODO: get error messages from database and load to errorMessageInDB
        return mapOf()
    }

    override fun getErrorMessage(domain: String, key: String): String? {
        return findErrorMessage(domain.plus(key.toLowerCase()))
    }

    override fun getErrorMessage(attribute: String): String? {
        return findErrorMessage(attribute)
    }

    private fun findErrorMessage(attribute: String): String? {
        return if (errorMessageInDB.containsKey(attribute)) errorMessageInDB[attribute]
        else null
    }
}
