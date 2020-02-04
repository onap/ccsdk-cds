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
import org.onap.ccsdk.error.catalog.ErrorCatalogManager
import org.onap.ccsdk.error.catalog.data.ErrorCatalog

class ErrorCatalogManagerImpl : ErrorCatalogManager() {
    override fun generateException(errorCatalog: ErrorCatalog, logLevel: String, errorMessage: String): CDSErrorException {
        return CDSErrorException(errorCatalog, logLevel, errorMessage)
    }

    override fun generateException(errorCatalog: ErrorCatalog, logLevel: String, errorMessage: String, errorCause: Throwable): CDSErrorException {
        return CDSErrorException(errorCatalog, logLevel, errorMessage, errorCause)
    }

    override fun generateException(errorCatalog: ErrorCatalog, logLevel: String, errorMessage: String, vararg args: Any?): CDSErrorException {
        return CDSErrorException(errorCatalog, logLevel, errorMessage, args)
    }

    override fun generateException(errorCatalog: ErrorCatalog, logLevel: String, errorMessage: String, errorCause: Throwable, vararg args: Any?): CDSErrorException {
        return CDSErrorException(errorCatalog, logLevel, errorMessage, errorCause, args)
    }
}
