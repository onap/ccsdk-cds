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
import org.onap.ccsdk.error.catalog.data.LogLevel
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.springframework.beans.factory.annotation.Autowired

abstract class ErrorCatalogManager {
    @Autowired
    lateinit var errorCatalogService: ErrorCatalogService

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, message: String
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, null, message)
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, code: Int?, message: String
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, code, message, Throwable())
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, message: String, cause: Throwable
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, null, message, cause)
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, code: Int?, message: String, cause: Throwable
    ): ErrorCatalog {
        return errorCatalogService.getErrorCatalog(enumErrorCatalog, protocol, code, message, cause)
    }


    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String, vararg args: Any?
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String, cause: Throwable
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, message: String, cause: Throwable, vararg args: Any?
    ): ErrorCatalogException


    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        message: String
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message)
        return generateException(errorCatalog, logLevel, message)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        code: Int?, message: String
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message)
        return generateException(errorCatalog, logLevel, message)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        message: String, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message)
        return generateException(errorCatalog, logLevel, message, args)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        code: Int?, message: String, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message)
        return generateException(errorCatalog, logLevel, message, args)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        message: String, cause: Throwable
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message, cause)
        return generateException(errorCatalog, logLevel, message, cause)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        code: Int?, message: String, cause: Throwable
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message, cause)
        return generateException(errorCatalog, logLevel, message, cause)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        message: String, cause: Throwable, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, message, cause)
        return generateException(errorCatalog, logLevel, message, cause, args)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        code: Int?, message: String, cause: Throwable, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, code, message, cause)
        return generateException(errorCatalog, logLevel, message, cause, args)
    }
}
