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

import org.onap.ccsdk.error.catalog.data.ErrorCatalogProperties
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.Properties

/**
 * Representation of Blueprint Error Message lib property service to load the properties
 */
open class ErrorMessagesLibPropertyService(private var errorCatalogProperties: ErrorCatalogProperties) :
        ErrorMessagesLibService {
    private val errorCatalogFilename = ErrorMessageLibConstants.ERROR_CATALOG_PROPERTIES_FILENAME
    /**
     * Static variable for logging.
     */
    companion object {
        var log = LoggerFactory.getLogger(
            ErrorMessagesLibPropertyService::class.java
        )!!
    }

    private var properties: Properties

    init {
        log.info("Application ID: ${errorCatalogProperties.applicationId} > Initializing error catalog message from properties...")
        properties = parseErrorMessagesProps()
    }

    /**
     * Parses the error-messages.properties file which contains error messages
     */
    private fun parseErrorMessagesProps(): Properties {
        var inputStream: InputStream? = null
        val props = Properties()
        try {
            inputStream = javaClass.classLoader.getResourceAsStream(errorCatalogFilename)
            if (inputStream != null) {
                props.load(inputStream)
            } else {
                log.error("Application ID: ${errorCatalogProperties.applicationId} > Property file '$errorCatalogFilename' not found in the classpath.")
            }
        } catch (e: Exception) {
            log.error("Application ID: ${errorCatalogProperties.applicationId} > Fail to load property file '$errorCatalogFilename}' for message errors.")
        } finally {
            inputStream!!.close()
        }
        return props
    }

    override fun getErrorMessage(domain: String, key: String): String? {
        return properties.getProperty("$domain.${key.toLowerCase()}")
    }

    override fun getErrorMessage(attribute: String): String? {
        return properties.getProperty(attribute)
    }
}
