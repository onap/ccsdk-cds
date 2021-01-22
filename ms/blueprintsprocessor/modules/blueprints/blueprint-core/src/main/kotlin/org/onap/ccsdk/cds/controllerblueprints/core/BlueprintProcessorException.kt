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
open class BlueprintProcessorException : ErrorCatalogException, ErrorCatalogExceptionFluent<BlueprintProcessorException> {

    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(cause, message, args)
    constructor(code: Int, cause: Throwable) : super(code, cause)
    constructor(code: Int, message: String) : super(code, message)
    constructor(code: Int, message: String, cause: Throwable) : super(code, message, cause)

    override fun code(code: Int): BlueprintProcessorException {
        return this.updateCode(code)
    }

    override fun domain(domain: String): BlueprintProcessorException {
        return this.updateDomain(domain)
    }

    override fun action(action: String): BlueprintProcessorException {
        return this.updateAction(action)
    }

    override fun http(type: String): BlueprintProcessorException {
        return this.updateHttp(type)
    }

    override fun grpc(type: String): BlueprintProcessorException {
        return this.updateGrpc(type)
    }

    override fun convertToHttp(): BlueprintProcessorException {
        return this.inverseToHttp()
    }

    override fun convertToGrpc(): BlueprintProcessorException {
        return this.inverseToHttp()
    }

    override fun payloadMessage(message: String): BlueprintProcessorException {
        return this.updatePayloadMessage(message)
    }

    override fun addErrorPayloadMessage(message: String): BlueprintProcessorException {
        return this.updateErrorPayloadMessage(message)
    }

    override fun addSubError(errorMessage: ErrorMessage): BlueprintProcessorException {
        return this.updateSubError(errorMessage)
    }
}

class BlueprintRetryException : RuntimeException {
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(cause: Throwable, message: String, vararg args: Any?) : super(format(message, *args), cause)
}

/** Extension Functions */

fun processorException(message: String): BlueprintProcessorException {
    return BlueprintProcessorException(message)
}

fun processorException(message: String, cause: Throwable): BlueprintProcessorException {
    return BlueprintProcessorException(message, cause)
}

fun processorException(cause: Throwable, message: String, vararg args: Any?): BlueprintProcessorException {
    return BlueprintProcessorException(cause, message, args)
}

fun processorException(code: Int, message: String): BlueprintProcessorException {
    return processorException(message).code(code)
}

fun processorException(code: Int, message: String, cause: Throwable): BlueprintProcessorException {
    return processorException(message, cause).code(code)
}

fun processorException(code: Int, cause: Throwable, message: String, vararg args: Any?): BlueprintProcessorException {
    return processorException(cause, message, args).code(code)
}

fun httpProcessorException(type: String, message: String): BlueprintProcessorException {
    return processorException(message).http(type)
}

fun grpcProcessorException(type: String, message: String): BlueprintProcessorException {
    return processorException(message).grpc(type)
}

fun httpProcessorException(type: String, domain: String, message: String): BlueprintProcessorException {
    val bluePrintProcessorException = processorException(message).http(type)
    return bluePrintProcessorException.addDomainAndErrorMessage(domain, message)
}

fun grpcProcessorException(type: String, domain: String, message: String): BlueprintProcessorException {
    val bluePrintProcessorException = processorException(message).grpc(type)
    return bluePrintProcessorException.addDomainAndErrorMessage(domain, message)
}

fun httpProcessorException(type: String, domain: String, message: String, cause: Throwable):
    BlueprintProcessorException {
        val bluePrintProcessorException = processorException(message, cause).http(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun grpcProcessorException(type: String, domain: String, message: String, cause: Throwable):
    BlueprintProcessorException {
        val bluePrintProcessorException = processorException(message, cause).grpc(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun httpProcessorException(type: String, domain: String, message: String, cause: Throwable, vararg args: Any?):
    BlueprintProcessorException {
        val bluePrintProcessorException = processorException(cause, message, args).http(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun grpcProcessorException(type: String, domain: String, message: String, cause: Throwable, vararg args: Any?):
    BlueprintProcessorException {
        val bluePrintProcessorException = processorException(cause, message, args).grpc(type)
        return bluePrintProcessorException.addDomainAndErrorMessage(domain, message, ExceptionUtils.getRootCauseMessage(cause))
    }

fun BlueprintProcessorException.updateErrorMessage(domain: String, message: String, cause: String):
    BlueprintProcessorException {
        return this.addDomainAndErrorMessage(domain, message, cause).domain(domain)
            .addErrorPayloadMessage(message)
            .payloadMessage(message)
    }

fun BlueprintProcessorException.updateErrorMessage(domain: String, message: String): BlueprintProcessorException {
    return this.addDomainAndErrorMessage(domain, message).domain(domain)
        .addErrorPayloadMessage(message)
        .payloadMessage(message)
}

private fun BlueprintProcessorException.addDomainAndErrorMessage(
    domain: String,
    message: String,
    cause: String = ""
): BlueprintProcessorException {
    return this.addSubError(ErrorMessage(domain, message, cause)).domain(domain)
}
