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
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.data.ErrorModel
import org.onap.ccsdk.cds.blueprintsprocessor.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.cds.controllerblueprints.core.ErrorCatalogDomainsConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment

/**
 * SdcListenerErrorCodes Purpose: Maintain a list of error code into SDC Listener with their corresponding message
 *
 * @author Steve Siani
 * @version 1.0
 */
enum class SdcListenerErrorCodes(
    val domain: String = ErrorCatalogDomainsConstants.SDC_LISTENER,
    val httpCode: Int,
    val grpcCode: Int
) : EnumErrorCatalogInterface {
    GENERIC_FAILURE(httpCode = 500, grpcCode = 2) {
        override fun getErrorModel(): ErrorModel {
            return findErrorModel()
        }
    };

    companion object {
        private val map = HashMap<String, BlueprintProcessorErrorCodes>()
        private const val DEFAULT_ERROR_ID = "GENERIC_FAILURE"

        @Autowired
        private lateinit var env: Environment
        private var errorCatalogServiceImpl = ErrorCatalogServiceImpl(env)

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
        return errorCatalogServiceImpl.getErrorModel(name, domain)
        ?: GENERIC_FAILURE.getErrorModel()
    }
}
