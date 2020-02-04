/*
 *  Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.interfaces

import com.fasterxml.jackson.annotation.JsonProperty
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data.Status

abstract class ErrorInterface: RuntimeException() {
    @get:JsonProperty("code")
    var code: Int= 500
    @get:JsonProperty("status")
    var status: String = Status.FAILED.toString()
    @get:JsonProperty("timestamp")
    var timestamp: String = ""
    @get:JsonProperty("message")
    override var message: String = ""
    @get:JsonProperty("debugMessage")
    var debugMessage: String = ""
    @get:JsonProperty("subErrors")
    var subErrors: ArrayList<ErrorModel> = ArrayList()

    fun addError(errorCatalogue: EnumErrorCatalogueInterface) {
        subErrors.add(errorCatalogue.getErrorModel())
    }
}