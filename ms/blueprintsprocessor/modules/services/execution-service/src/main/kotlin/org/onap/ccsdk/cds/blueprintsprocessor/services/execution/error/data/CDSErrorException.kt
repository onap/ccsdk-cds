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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.data

import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.onap.ccsdk.error.catalog.interfaces.ErrorInterface

open class CDSErrorException : ErrorCatalogException {
    constructor(errorMessage: ErrorInterface, cause: Throwable) : super(errorMessage, cause) {
        this.errorMessage = errorMessage
        this.errorMessage.debugMessage = cause.toString()
    }
    constructor(errorMessage: ErrorInterface, message: String) : super(errorMessage, message) {
        this.errorMessage = errorMessage
        this.errorMessage.message = message
    }

    constructor(errorMessage: ErrorInterface, message: String, cause: Throwable) : super(errorMessage, message, cause) {
        this.errorMessage = errorMessage
        this.errorMessage.message = message
        this.errorMessage.debugMessage = cause.toString()
    }

    constructor(errorMessage: ErrorInterface, cause: Throwable, message: String, vararg args: Any?) :
            super(errorMessage, String.format(message, *args), cause) {
        this.errorMessage = errorMessage
        this.errorMessage.message = message
        this.errorMessage.debugMessage = cause.toString()
    }
}

open class CDSErrorImpl : ErrorInterface {
    constructor()

    constructor(code: Int) {
        this.code = code
    }
    constructor(status: String) {
        this.status = status
    }
    constructor(code: Int, status: String) {
        this.code = code
        this.status = status
    }
    constructor(code: Int, status: String, debugMessage: String) {
        this.code = code
        this.status = status
        this.debugMessage = debugMessage
    }
}
