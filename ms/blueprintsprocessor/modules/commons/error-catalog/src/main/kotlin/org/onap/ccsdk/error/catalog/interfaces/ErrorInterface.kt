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

package org.onap.ccsdk.error.catalog.interfaces

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import org.onap.ccsdk.error.catalog.data.ErrorModel
import java.time.LocalDateTime

abstract class ErrorInterface {
    @get:JsonProperty("code")
    var code: Int = 500
    @get:JsonProperty("status")
    var status: String = ""
    @get:JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    var timestamp: LocalDateTime = LocalDateTime.now()
    @get:JsonProperty("message")
    var message: String = ""
    @get:JsonProperty("debugMessage")
    var debugMessage: String = ""
    @get:JsonProperty("subErrors")
    var subErrors: ArrayList<ErrorModel> = ArrayList()

    fun addError(errorCatalog: EnumErrorCatalogInterface) {
        subErrors.add(errorCatalog.getErrorModel())
    }
}
