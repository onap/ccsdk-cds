/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2018 - 2020 IBM, Bell Canada.
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

import org.apache.commons.lang.exception.ExceptionUtils
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogException
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogExceptionFluent
import org.onap.ccsdk.cds.error.catalog.core.ErrorMessage

/**
 *
 *
 * @author Brinda Santh
 */
open class BluePrintProcessorException : ErrorCatalogException, ErrorCatalogExceptionFluent<BluePrintProcessorException> {

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(cause, message, args)
    constructor(code: Int, cause: Throwable) : super(code, cause)
    constructor(code: Int, message: String) : super(code, message)
    constructor(code: Int, message: String, cause: Throwable) : super(code, message, cause)

    override fun code(code: Int): BluePrintProcessorException {
        return this.updateCode(code)
    }

    override fun domain(domain: String): BluePrintProcessorException {
        return this.updateDomain(domain)
    }

    override fun action(action: String): BluePrintProcessorException {
        return this.updateAction(action)
    }

    override fun http(type: String): BluePrintProcessorException {
        return this.updateHttp(type)
    }

    override fun grpc(type: String): BluePrintProcessorException {
        return this.updateGrpc(type)
    }

    override fun convertToHttp(): BluePrintProcessorException {
        return this.inverseToHttp()
    }

    override fun convertToGrpc(): BluePrintProcessorException {
        return this.inverseToHttp()
    }

    override fun payloadMessage(message: String): BluePrintProcessorException {
        return this.updatePayloadMessage(message)
    }

    override fun addErrorPayloadMessage(message: String): BluePrintProcessorException {
        return this.updateErrorPayloadMessage(message)
    }

    override fun addSubError(errorMessage: ErrorMessage): BluePrintProcessorException {
        return this.updateSubError(errorMessage)
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

fun processorException(message: String, cause: Throwable): BluePrintProcessorException {
    return BluePrintProcessorException(message, cause)
}

fun processorException(cause: Throwable, message: String, vararg args: Any?): BluePrintProcessorException {
    return BluePrintProcessorException(cause, message, args)
}

fun processorException(code: Int, message: String): BluePrintProcessorException {
    return processorException(message).code(code)
}

fun processorException(code: Int, message: String, cause: Throwable): BluePrintProcessorException {
    return processorException(message, cause).code(code)
}

fun processorException(code: Int, cause: Throwable, message: String, vararg args: Any?): BluePrintProcessorException {
    return processorException(cause, message, args).code(code)
}

fun httpProcessorException(type: String, message: String): BluePrintProcessorException {
    return processorException(message).http(type)
}

fun grpcProcessorException(type: String, message: String): BluePrintProcessorException {
    return processorException(message).grpc(type)
}

fun httpProcessorException(type: String, domain: String, message: String): BluePrintProcessorException {
    val bluePrintProcessorException = processorException(message).http(type)
    return bluePrintProcessorException.addDomainAndErrorMessage(domain, message)
}

fun grpcProcessorException(type: String, domain: String, message: String): BluePrintProcessorException {
    val bluePrintProcessorException = processorException(message).grpc(type)
    return bluePrintProcessorException.addDomainAndErrorMessage(domain, message)
}

fun httpProcessorException(type: String, domain: String, message: String, cause: Throwable):
    BluePrintProcessorException {
        val bluePrintProcessorException = processorException(message, cause).http(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun grpcProcessorException(type: String, domain: String, message: String, cause: Throwable):
    BluePrintProcessorException {
        val bluePrintProcessorException = processorException(message, cause).grpc(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun httpProcessorException(type: String, domain: String, message: String, cause: Throwable, vararg args: Any?):
    BluePrintProcessorException {
        val bluePrintProcessorException = processorException(cause, message, args).http(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun grpcProcessorException(type: String, domain: String, message: String, cause: Throwable, vararg args: Any?):
    BluePrintProcessorException {
        val bluePrintProcessorException = processorException(cause, message, args).grpc(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun BluePrintProcessorException.updateErrorMessage(domain: String, message: String, cause: String):
    BluePrintProcessorException {
        return this.addDomainAndErrorMessage(domain, message, cause).domain(domain)
            .addErrorPayloadMessage(message)
            .payloadMessage(message)
    }

fun BluePrintProcessorException.updateErrorMessage(domain: String, message: String): BluePrintProcessorException {
    return this.addDomainAndErrorMessage(domain, message).domain(domain)
        .addErrorPayloadMessage(message)
        .payloadMessage(message)
}

private fun BluePrintProcessorException.addDomainAndErrorMessage(
    domain: String,
    message: String,
    cause: String = ""
): BluePrintProcessorException {
    return this.addSubError(ErrorMessage(domain, message, cause)).domain(domain)
}
