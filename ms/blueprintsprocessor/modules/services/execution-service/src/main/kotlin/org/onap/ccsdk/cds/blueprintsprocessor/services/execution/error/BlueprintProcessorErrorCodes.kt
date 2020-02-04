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

import org.onap.ccsdk.cds.controllerblueprints.core.ErrorCatalogDomainsConstants
import org.onap.ccsdk.error.catalog.data.ErrorModel
import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface
import org.onap.ccsdk.error.catalog.service.ErrorCatalogService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope

/**
 * BlueprintProcessorErrorCodes Purpose: Maintain a list of error code into Blueprint Processor Run time with their corresponding message
 *
 * @author Steve Siani
 * @version 1.0
 */
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
enum class BlueprintProcessorErrorCodes(
    val domain: String = ErrorCatalogDomainsConstants.BLUEPRINT_PROCESSOR,
    val httpCode: Int,
    val grpcCode: Int
) : EnumErrorCatalogInterface {

    GENERIC_FAILURE(httpCode = 500, grpcCode = 2) {
        override fun getErrorModel(): ErrorModel {
            return findErrorModel()
        }
    },
    GENERIC_PROCESS_FAILURE(ErrorCatalogDomainsConstants.SELF_SERVICE_API, 500, 2) {
        override fun getErrorModel(): ErrorModel {
            return findErrorModel()
        }
    },
    INVALID_FILE_EXTENSION(ErrorCatalogDomainsConstants.SELF_SERVICE_API, 415, 3) {
        override fun getErrorModel(): ErrorModel {
            return findErrorModel()
        }
    };

    companion object {
        private val map = HashMap<String, BlueprintProcessorErrorCodes>()
        private const val DEFAULT_ERROR_ID = "GENERIC_FAILURE"
        @Autowired
        lateinit var errorCatalogServiceImpl: ErrorCatalogService

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
