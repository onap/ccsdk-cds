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

import org.onap.ccsdk.cds.error.catalog.services.ErrorCatalogExceptionHandler
import org.onap.ccsdk.cds.error.catalog.services.ErrorCatalogService
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * ExecutionServiceExceptionHandler.kt Purpose: Handle exceptions in selfservice API and provide the right
 * HTTP code status
 *
 * @author Steve Siani
 * @version 1.0
 */
@RestControllerAdvice("org.onap.ccsdk.cds.blueprintsprocessor.selfservice.api")
class ExecutionServiceExceptionHandler(private val errorCatalogService: ErrorCatalogService) :
    ErrorCatalogExceptionHandler(errorCatalogService)
