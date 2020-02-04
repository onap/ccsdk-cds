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

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants.Companion.ERROR_CATALOG_DEFAULT_ERROR_CODE
import org.onap.ccsdk.error.catalog.data.ErrorMessage
import org.onap.ccsdk.error.catalog.data.LogLevel
import java.time.LocalDateTime

class ErrorPayload {
    @get:JsonProperty("code")
    var code: Int = ERROR_CATALOG_DEFAULT_ERROR_CODE
    @get:JsonProperty("status")
    var status: String = ""
    @get:JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    var timestamp: LocalDateTime = LocalDateTime.now()
    @get:JsonProperty("message")
    var message: String = ""
    @get:JsonProperty("debugMessage")
    var debugMessage: String = ""
    @get:JsonProperty("logLevel")
    var logLevel: String = LogLevel.ERROR.name
    @get:JsonProperty("subErrors")
    var subErrors: ArrayList<ErrorMessage> = ArrayList()

    constructor()

    constructor(code: Int = ErrorMessageLibConstants.ERROR_CATALOG_DEFAULT_ERROR_CODE, status: String,
                logLevel: String = LogLevel.ERROR.name, debugMessage: String = "") {
        this.code = code
        this.status = status
        this.logLevel = logLevel
        this.debugMessage = debugMessage
    }

    constructor(code: Int = ErrorMessageLibConstants.ERROR_CATALOG_DEFAULT_ERROR_CODE, status: String,
                logLevel: String = LogLevel.ERROR.name, debugMessage: String = "",
                errorMessage: ErrorMessage) {
        this.code = code
        this.debugMessage = debugMessage
        this.status = status
        this.logLevel = logLevel
        this.subErrors.add(errorMessage)
    }
}
