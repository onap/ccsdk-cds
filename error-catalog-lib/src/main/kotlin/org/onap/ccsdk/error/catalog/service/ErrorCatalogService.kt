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

import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.utils.ErrorCatalogUtils
import org.springframework.stereotype.Service

@Service
class ErrorCatalogService(private var errorMessagesLibService: ErrorMessagesLibService) {

    private fun getMessage(domain: String, key: String): String? {
        return errorMessagesLibService.getErrorMessage(domain, key)
    }

    fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorCode: Int?, errorMessage: String,
        errorCause: Throwable
    ): ErrorCatalog {
        val message = getMessage(enumErrorCatalog.getErrorDomain(), enumErrorCatalog.getErrorName()) ?: errorMessage
        val code = errorCode ?: getProtocolErrorCode(enumErrorCatalog, protocol)
        val action = ErrorCatalogUtils.readErrorActionFromMessage(message)
        val cause = errorCause.message ?: ErrorCatalogUtils.readErrorCauseFromMessage(message) ?: message

        return ErrorCatalog(enumErrorCatalog.getErrorName(), enumErrorCatalog.getErrorDomain(), code, action, cause)
    }

    private fun getProtocolErrorCode(enumErrorCatalog: EnumErrorCatalogInterface, protocol: String): Int {
        return if (protocol == ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC) {
            enumErrorCatalog.getErrorGrpcCode()
        } else {
            enumErrorCatalog.getErrorHttpCode()
        }
    }
}
