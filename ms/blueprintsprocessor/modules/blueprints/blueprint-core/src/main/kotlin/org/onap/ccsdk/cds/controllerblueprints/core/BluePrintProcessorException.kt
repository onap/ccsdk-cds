/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 IBM.
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
import org.onap.ccsdk.error.catalog.core.ErrorCatalogExceptionExtension
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
class BluePrintProcessorException : ErrorCatalogException, ErrorCatalogExceptionExtension<BluePrintProcessorException> {

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(cause, message, args)
    constructor(code: Int, cause: Throwable) : super(code, cause)
    constructor(code: Int, message: String) : super(code, message)
    constructor(code: Int, message: String, cause: Throwable) : super(code, message, cause)

    override fun code(code: Int): BluePrintProcessorException {
        this.code = code
        return this
    }

    override fun domain(domain: String): BluePrintProcessorException {
        this.domain = domain
        return this
    }

    override fun action(action: String): BluePrintProcessorException {
        this.action = action
        return this
    }

    override fun http(type: String): BluePrintProcessorException {
        this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP
        this.code = HttpErrorCodes.code(type)
        return this
    }

    override fun grpc(type: String): BluePrintProcessorException {
        this.protocol = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC
        this.code = GrpcErrorCodes.code(type)
        return this
    }

    override fun payloadMessage(message: String): BluePrintProcessorException {
        if (this.errorPayloadMessages == null) this.errorPayloadMessages = arrayListOf()
        this.errorPayloadMessages!!.add(message)
        return this
    }

    override fun addErrorPayloadMessage(message: String): BluePrintProcessorException {
        if (errorPayload == null) {
            errorPayload = ErrorPayload()
        }
        errorPayload!!.message = "${errorPayload!!.message} $messageSeparator $message"
        return this
    }

    override fun addSubError(errorMessage: ErrorMessage): BluePrintProcessorException {
        if (errorPayload == null) {
            errorPayload = ErrorPayload()
        }
        errorPayload!!.subErrors.add(errorMessage)
        return this
    }
}

class BluePrintRetryException : RuntimeException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause)
}

/** Extension Functions */

fun processorException(message: String): BluePrintProcessorException {
    return BluePrintProcessorException(message)
}

fun processorException(code: Int, message: String): BluePrintProcessorException {
    return processorException(message).code(code)
}

fun httpProcessorException(type: String, message: String): BluePrintProcessorException {
    return processorException(message).http(type)
}

fun grpcProcessorException(type: String, message: String): BluePrintProcessorException {
    return processorException(message).grpc(type)
}
