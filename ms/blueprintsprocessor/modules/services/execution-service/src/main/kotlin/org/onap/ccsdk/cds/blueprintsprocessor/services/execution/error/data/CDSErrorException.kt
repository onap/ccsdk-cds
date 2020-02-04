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

import org.onap.ccsdk.error.catalog.ErrorPayload
import org.onap.ccsdk.error.catalog.data.ErrorCatalog
import org.springframework.boot.logging.LogLevel
import org.onap.ccsdk.error.catalog.interfaces.ErrorCatalogException

class CDSErrorException : ErrorCatalogException {
    constructor(errorCatalog: ErrorCatalog, message: String, logLevel: String = LogLevel.ERROR.name) :
            super(errorCatalog, message, logLevel)

    constructor(errorCatalog: ErrorCatalog, message: String, logLevel: String = LogLevel.ERROR.name, vararg args: Any?) :
            super(errorCatalog, message, logLevel, args)

    constructor(errorCatalog: ErrorCatalog, message: String, cause: Throwable, logLevel: String = LogLevel.ERROR.name) :
            super(errorCatalog, message, cause, logLevel)

    constructor(errorCatalog: ErrorCatalog, message: String, cause: Throwable, logLevel: String = LogLevel.ERROR.name, vararg args: Any?) :
            super(errorCatalog, message, cause, logLevel, args)

    constructor(errorPayload: ErrorPayload, domainId: String, message: String) :
            super(errorPayload, domainId, message)

    constructor(errorPayload: ErrorPayload, domainId: String, message: String, cause: Throwable) :
            super(errorPayload, domainId, message, cause)

    constructor(errorPayload: ErrorPayload, domainId: String, message: String, cause: Throwable, vararg args: Any?) :
            super(errorPayload, domainId, message, cause, args)
}
