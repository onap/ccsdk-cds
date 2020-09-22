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

package org.onap.ccsdk.cds.error.catalog.core

interface ErrorCatalogExceptionFluent<T> {

    fun code(code: Int): T
    fun domain(domain: String): T
    fun action(action: String): T
    fun http(type: String): T
    fun grpc(type: String): T
    fun convertToHttp(): T
    fun convertToGrpc(): T
    fun payloadMessage(message: String): T
    fun addErrorPayloadMessage(message: String): T
    fun addSubError(errorMessage: ErrorMessage): T
}

open class ErrorCatalogException : RuntimeException {

    var code: Int = -1
    var domain: String = ""
    var name: String = ErrorCatalogCodes.GENERIC_FAILURE
    var action: String = ""
    var errorPayload: ErrorPayload? = null
    var protocol: String = ""
    var errorPayloadMessages: MutableList<String>? = null

    val messageSeparator = "${System.lineSeparator()} -> "

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause)

    constructor(code: Int, cause: Throwable) : super(cause) {
        this.code = code
    }

    constructor(code: Int, message: String) : super(message) {
        this.code = code
    }

    constructor(code: Int, message: String, cause: Throwable) : super(message, cause) {
        this.code = code
    }

    constructor(code: Int, cause: Throwable, message: String, vararg args: Any?) :
        super(String.format(message, *args), cause) {
            this.code = code
        }

    open fun <T : ErrorCatalogException> updateCode(code: Int): T {
        this.code = code
        return this as T
    }

    open fun <T : ErrorCatalogException> updateDomain(domain: String): T {
        this.domain = domain
        return this as T
    }

    open fun <T : ErrorCatalogException> updateAction(action: String): T {
        this.action = action
        return this as T
    }

    fun <T : ErrorCatalogException> updateHttp(type: String): T {
        this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP
        this.name = type
        this.code = HttpErrorCodes.code(type)
        return this as T
    }

    fun <T : ErrorCatalogException> inverseToHttp(): T {
        if (this.protocol != "" && this.protocol == ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC) {
            this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP
            this.code = HttpErrorCodes.code(this.name)
        }
        return this as T
    }

    fun <T : ErrorCatalogException> inverseToGrpc(): T {
        if (this.protocol != "" && this.protocol == ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP) {
            this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC
            this.code = GrpcErrorCodes.code(this.name)
        }
        return this as T
    }

    fun <T : ErrorCatalogException> updateGrpc(type: String): T {
        this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC
        this.name = type
        this.code = GrpcErrorCodes.code(type)
        return this as T
    }

    fun <T : ErrorCatalogException> updatePayloadMessage(message: String): T {
        if (this.errorPayloadMessages == null) this.errorPayloadMessages = arrayListOf()
        this.errorPayloadMessages!!.add(message)
        return this as T
    }

    fun <T : ErrorCatalogException> updateErrorPayloadMessage(message: String): T {
        if (errorPayload == null) {
            errorPayload = ErrorPayload()
        }
        errorPayload!!.message = "${errorPayload!!.message} $messageSeparator $message"
        return this as T
    }

    fun <T : ErrorCatalogException> updateSubError(errorMessage: ErrorMessage): T {
        if (errorPayload == null) {
            errorPayload = ErrorPayload()
        }
        errorPayload!!.subErrors.add(errorMessage)
        return this as T
    }
}
