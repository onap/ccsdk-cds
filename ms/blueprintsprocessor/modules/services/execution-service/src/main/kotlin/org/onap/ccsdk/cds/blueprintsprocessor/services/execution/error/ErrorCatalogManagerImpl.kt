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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error

import org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.data.CDSErrorException
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.onap.ccsdk.error.catalog.ErrorCatalogManager
import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.springframework.stereotype.Service

@Service
class ErrorCatalogManagerImpl(private val errorCatalogService: ErrorCatalogService) : ErrorCatalogManager(errorCatalogService) {
    override fun generateException(errorCatalog: ErrorCatalog, errorMessage: String, logLevel: String): CDSErrorException {
        return CDSErrorException(errorCatalog, errorMessage, logLevel)
    }

    override fun generateException(errorCatalog: ErrorCatalog, errorMessage: String, errorCause: Throwable, logLevel: String): CDSErrorException {
        return CDSErrorException(errorCatalog, errorMessage, errorCause, logLevel)
    }

    override fun generateException(errorCatalog: ErrorCatalog, errorMessage: String, logLevel: String, vararg args: Any?): CDSErrorException {
        return CDSErrorException(errorCatalog, errorMessage, logLevel, args)
    }

    override fun generateException(errorCatalog: ErrorCatalog, errorMessage: String, errorCause: Throwable, logLevel: String, vararg args: Any?): CDSErrorException {
        return CDSErrorException(errorCatalog, errorMessage, errorCause, logLevel, args)
    }
}
