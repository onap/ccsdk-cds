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
package org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data

/**
 *
 *
 * @author Steve Siani
 */
data class ErrorModel(
    val id: String,
    val domainId: String,
    val cause: String,
    val action: String,
    var logLevel: String = LogLevel.ERROR.toString(),
    var debugMessage: String = ""
) {
    fun message(): String {
        return "Cause: $cause \n\t Action: $action"
    }
}

enum class Status {
    SUCCESS,
    FAILED,
    ABORTED
}

enum class LogLevel {
    INFO,
    WARN,
    ERROR
}

