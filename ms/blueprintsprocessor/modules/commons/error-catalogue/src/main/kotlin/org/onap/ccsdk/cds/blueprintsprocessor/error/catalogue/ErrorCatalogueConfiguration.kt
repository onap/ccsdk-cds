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

package org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.service.ErrorMessagesLibService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

/**
 * Exposed Dependency Service Error Message properties
 */
@Configuration
@ComponentScan
@PropertySource("classpath:application.properties")
open class BlueprintErrorMessageLibConfiguration {
    @Value("\${blueprintprocessor.error.catalogue.type}")
    private val catalogueType: String? = null

    @Value("\${blueprintprocessor.error.catalogue.application.id}")
    private val applicationId: String? = null

    @Value("\${blueprintprocessor.error.catalogue.db.type}")
    private val dbType: String? = null

    @Value("\${blueprintprocessor.error.catalogue.db.url}")
    private val url: String? = null

    @Value("\${blueprintprocessor.error.catalogue.db.username}")
    private val username: String? = null

    @Value("\${blueprintprocessor.error.catalogue.db.password}")
    private val password: String? = null

    @Bean
    open fun getErrorMessagesLibService(): ErrorCatalogueProperties {

        val errorCatalogueProperties: ErrorCatalogueProperties

        if (catalogueType==ErrorMessageLibConstants.ERROR_MESSAGE_LIB_PROPERTIES) {
            errorCatalogueProperties = ErrorCatalogueProperties()
            errorCatalogueProperties.type = catalogueType!!
            errorCatalogueProperties.applicationId = applicationId!!
        }
        else {
            errorCatalogueProperties = ErrorCatalogueDataBaseProperties()
            errorCatalogueProperties.type = catalogueType!!
            errorCatalogueProperties.applicationId = applicationId!!
            errorCatalogueProperties.dbType = dbType!!
            errorCatalogueProperties.url = url!!
            errorCatalogueProperties.username = username!!
            errorCatalogueProperties.password = applicationId!!
        }

        return errorCatalogueProperties
    }
}

/**
 * Exposed Dependency Service by this Error Message Lib Module
 */
fun BluePrintDependencyService.errorMessagesLibService(catalogueType: String): ErrorMessagesLibService {
    val serviceName = if (catalogueType==ErrorMessageLibConstants.ERROR_MESSAGE_LIB_PROPERTIES)
        ErrorMessageLibConstants.SERVICE_BLUEPRINT_ERROR_MESSAGE_LIB_PROPERTIES
    else ErrorMessageLibConstants.SERVICE_BLUEPRINT_ERROR_MESSAGE_LIB_DB
    return instance(serviceName)
}

fun BluePrintDependencyService.getErrorMessageFromDB(domain: String, key: String): String? {
    return errorMessagesLibService(ErrorMessageLibConstants.ERROR_MESSAGE_LIB_DB).getErrorMessage(domain, key)
}

fun BluePrintDependencyService.getErrorMessageFromPopertiesFile(domain: String, key: String): String? {
    return errorMessagesLibService(ErrorMessageLibConstants.ERROR_MESSAGE_LIB_PROPERTIES).getErrorMessage(domain, key)
}

fun BluePrintDependencyService.getErrorMessageFromDB(attribute: String): String? {
    return errorMessagesLibService(ErrorMessageLibConstants.ERROR_MESSAGE_LIB_DB).getErrorMessage(attribute)
}


fun BluePrintDependencyService.getErrorMessageFromPopertiesFile(attribute: String): String? {
    return errorMessagesLibService(ErrorMessageLibConstants.ERROR_MESSAGE_LIB_PROPERTIES).getErrorMessage(attribute)
}

open class ErrorMessageLibConstants {
    companion object {
        const val SERVICE_BLUEPRINT_ERROR_MESSAGE_LIB_PROPERTIES = "blueprint-error-message-lib-property-service"
        const val SERVICE_BLUEPRINT_ERROR_MESSAGE_LIB_DB = "blueprint-error-message-lib-DB-service"
        const val SERVICE_ERROR_MESSAGE_LIB = "blueprint-error-message-lib-service"
        const val ERROR_MESSAGE_LIB_PROPERTIES = "properties"
        const val ERROR_MESSAGE_LIB_DB = "DB"
    }
}
