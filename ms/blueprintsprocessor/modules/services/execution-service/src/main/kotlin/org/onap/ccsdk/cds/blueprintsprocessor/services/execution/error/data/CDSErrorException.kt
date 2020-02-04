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
import org.onap.ccsdk.cds.controllerblueprints.core.format

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

open class CDSErrorException : ErrorCatalogException {
    constructor(message: String, cause: Throwable) : super(message, cause) {
        this.errorMessage = CDSErrorImpl()
        this.errorMessage.message = message
        this.errorMessage.debugMessage = cause.toString()
    }
    constructor(message: String) : super(message) {
        this.errorMessage = CDSErrorImpl()
        this.errorMessage.message = message
    }
    constructor(cause: Throwable) : super(cause) {
        this.errorMessage = CDSErrorImpl()
        this.errorMessage.debugMessage = cause.toString()
    }
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause) {
        this.errorMessage = CDSErrorImpl()
        this.errorMessage.message = message
        this.errorMessage.debugMessage = cause.toString()
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
