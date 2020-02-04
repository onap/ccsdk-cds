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
import org.onap.ccsdk.error.catalog.data.LogLevel

abstract class ErrorCatalogException : RuntimeException {
    var errorPayload: ErrorPayload
    private val messageSeparator = "${System.lineSeparator()} -> "

    fun addErrorModel(errorMessage: ErrorMessage) {
        errorPayload.subErrors.add(errorMessage)
    }

    fun addErrorPayloadMessage(message: String) {
        errorPayload.message = "${errorPayload.message} $messageSeparator $message"
    }

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String) : super(message) {

        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, logLevel)
        addErrorModel(ErrorMessage(errorCatalog.domainId, message, null))
    }

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String, vararg args: Any?):
            super(String.format(message, *args)) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, logLevel)
        addErrorModel(ErrorMessage(errorCatalog.domainId, String.format(message, *args), null))
    }

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String, cause: Throwable):
            super(message, cause) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, logLevel, cause.message
                ?: "")
        addErrorModel(ErrorMessage(errorCatalog.domainId, message, cause))
    }

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, cause: Throwable, message: String,
                vararg args: Any?): super(String.format(message, *args), cause) {
        this.errorPayload = ErrorPayload(errorCatalog.code, errorCatalog.errorId, logLevel, cause.message
                ?: "")
        addErrorModel(ErrorMessage(errorCatalog.domainId, String.format(message, *args), cause))
    }

    constructor(errorPayload: ErrorPayload, domainId: String, message: String) : super(message) {
        this.errorPayload = errorPayload
        addErrorModel(ErrorMessage(domainId, message, null))
    }

    constructor(errorPayload: ErrorPayload, domainId: String, message: String, cause: Throwable):
            super(message, cause) {
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
}
