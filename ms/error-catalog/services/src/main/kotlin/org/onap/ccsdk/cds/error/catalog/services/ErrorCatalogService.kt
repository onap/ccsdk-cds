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

import kotlinx.coroutines.runBlocking
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalog
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogException
import org.onap.ccsdk.cds.error.catalog.core.ErrorMessageLibConstants
import org.onap.ccsdk.cds.error.catalog.core.ErrorPayload
import org.onap.ccsdk.cds.error.catalog.core.GrpcErrorCodes
import org.onap.ccsdk.cds.error.catalog.core.HttpErrorCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.ErrorCatalogUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
@ConditionalOnBean(ErrorCatalogLoadService::class)
open class ErrorCatalogService(private var errorCatalogLoadService: ErrorCatalogLoadService) {

    @PostConstruct
    open fun init() = runBlocking {
        errorCatalogLoadService.loadErrorCatalog()
    }

    fun errorPayload(errorCatalogException: ErrorCatalogException): ErrorPayload {
        val errorCatalog = getErrorCatalog(errorCatalogException)
        val errorPayload: ErrorPayload
        if (errorCatalogException.errorPayload == null) {
            errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, errorCatalog.getMessage())
        } else {
            errorPayload = errorCatalogException.errorPayload!!
            errorPayload.code = errorCatalog.code
            errorPayload.message = errorCatalog.getMessage()
            errorPayload.status = errorCatalog.errorId
        }
        if (errorCatalogException.cause != null) {
            errorPayload.debugMessage = ExceptionUtils.getStackTrace(errorCatalogException.cause)
        }
        return errorPayload
    }

    fun getErrorCatalog(errorCatalogException: ErrorCatalogException): ErrorCatalog {
        val errorMessage = getMessage(errorCatalogException.domain, errorCatalogException.name)
        val errorCode =
            if (errorCatalogException.code == -1) {
                getProtocolErrorCode(
                    errorCatalogException.protocol,
                    errorCatalogException.name
                )
            } else {
                errorCatalogException.code
            }
        val action: String
        val errorCause: String
        if (errorMessage.isNullOrEmpty()) {
            action = errorCatalogException.action
            errorCause = errorCatalogException.message ?: ""
        } else {
            action = ErrorCatalogUtils.readErrorActionFromMessage(errorMessage)
            errorCause = errorCatalogException.message ?: ErrorCatalogUtils.readErrorCauseFromMessage(errorMessage)
        }

        return ErrorCatalog(
            errorCatalogException.name,
            errorCatalogException.domain,
            errorCode,
            action,
            errorCause
        )
    }

    private fun getProtocolErrorCode(protocol: String, type: String): Int {
        return when (protocol) {
            ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC -> GrpcErrorCodes.code(type)
            else -> HttpErrorCodes.code(type)
        }
    }

    private fun getMessage(domain: String, key: String): String? {
        return errorCatalogLoadService.getErrorMessage(domain, key)
    }
}
