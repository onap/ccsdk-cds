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

package org.onap.ccsdk.error.catalog.service

import org.onap.ccsdk.error.catalog.ErrorCatalogProperties
import org.onap.ccsdk.error.catalog.domain.ErrorMessage
import org.slf4j.LoggerFactory

/**
 * Representation of Blueprint Error Message lib from database service to load the properties
 */
open class ErrorMessagesLibDBService(private var errorCatalogProperties: ErrorCatalogProperties,
                                     private var errorCatalogDatabaseHandler: ErrorCatalogDatabaseHandler) :
        ErrorMessagesLibService {

    /**
     * Static variable for logging.
     */
    companion object {
        var log = LoggerFactory.getLogger(
            ErrorMessagesLibDBService::class.java
        )!!
    }

    private val errorMessageInDB: Map<String, ErrorMessage>

    init {
        errorMessageInDB = getErrorMessagesFromDB()
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private fun getErrorMessagesFromDB(): Map<String, ErrorMessage> {
        return errorCatalogDatabaseHandler.getAllErrorMessagesByApplication(errorCatalogProperties.applicationId)
    }

    override fun getErrorMessage(domain: String, key: String): String? {
        val errorMessage = findErrorMessage(domain.plus(key.toLowerCase()))
        return prepareErrorMessage(errorMessage)
    }

    override fun getErrorMessage(attribute: String): String? {
        val errorMessage = findErrorMessage(attribute)
        return prepareErrorMessage(errorMessage)
    }

    private fun findErrorMessage(attribute: String): ErrorMessage? {
        return if (errorMessageInDB.containsKey(attribute)) {
            errorMessageInDB[attribute]
        }
        else {
            null
        }
    }

    private fun prepareErrorMessage(errorMessage: ErrorMessage?): String? {
        return if (errorMessage != null) {
            "cause=".plus(errorMessage.cause) + "," + "action=".plus(errorMessage.action)
        }
        else {
            null
        }
    }
}
