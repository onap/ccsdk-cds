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

package org.onap.ccsdk.cds.error.catalog.services

import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogCodes
import org.onap.ccsdk.cds.error.catalog.core.ErrorCatalogException
import org.onap.ccsdk.cds.error.catalog.core.ErrorPayload
import org.onap.ccsdk.cds.error.catalog.core.HttpErrorCodes
import org.onap.ccsdk.cds.error.catalog.core.utils.errorCauseOrDefault
import org.onap.ccsdk.cds.error.catalog.core.utils.errorMessageOrDefault
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.dao.IncorrectResultSizeDataAccessException
import org.springframework.http.ResponseEntity
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.server.ServerWebInputException

abstract class ErrorCatalogExceptionHandler(private val errorCatalogService: ErrorCatalogService) {

    @ExceptionHandler(ErrorCatalogException::class)
    fun errorCatalogException(e: ErrorCatalogException): ResponseEntity<ErrorPayload> {
        val errorPayload = errorCatalogService.errorPayload(e)
        return errorPayload.toResponseEntity()
    }

    @ExceptionHandler
    fun errorCatalogException(e: ServerWebInputException): ResponseEntity<ErrorPayload> {
        val error = ErrorCatalogException(
            HttpErrorCodes.code(ErrorCatalogCodes.REQUEST_NOT_FOUND),
            e.errorMessageOrDefault(), e.errorCauseOrDefault()
        )
        val errorPayload = ErrorPayload(error.code, error.name, error.errorMessageOrDefault())
        return errorPayload.toResponseEntity()
    }

    @ExceptionHandler
    fun errorCatalogException(e: IncorrectResultSizeDataAccessException): ResponseEntity<ErrorPayload> {
        val error = ErrorCatalogException(
            HttpErrorCodes.code(ErrorCatalogCodes.DUPLICATE_DATA),
            e.errorMessageOrDefault(), e.errorCauseOrDefault()
        )
        val errorPayload = ErrorPayload(error.code, error.name, error.errorMessageOrDefault())
        return errorPayload.toResponseEntity()
    }

    @ExceptionHandler
    fun errorCatalogException(e: EmptyResultDataAccessException): ResponseEntity<ErrorPayload> {
        val error = ErrorCatalogException(
            HttpErrorCodes.code(ErrorCatalogCodes.RESOURCE_NOT_FOUND),
            e.errorMessageOrDefault(), e.errorCauseOrDefault()
        )
        val errorPayload = ErrorPayload(error.code, error.name, error.errorMessageOrDefault())
        return errorPayload.toResponseEntity()
    }

    @ExceptionHandler
    fun errorCatalogException(e: JpaObjectRetrievalFailureException): ResponseEntity<ErrorPayload> {
        val error = ErrorCatalogException(
            HttpErrorCodes.code(ErrorCatalogCodes.RESOURCE_NOT_FOUND),
            e.errorMessageOrDefault(), e.errorCauseOrDefault()
        )
        val errorPayload = ErrorPayload(error.code, error.name, error.errorMessageOrDefault())
        return errorPayload.toResponseEntity()
    }

    @ExceptionHandler
    fun errorCatalogException(e: Exception): ResponseEntity<ErrorPayload> {
        val error = ErrorCatalogException(
            HttpErrorCodes.code(ErrorCatalogCodes.GENERIC_FAILURE),
            e.errorMessageOrDefault(), e.errorCauseOrDefault()
        )
        val errorPayload = ErrorPayload(error.code, error.name, error.errorMessageOrDefault())
        return errorPayload.toResponseEntity()
    }
}
