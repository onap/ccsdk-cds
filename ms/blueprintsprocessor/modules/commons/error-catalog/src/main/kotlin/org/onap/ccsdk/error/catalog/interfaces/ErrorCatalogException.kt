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

import java.lang.String.format

abstract class ErrorCatalogException : RuntimeException {
    lateinit var errorMessage: ErrorInterface

    fun addErrorModel(errorCatalog: EnumErrorCatalogInterface) {
        errorMessage.addError(errorCatalog)
    }

    fun getErrorCatalogException(exception: Exception? = null, errorModel: ErrorInterface,
                                 message: String, cause: Throwable) {

    }

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause)

    constructor(errorMessage: ErrorInterface, cause: Throwable) : super(cause) {
        this.errorMessage = errorMessage
    }
    constructor(errorMessage: ErrorInterface, message: String) : super(message) {
        this.errorMessage = errorMessage
    }

    constructor(errorMessage: ErrorInterface, message: String, cause: Throwable) : super(message, cause) {
        this.errorMessage = errorMessage
    }

    constructor(errorMessage: ErrorInterface, cause: Throwable, message: String, vararg args: Any?) :
            super(String.format(message, *args), cause) {
        this.errorMessage = errorMessage
    }
}
