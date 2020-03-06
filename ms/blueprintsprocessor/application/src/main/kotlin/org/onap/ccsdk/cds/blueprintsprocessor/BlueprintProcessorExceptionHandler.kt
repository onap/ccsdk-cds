/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.blueprintsprocessor

import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintProcessorException
import org.onap.ccsdk.error.catalog.core.ErrorPayload
import org.onap.ccsdk.error.catalog.core.logger
import org.onap.ccsdk.error.catalog.services.ErrorCatalogService
import org.onap.ccsdk.error.catalog.services.toResponseEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice("org.onap.ccsdk.cd")
open class BlueprintProcessorExceptionHandler(private val errorCatalogService: ErrorCatalogService) {

    private val log = logger(BlueprintProcessorExceptionHandler::class)

    @ExceptionHandler
    fun bluePrintProcessorExceptionHandler(e: BluePrintProcessorException): ResponseEntity<ErrorPayload> {
        return errorCatalogService.errorPayload(e).toResponseEntity()
    }
}
