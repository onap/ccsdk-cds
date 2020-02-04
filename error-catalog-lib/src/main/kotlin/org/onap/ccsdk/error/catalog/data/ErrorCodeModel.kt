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
package org.onap.ccsdk.error.catalog.data

/**
 *
 *
 * @author Steve Siani
 */
data class ErrorMessage(
    val domainId: String,
    val message: String,
    val cause: Throwable?
)

data class ErrorCatalog(
        val errorId: String,
        val domainId: String,
        val action: String,
        val cause: String
)

enum class LogLevel {
    INFO,
    WARN,
    ERROR
}
