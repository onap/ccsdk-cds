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

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.ErrorMessageLibConstants
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.utils.ErrorCatalogUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.PropertySource

@PropertySource("classpath:application.properties")
open class ErrorCatalogService {
    @Value("\${blueprintprocessor.error.catalog.type}")
    private val catalogType: String? = null

    private var errorMessagesLibService: ErrorMessagesLibService

    init {
        errorMessagesLibService = if (catalogType == ErrorMessageLibConstants.ERROR_MESSAGE_LIB_TYPE_DB)
            ErrorMessagesLibDBService()
        else
            ErrorMessagesLibPropertyService()
    }

    /**
     * Exposed Dependency Service by this Error Message Lib Module
     */
    fun getErrorMessagesLibService(): ErrorMessagesLibService {
        return errorMessagesLibService
    }

    fun getErrorMessage(domain: String, key: String): String? {
        return errorMessagesLibService.getErrorMessage(domain, key)
    }

    fun getErrorMessage(attribute: String): String? {
        return errorMessagesLibService.getErrorMessage(attribute)
    }

    fun getErrorModel(errorId: String, domain: String): ErrorModel? {
        val errorMessage = getErrorMessage(domain, errorId) ?: return null
        return ErrorModel(errorId, domain, ErrorCatalogUtils.readErrorCauseFromMessage(errorMessage), ErrorCatalogUtils.readErrorActionFromMessage(errorMessage))
    }
}
