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

package org.onap.ccsdk.cds.error.catalog.core

import com.fasterxml.jackson.annotation.JsonFormat
import org.onap.ccsdk.cds.error.catalog.core.ErrorMessageLibConstants.ERROR_CATALOG_DEFAULT_ERROR_CODE
import org.slf4j.event.Level
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

open class ErrorPayload {

    var code: Int = ERROR_CATALOG_DEFAULT_ERROR_CODE
    var status: String = ""

    @get:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    var timestamp: Date = controllerDate()
    var message: String = ""
    var debugMessage: String = ""
    var logLevel: String = Level.ERROR.name
    val subErrors: ArrayList<ErrorMessage> = ArrayList()

    constructor()

    constructor(
        code: Int = ERROR_CATALOG_DEFAULT_ERROR_CODE,
        status: String,
        message: String,
        logLevel: String = Level.ERROR.name,
        debugMessage: String = ""
    ) {
        this.code = code
        this.status = status
        this.message = message
        this.logLevel = logLevel
        this.debugMessage = debugMessage
    }

    constructor(
        code: Int = ERROR_CATALOG_DEFAULT_ERROR_CODE,
        status: String,
        message: String,
        logLevel: String = Level.ERROR.name,
        debugMessage: String = "",
        errorMessage: ErrorMessage
    ) {
        this.code = code
        this.status = status
        this.message = message
        this.logLevel = logLevel
        this.debugMessage = debugMessage
        this.subErrors.add(errorMessage)
    }

    fun isEqualTo(errorPayload: ErrorPayload): Boolean {
        return (
            this.code == errorPayload.code && this.status == errorPayload.status && this.message == errorPayload.message &&
                this.logLevel == errorPayload.logLevel && this.debugMessage == errorPayload.debugMessage &&
                this.subErrors == errorPayload.subErrors
            )
    }

    private fun controllerDate(): Date {
        val localDateTime = LocalDateTime.now(ZoneId.systemDefault())
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }
}

/**
 *
 *
 * @author Steve Siani
 */
data class ErrorMessage(
    val domainId: String,
    val message: String,
    val cause: String
)

data class ErrorCatalog(
    val errorId: String,
    val domainId: String,
    val code: Int,
    val action: String,
    val cause: String
) {

    fun getMessage(): String {
        return "Cause: $cause ${System.lineSeparator()} Action : $action"
    }
}
