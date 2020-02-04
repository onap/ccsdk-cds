/*
 *  Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.service

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.ErrorCatalogueProperties
import org.slf4j.LoggerFactory

/**
 * Representation of Blueprint Error Message lib from database service to load the properties
 */
open class ErrorMessagesLibDBService(private val errorCatalogueProperties: ErrorCatalogueProperties) :
        ErrorMessagesLibService() {

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

    init {
        errorMessageInDB = getErrorMessagesFromDB()
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private fun getErrorMessagesFromDB(): Map<String, String> {
        //TODO: get error messages from database and load to errorMessageInDB
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
