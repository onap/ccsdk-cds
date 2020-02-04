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
    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, errorMessage: String) :
            super(errorCatalog, logLevel, errorMessage)

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, errorMessage: String, vararg args: Any?) :
            super(errorCatalog, logLevel, errorMessage, args)

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, errorMessage: String, errorCause: Throwable) :
            super(errorCatalog, logLevel, errorMessage, errorCause)

    constructor(errorCatalog: ErrorCatalog, logLevel: String = LogLevel.ERROR.name, errorMessage: String, errorCause: Throwable, vararg args: Any?) :
            super(errorCatalog, logLevel, errorMessage, errorCause, args)

    constructor(errorPayload: ErrorPayload, domainId: String, errorMessage: String) :
            super(errorPayload, domainId, errorMessage)

    constructor(errorPayload: ErrorPayload, domainId: String, errorMessage: String, errorCause: Throwable) :
            super(errorPayload, domainId, errorMessage, errorCause)

    constructor(errorPayload: ErrorPayload, domainId: String, errorMessage: String, errorCause: Throwable, vararg args: Any?) :
            super(errorPayload, domainId, errorMessage, errorCause, args)
}
