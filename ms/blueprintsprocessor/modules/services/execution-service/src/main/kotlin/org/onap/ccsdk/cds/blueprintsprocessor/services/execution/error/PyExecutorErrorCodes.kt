/*
 *  Copyright © 2019 IBM, Bell Canada.
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
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalogue.interfaces.EnumErrorCatalogueInterface
import org.onap.ccsdk.cds.controllerblueprints.core.ErrorCodeDomainsConstants
import org.springframework.beans.factory.annotation.Autowired

/**
 * ErrorCode.java Purpose: Maintain a list of error code with their corresponding message
 *
 * @author Steve Siani
 * @version 1.0
 */
enum class PyExecutorErrorCodes(val domain: String = ErrorCodeDomainsConstants.PYTHON_EXECUTOR): EnumErrorCatalogueInterface {
    GENERIC_FAILURE() {
        override fun getErrorModel(): ErrorModel {
            return findErrorModel()
        }
    };

    companion object {
        private val map = HashMap<String, BlueprintProcessorErrorCodes>()
        private const val DEFAULT_ERROR_ID = "GENERIC_FAILURE"

        @Autowired
        lateinit var errorCatalogueServiceImpl: ErrorCatalogueServiceImpl

        init {
            for (enumErrorCode in BlueprintProcessorErrorCodes.values()) {
                map[enumErrorCode.name] = enumErrorCode
            }
        }

        fun valueOf(value: String): BlueprintProcessorErrorCodes {
            return if (map.containsKey(value)) map[value]!! else map[DEFAULT_ERROR_ID]!!
        }
    }

    protected fun findErrorModel(): ErrorModel {
        return errorCatalogueServiceImpl.getErrorModel(name, domain) ?:
        GENERIC_FAILURE.getErrorModel()
    }
}
