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

import org.onap.ccsdk.error.catalog.data.ErrorModel
import org.onap.ccsdk.error.catalog.utils.ErrorCatalogUtils
import org.springframework.stereotype.Service

@Service
open class ErrorCatalogService(private var errorMessagesLibService: ErrorMessagesLibService) {

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
