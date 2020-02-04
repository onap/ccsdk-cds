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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.data

import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data.Status
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.interfaces.ErrorInterface


open class CDSError : ErrorInterface {
    constructor()

    constructor(code: Int = 500, message: String = "", status: String = Status.FAILED.toString(),
                timestamp: String = "", debugMessage: String = "", subErrors: ArrayList<ErrorModel> = ArrayList()) {
        this.code = code
        this.message = message
        this.status = status
        this.timestamp = timestamp
        this.debugMessage = debugMessage
        this.subErrors = subErrors
    }
}