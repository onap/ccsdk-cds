/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.controllerblueprints.core

import org.onap.ccsdk.error.catalog.core.ErrorCatalogException
import org.onap.ccsdk.error.catalog.core.ErrorCatalogExceptionFluent
import org.onap.ccsdk.error.catalog.core.ErrorMessage
import org.onap.ccsdk.error.catalog.core.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.core.ErrorPayload
import org.onap.ccsdk.error.catalog.core.GrpcErrorCodes
import org.onap.ccsdk.error.catalog.core.HttpErrorCodes

/**
 *
 *
 * @author Brinda Santh
 */
class BluePrintException : ErrorCatalogException, ErrorCatalogExceptionFluent<BluePrintException> {

    constructor(cause: Throwable) : super(cause)
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(cause, message, args)
    constructor(code: Int, cause: Throwable) : super(code, cause)
    constructor(code: Int, message: String) : super(code, message)
    constructor(code: Int, message: String, cause: Throwable) : super(code, message, cause)

    override fun code(code: Int): BluePrintException {
        this.code = code
        return this
    }

    override fun domain(domain: String): BluePrintException {
        this.domain = domain
        return this
    }

    override fun action(action: String): BluePrintException {
        this.action = action
        return this
    }

    override fun http(type: String): BluePrintException {
        this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP
        this.code = HttpErrorCodes.code(type)
        return this
    }

    override fun grpc(type: String): BluePrintException {
        this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC
        this.code = GrpcErrorCodes.code(type)
        return this
    }

    override fun payloadMessage(message: String): BluePrintException {
        if (this.errorPayloadMessages == null) this.errorPayloadMessages = arrayListOf()
        this.errorPayloadMessages!!.add(message)
        return this
    }

    override fun addErrorPayloadMessage(message: String): BluePrintException {
        if (errorPayload == null) {
            errorPayload = ErrorPayload()
        }
        errorPayload!!.message = "${errorPayload!!.message} $messageSeparator $message"
        return this
    }

    override fun addSubError(errorMessage: ErrorMessage): BluePrintException {
        if (errorPayload == null) {
            errorPayload = ErrorPayload()
        }
        errorPayload!!.subErrors.add(errorMessage)
        return this
    }
}

/** Extension Functions */

fun BluePrintException.updateErrorMessage(domain: String, message: String, cause: Throwable):
        BluePrintException {
    return this.addDomainAndErrorMessage(domain, message, cause).domain(domain)
            .addErrorPayloadMessage(message)
            .payloadMessage(message)
}

fun BluePrintException.updateErrorMessage(domain: String, message: String): BluePrintException {
    return this.addDomainAndErrorMessage(domain, message).domain(domain)
            .addErrorPayloadMessage(message)
            .payloadMessage(message)
}

private fun BluePrintException.addDomainAndErrorMessage(
        domain: String,
        message: String,
        cause: Throwable = Throwable()
): BluePrintException {
    return this.addSubError(ErrorMessage(domain, message, cause.message ?: "")).domain(domain)
}
