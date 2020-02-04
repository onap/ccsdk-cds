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

import org.onap.ccsdk.error.catalog.data.ErrorModel
import java.time.LocalDateTime

abstract class ErrorCatalogException : RuntimeException {
    var errorMessage: ErrorInterface
    private val messageSeparator = "\n\t -> "

    private fun addErrorModel(errorModel: ErrorModel) {
        errorMessage.subErrors.add(errorModel)
    }

    fun updateErrorCatalogException(errorModel: ErrorModel, exception: Exception? = null, code: Int? = null,
                                 message: String? = null) {

        addErrorModel(errorModel)

        if (exception?.message != null) {
            if (errorMessage.debugMessage != "") {
                errorMessage.debugMessage = messageSeparator.plus(exception.message!!)
            }
            else {
                errorMessage.debugMessage = exception.message!!
            }
        }

        if (code != null) {
            errorMessage.code = code
        }

        if (message != null) {
            if (errorMessage.message != "") {
                errorMessage.message = messageSeparator.plus(message)
            }
            else {
                errorMessage.message = message
            }
        }

        this.errorMessage.timestamp = LocalDateTime.now()
    }

    constructor(errorMessage: ErrorInterface, cause: Throwable) : super(cause) {
        this.errorMessage = errorMessage
        this.errorMessage.debugMessage = cause.toString()
    }
    constructor(errorMessage: ErrorInterface, message: String) : super(message) {
        this.errorMessage = errorMessage
        this.errorMessage.message = message
    }

    constructor(errorMessage: ErrorInterface, message: String, cause: Throwable) : super(message, cause) {
        this.errorMessage = errorMessage
        this.errorMessage.message = message
        this.errorMessage.debugMessage = cause.toString()
    }

    constructor(errorMessage: ErrorInterface, cause: Throwable, message: String, vararg args: Any?) :
            super(String.format(message, *args), cause) {
        this.errorMessage = errorMessage
        this.errorMessage.message = message
        this.errorMessage.debugMessage = cause.toString()
    }
}
