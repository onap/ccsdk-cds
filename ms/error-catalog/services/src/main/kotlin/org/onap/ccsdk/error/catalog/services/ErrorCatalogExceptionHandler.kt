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

package org.onap.ccsdk.error.catalog.services

import org.onap.ccsdk.error.catalog.core.ErrorCatalogException
import org.onap.ccsdk.error.catalog.core.ErrorPayload
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler

abstract class ErrorCatalogExceptionHandler(private val errorCatalogService: ErrorCatalogService) {

    @ExceptionHandler(ErrorCatalogException::class)
    fun errorCatalogException(e: ErrorCatalogException): ResponseEntity<ErrorPayload> {
        val errorPayload = errorCatalogService.errorPayload(e)
        return errorPayload.toResponseEntity()
    }
}
