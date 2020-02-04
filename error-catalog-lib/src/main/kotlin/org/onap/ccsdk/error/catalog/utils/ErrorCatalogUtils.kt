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

package org.onap.ccsdk.error.catalog.utils

import org.onap.ccsdk.error.catalog.ErrorPayload
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.lang.Exception

object ErrorCatalogUtils {
    private const val REGEX_PATTERN = "^cause=(.*),action=(.*)"
    private val regex = REGEX_PATTERN.toRegex()

    fun readErrorCauseFromMessage(message: String): String {
        val matchResults = regex.matchEntire(message)
        return matchResults!!.groupValues[1]
    }
    fun readErrorActionFromMessage(message: String): String {
        val matchResults = regex.matchEntire(message)
        return matchResults!!.groupValues[2]
    }

    fun returnResponseEntity(errorException: ErrorCatalogException): ResponseEntity<ErrorPayload> {
        return ResponseEntity(errorException.errorPayload, HttpStatus.resolve(errorException.errorPayload.code)!!)
    }
}

fun Exception.errorCauseOrDefault(): Throwable{
    return this.cause ?: Throwable()
}

fun Exception.errorMessageOrDefault(): String{
    return this.message ?: ""
}

fun ErrorCatalogException.setProtocolsCode(enumErrorCatalog: EnumErrorCatalogInterface) {
    this.httpCode = enumErrorCatalog.getErrorHttpCode()
    this.grpcCode = enumErrorCatalog.getErrorGrpcCode()
}