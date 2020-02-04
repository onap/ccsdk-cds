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

package org.onap.ccsdk.cds.blueprintsprocessor.services.execution.error.data

import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException
import org.onap.ccsdk.error.catalog.ErrorPayload
import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.onap.ccsdk.error.catalog.data.LogLevel

class CDSErrorException : ErrorCatalogException {
    constructor(errorCatalog: ErrorCatalog, errorMessage: String, logLevel: String = LogLevel.ERROR.name) :
            super(errorCatalog, errorMessage, logLevel)

    constructor(errorCatalog: ErrorCatalog, errorMessage: String, logLevel: String = LogLevel.ERROR.name, vararg args: Any?) :
            super(errorCatalog, errorMessage, logLevel, args)

    constructor(
        errorCatalog: ErrorCatalog,
        errorMessage: String,
        errorCause: Throwable,
        logLevel: String = LogLevel.ERROR.name
    ) :
            super(errorCatalog, errorMessage, errorCause, logLevel)

    constructor(
        errorCatalog: ErrorCatalog,
        errorMessage: String,
        errorCause: Throwable,
        logLevel: String = LogLevel.ERROR.name,
        vararg args: Any?
    ) :
            super(errorCatalog, errorMessage, errorCause, logLevel, args)

    constructor(errorPayload: ErrorPayload, domainId: String, message: String) :
            super(errorPayload, domainId, message)

    constructor(errorPayload: ErrorPayload, domainId: String, message: String, cause: Throwable) :
            super(errorPayload, domainId, message, cause)

    constructor(
        errorPayload: ErrorPayload,
        domainId: String,
        cause: Throwable,
        message: String,
        vararg args: Any?
    ) : super(errorPayload, domainId, cause, message, args)
}
