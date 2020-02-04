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
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorCode: Int?, message: String,
        cause: Throwable?
    ): ErrorCatalog {
        val errorMessage = getMessage(enumErrorCatalog.getErrorDomain(), enumErrorCatalog.getErrorName()) ?: message
        val errorCode = errorCode ?: getProtocolErrorCode(enumErrorCatalog, protocol)
        val action = ErrorCatalogUtils.readErrorActionFromMessage(errorMessage)
        val errorCause = cause!!.message ?: ErrorCatalogUtils.readErrorCauseFromMessage(errorMessage)

        return ErrorCatalog(enumErrorCatalog.getErrorName(), enumErrorCatalog.getErrorDomain(), errorCode, action, errorCause)
    }

    private fun getProtocolErrorCode(enumErrorCatalog: EnumErrorCatalogInterface, protocol: String): Int {
        return if (protocol == ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC) {
            enumErrorCatalog.getErrorGrpcCode()
        } else {
            enumErrorCatalog.getErrorHttpCode()
        }
    }
}
