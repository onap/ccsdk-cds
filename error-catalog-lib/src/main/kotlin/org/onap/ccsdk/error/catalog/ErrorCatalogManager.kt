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
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorMessage: String
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, errorMessage, Throwable())
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorMessage: String,
        errorCause: Throwable
    ): ErrorCatalog {
        return getErrorCatalog(enumErrorCatalog, protocol, null, errorMessage, errorCause)
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorCode: Int?, errorMessage: String
    ): ErrorCatalog {
        return errorCatalogService.getErrorCatalog(enumErrorCatalog, protocol, errorCode, errorMessage, Throwable())
    }

    private fun getErrorCatalog(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorCode: Int?, errorMessage: String,
        errorCause: Throwable
    ): ErrorCatalog {
        return errorCatalogService.getErrorCatalog(enumErrorCatalog, protocol, errorCode, errorMessage, errorCause)
    }

    abstract fun generateException(
        errorCatalog: ErrorCatalog,
        logLevel: String = LogLevel.ERROR.name, errorMessage: String
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name,
        errorMessage: String, errorCause: Throwable
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, errorMessage: String,
        vararg args: Any?
    ): ErrorCatalogException

    abstract fun generateException(
        errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, errorMessage: String,
        vararg args: Any?, errorCause: Throwable
    ): ErrorCatalogException

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String,
        logLevel: String = LogLevel.ERROR.name, errorMessage: String
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage)
        return generateException(errorCatalog, logLevel, errorMessage)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, errorCode: Int?,
        logLevel: String = LogLevel.ERROR.name, errorMessage: String
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorCode, errorMessage)
        return generateException(errorCatalog, logLevel, errorMessage)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name,
        errorMessage: String, errorCause: Throwable
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage, errorCause)
        return generateException(errorCatalog, logLevel, errorMessage, errorCause)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name, errorMessage: String,
        vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage)
        return generateException(errorCatalog, logLevel, errorMessage, args)
    }

    fun generateException(
        enumErrorCatalog: EnumErrorCatalogInterface, protocol: String, logLevel: String = LogLevel.ERROR.name, errorCause: Throwable,
        errorMessage: String, vararg args: Any?
    ): ErrorCatalogException {
        val errorCatalog = getErrorCatalog(enumErrorCatalog, protocol, errorMessage, errorCause)
        return generateException(errorCatalog, logLevel, errorMessage, errorCause, args)
    }
}