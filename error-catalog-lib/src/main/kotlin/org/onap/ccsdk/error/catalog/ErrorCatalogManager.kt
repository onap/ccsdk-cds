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

package org.onap.ccsdk.error.catalog

import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.springframework.boot.logging.LogLevel

abstract class ErrorCatalogManager(private val errorCatalogService: ErrorCatalogService) {

    fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String,
        message: String
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, message, null)
    }

    fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String,
        message: String, cause: Throwable?
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, null, message, cause ?: Throwable())
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String,
        code: Int?, message: String
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, code, message, null)
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String,
        code: Int?, message: String, cause: Throwable?
    ): ErrorCatalog {
        return errorCatalogService.getErrorCatalog(enumErrorCatalog, protocol, code, message, cause ?: Throwable())
    }

    abstract fun generateException(
        errorCatalog: ErrorCatalog, message: String,
        logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, message: String,
        logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, message: String, cause: Throwable,
        logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, message: String, cause: Throwable,
        logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, message: String,
        logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message)
        val errorCatalogException = generateException(errorCatalog, message, logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, message: String,
        logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message)
        val errorCatalogException = generateException(errorCatalog, message, logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, message: String,
        logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message)
        val errorCatalogException = generateException(errorCatalog, message, logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, message: String,
        logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message)
        val errorCatalogException = generateException(errorCatalog, message, logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, message: String,
        cause: Throwable?, logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message, cause)
        val errorCatalogException = generateException(errorCatalog, message, cause ?: Throwable(), logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, message: String,
        cause: Throwable?, logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message, cause)
        val errorCatalogException = generateException(errorCatalog, message, cause ?: Throwable(), logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, message: String, cause: Throwable?,
        logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message, cause)
        val errorCatalogException = generateException(errorCatalog, message, cause ?: Throwable(), logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface,
        protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, message: String,
        cause: Throwable?, logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message, cause)
        val errorCatalogException = generateException(errorCatalog, message, cause ?: Throwable(), logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }
}
