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

package org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.BlueprintProcessorErrorCodes
import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.ErrorCatalogManagerImpl
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.error.catalog.ErrorPayload
import org.onap.ccsdk.error.catalog.data.ErrorMessageLibConstants
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogExceptionHandler
import org.onap.ccsdk.error.catalog.utils.ErrorCatalogUtils
import org.onap.ccsdk.error.catalog.utils.errorCauseOrDefault
import org.onap.ccsdk.error.catalog.utils.errorMessageOrDefault
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * ExecutionServiceExceptionHandler.kt Purpose: Handle exceptions in selfservice API and provide the right
 * HTTP code status
 *
 * @author Steve Siani
 * @version 1.0
 */
@RestControllerAdvice("org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api")
class ExecutionServiceExceptionHandler : ErrorCatalogExceptionHandler {
    @Autowired
    lateinit var errorManager: ErrorCatalogManagerImpl

    @ExceptionHandler(BluePrintProcessorException::class)
    fun errorCatalogException(e: BluePrintProcessorException): ResponseEntity<ErrorPayload> {
        return ErrorCatalogUtils.returnResponseEntity(errorManager.generateException(
                BlueprintProcessorErrorCodes.GENERIC_PROCESS_FAILURE,
                ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP,
                e.errorMessageOrDefault(), e.errorCauseOrDefault()))
    }

    @ExceptionHandler(Exception::class)
    fun errorCatalogException(e: Exception): ResponseEntity<ErrorPayload> {
        return ErrorCatalogUtils.returnResponseEntity(errorManager.generateException(
                BlueprintProcessorErrorCodes.GENERIC_PROCESS_FAILURE,
                ErrorMessageLibConstants.ERROR_CATALOG_PROTOCOL_HTTP,
                e.errorMessageOrDefault(), e.errorCauseOrDefault()))
    }
}
