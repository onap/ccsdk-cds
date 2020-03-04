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

package org.onap.ccsdk.error.catalog.interfaces

import org.onap.ccsdk.error.catalog.ErrorPayload
import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.onap.ccsdk.error.catalog.data.ErrorMessage
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.data.LogLevel

abstract class ErrorCatalogException : RuntimeException {
    var errorPayload: ErrorPayload
    private val messageSeparator = "${System.lineSeparator()} -> "
    var httpCode: Int = 500
    var grpcCode: Int = 13

    fun addErrorModel(errorMessage: ErrorMessage) {
        errorPayload.subErrors.add(errorMessage)
    }

    fun addErrorPayloadMessage(message: String) {
        errorPayload.message = "${errorPayload.message} $messageSeparator $message"
    }

    constructor(errorCatalog: ErrorCatalog, message: String, logLevel: String = LogLevel.ERROR.name) :
            super(message) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, errorCatalog.getMessage(), logLevel)
        addErrorModel(ErrorMessage(errorCatalog.domainId, message, null))
    }

    constructor(errorCatalog: ErrorCatalog, message: String, logLevel: String = LogLevel.ERROR.name, vararg args: Any?):
            super(String.format(message, *args)) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, errorCatalog.getMessage(), logLevel)
        addErrorModel(ErrorMessage(errorCatalog.domainId, String.format(message, *args), null))
    }

    constructor(errorCatalog: ErrorCatalog, message: String, cause: Throwable, logLevel: String = LogLevel.ERROR.name):
            super(message, cause) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, errorCatalog.getMessage(), logLevel,
                cause.message ?: message)
        addErrorModel(ErrorMessage(errorCatalog.domainId, message, cause))
    }

    constructor(errorCatalog: ErrorCatalog, message: String, cause: Throwable, logLevel: String = LogLevel.ERROR.name, vararg args: Any?) :
            super(String.format(message, *args), cause) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, errorCatalog.getMessage(), logLevel,
                cause.message ?: message)
        addErrorModel(ErrorMessage(errorCatalog.domainId, String.format(message, *args), cause))
    }

    constructor(errorPayload: ErrorPayload, domainId: String, message: String) : super(message) {
        this.errorPayload = errorPayload
        addErrorModel(ErrorMessage(domainId, message, null))
    }

    constructor(errorPayload: ErrorPayload, domainId: String, message: String, cause: Throwable) : super(message, cause) {
        this.errorPayload = errorPayload
        this.errorPayload.debugMessage = cause.message ?: ""
        addErrorModel(ErrorMessage(domainId, message, cause))
    }

    constructor(errorPayload: ErrorPayload, domainId: String, cause: Throwable, message: String,
                vararg args: Any?): super(String.format(message, *args), cause) {
        this.errorPayload = errorPayload
        this.errorPayload.debugMessage = cause.message ?: ""
        addErrorModel(ErrorMessage(domainId, String.format(message, *args), cause))
    }

    fun updateErrorPayloadCode(protocol: String) {
        if (protocol == ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC) {
            this.errorPayload.code = this.grpcCode
        } else {
            this.errorPayload.code = this.httpCode
        }
    }

    fun setProtocolsCode(enumErrorCatalog: EnumErrorCatalogInterface) {
        this.httpCode = enumErrorCatalog.getErrorHttpCode()
        this.grpcCode = enumErrorCatalog.getErrorGrpcCode()
    }

    fun isEqualTo(errorCatalogException: ErrorCatalogException): Boolean {
        return (this.httpCode==errorCatalogException.httpCode && this.grpcCode==errorCatalogException.grpcCode &&
                this.errorPayload.isEqualTo(errorCatalogException.errorPayload))
    }
}
