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

package org.onap.ccsdk.error.catalog.interfaces

import org.onap.ccsdk.error.catalog.data.ErrorCatalog

interface EnumErrorCatalogInterface {
    fun getErrorCatalog(): ErrorCatalog

    fun getMessage(detailMsg: String = ""): String {
        return "Cause: ${getErrorCatalog().action} \n Action: ${getErrorCatalog().cause}  \n\n\t $detailMsg"
    }

    fun getErrorCause(): String {
        return getErrorCatalog().cause
    }

    fun getErrorAction(): String {
        return getErrorCatalog().action
    }

    fun getErrorDomain(): String {
        return getErrorCatalog().domainId
    }

    fun getErrorName(): String {
        return getErrorCatalog().errorId
    }
}
