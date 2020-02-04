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

import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface

/**
 * Extended Error code list
 *
 * @author Steve Siani
 * @version 1.0
 */

/**
 * CDSErrorCodes.<ERROR_TYPE>.getErrorType Purpose: Return the error type with different domain
 *
 * @author Steve Siani
 * @version 1.0
 */
fun <T : CDSErrorCodes> T.getErrorType(domain: String):
        EnumErrorCatalogInterface {
    this.domain = domain
    return this
}
