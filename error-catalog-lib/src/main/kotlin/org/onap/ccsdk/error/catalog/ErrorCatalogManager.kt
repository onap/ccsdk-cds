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
import org.onap.ccsdk.error.catalog.data.LogLevel
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService

abstract class ErrorCatalogManager(private val errorCatalogService: ErrorCatalogService) {

    private fun getErrorCatalog(
            enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, message: String
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, message, null)
    }

    private fun getErrorCatalog(
            enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, message: String,
            cause: Throwable?
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, null, message, cause)
    }

    private fun getErrorCatalog(
            enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, code: Int?, message: String
    ): ErrorCatalog {
        return errorCatalogService.getErrorCatalog(enumErrorCatalog, protocol, code, message, Throwable())
    }

    private fun getErrorCatalog(
            enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, code: Int?, message: String,
            cause: Throwable?
    ): ErrorCatalog {
        return errorCatalogService.getErrorCatalog(enumErrorCatalog, protocol, code, message, cause)
    }

    abstract fun generateException(
            errorCatalog: ErrorCatalog, errorMessage: String, logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException

    abstract fun generateException(
            errorCatalog: ErrorCatalog, errorMessage: String, logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException

    abstract fun generateException(
            errorCatalog: ErrorCatalog, errorMessage: String, errorCause: Throwable,
            logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException

    abstract fun generateException(
            errorCatalog: ErrorCatalog, errorMessage: String, errorCause: Throwable,
            logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, errorMessage: String,
            logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage)
        val errorCatalogException = generateException(errorCatalog, errorMessage, logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, errorMessage: String,
            logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, errorMessage)
        val errorCatalogException = generateException(errorCatalog, errorMessage, logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, errorMessage: String,
            logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage)
        val errorCatalogException = generateException(errorCatalog, errorMessage, logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, errorMessage: String,
            logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, errorMessage)
        val errorCatalogException = generateException(errorCatalog, errorMessage, logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, errorMessage: String,
            errorCause: Throwable, logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage, errorCause)
        val errorCatalogException = generateException(errorCatalog, errorMessage, errorCause, logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, errorMessage: String,
            errorCause: Throwable, logLevel: String = LogLevel.ERROR.name
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, errorMessage, errorCause)
        val errorCatalogException = generateException(errorCatalog, errorMessage, errorCause, logLevel)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, errorMessage: String, errorCause: Throwable,
            logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage, errorCause)
        val errorCatalogException = generateException(errorCatalog, errorMessage, errorCause, logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }

    fun generateException(
            enumErrorCatalog: EnumErrorCatalogInterface,
            protocol: String = ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_GRPC, code: Int?, errorMessage: String,
            errorCause: Throwable, logLevel: String = LogLevel.ERROR.name, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, errorMessage, errorCause)
        val errorCatalogException = generateException(errorCatalog, errorMessage, errorCause, logLevel, args)
        errorCatalogException.setProtocolsCode(enumErrorCatalog)
        return errorCatalogException
    }
}
