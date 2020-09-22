/*
 *  Copyright © 2020 IBM, Bell Canada.
 *  Modifications Copyright © 2019-2020 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.error.catalog.services

import org.onap.ccsdk.cds.error.catalog.core.ErrorMessageLibConstants
import org.onap.ccsdk.cds.error.catalog.core.logger
import org.onap.ccsdk.cds.error.catalog.services.domain.ErrorMessageModel
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths
import java.util.Properties

interface ErrorCatalogLoadService {

    suspend fun loadErrorCatalog()

    fun getErrorMessage(domain: String, key: String): String?

    fun getErrorMessage(attribute: String): String?
}

/**
 * Representation of Blueprint Error Message lib from database service to load the properties
 */
@Service
@ConditionalOnBean(ErrorCatalogDBService::class)
open class ErrorCatalogLoadDBService(
    private var errorCatalogProperties: ErrorCatalogProperties,
    private var errorCatalogDBService: ErrorCatalogDBService
) : ErrorCatalogLoadService {

    private var log = logger(ErrorCatalogLoadDBService::class)

    lateinit var errorMessageInDB: Map<String, ErrorMessageModel>

    override suspend fun loadErrorCatalog() {
        log.info("Application ID: ${errorCatalogProperties.applicationId} > Initializing error catalog message from database...")
        errorMessageInDB = getErrorMessagesFromDB()
    }

    override fun getErrorMessage(domain: String, key: String): String? {
        return getErrorMessage("$domain.${key.toLowerCase()}")
    }

    override fun getErrorMessage(attribute: String): String? {
        val errorMessage = findErrorMessage(attribute)
        return prepareErrorMessage(errorMessage)
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private suspend fun getErrorMessagesFromDB(): Map<String, ErrorMessageModel> {
        return errorCatalogDBService.getAllErrorMessagesByApplication(errorCatalogProperties.applicationId)
    }

    private fun findErrorMessage(attribute: String): ErrorMessageModel? {
        return if (errorMessageInDB.containsKey(attribute)) {
            errorMessageInDB[attribute]
        } else {
            null
        }
    }

    private fun prepareErrorMessage(errorMessage: ErrorMessageModel?): String? {
        return if (errorMessage != null) {
            "cause=${errorMessage.cause}, action=${errorMessage.action}"
        } else {
            null
        }
    }
}

/**
 * Representation of Blueprint Error Message lib property service to load the properties
 */
@Service
@ConditionalOnProperty(
    name = [ErrorMessageLibConstants.ERROR_CATALOG_TYPE],
    havingValue = ErrorMessageLibConstants.ERROR_CATALOG_TYPE_PROPERTIES
)
open class ErrorCatalogLoadPropertyService(private var errorCatalogProperties: ErrorCatalogProperties) :
    ErrorCatalogLoadService {

    private val propertyFileName = ErrorMessageLibConstants.ERROR_CATALOG_PROPERTIES_FILENAME
    private lateinit var propertyFile: File

    private var log = logger(ErrorCatalogLoadPropertyService::class)

    lateinit var properties: Properties

    override suspend fun loadErrorCatalog() {
        log.info("Application ID: ${errorCatalogProperties.applicationId} > Initializing error catalog message from properties...")
        val propertyDir = errorCatalogProperties.errorDefinitionDir ?: ErrorMessageLibConstants.ERROR_CATALOG_PROPERTIES_DIRECTORY
        propertyFile = Paths.get(propertyDir, propertyFileName).toFile().normalize()
        properties = parseErrorMessagesProps()
    }

    override fun getErrorMessage(domain: String, key: String): String? {
        return getErrorMessage("$domain.${key.toLowerCase()}")
    }

    override fun getErrorMessage(attribute: String): String? {
        return properties.getProperty(attribute)
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private fun parseErrorMessagesProps(): Properties {
        var inputStream: InputStream? = null
        val props = Properties()
        try {
            inputStream = propertyFile.inputStream()
            props.load(inputStream)
        } catch (e: FileNotFoundException) {
            log.error(
                "Application ID: ${errorCatalogProperties.applicationId} > Property File '$propertyFileName' " +
                    "not found in the application directory."
            )
        } catch (e: IOException) {
            log.error(
                "Application ID: ${errorCatalogProperties.applicationId} > Fail to load property file " +
                    "'$propertyFileName' for message errors."
            )
        } finally {
            inputStream?.close()
        }
        return props
    }
}
