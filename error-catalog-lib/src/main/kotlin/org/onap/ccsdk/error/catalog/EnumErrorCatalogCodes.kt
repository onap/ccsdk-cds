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

package org.onap.ccsdk.error.catalog

import org.onap.ccsdk.error.catalog.interfaces.EnumErrorCatalogInterface

/**
 * EnumErrorCodesManager Purpose: Maintain a list of generic error code application may need
 *
 * @author Steve Siani
 * @version 1.0
 */
enum class EnumErrorCatalogCodes(
    var domain: String = "root",
    var httpCode: Int,
    var grpcCode: Int
) : EnumErrorCatalogInterface {

    GENERIC_FAILURE(httpCode = 500, grpcCode = 2) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    GENERIC_PROCESS_FAILURE(httpCode = 500, grpcCode = 2) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    INVALID_FILE_EXTENSION(httpCode = 415, grpcCode = 3) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    RESOURCE_NOT_FOUND(httpCode = 404, grpcCode = 5) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    RESOURCE_PATH_MISSING(httpCode = 503, grpcCode = 3) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    RESOURCE_WRITING_FAIL(httpCode = 503, grpcCode = 9) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    IO_FILE_INTERRUPT(httpCode = 503, grpcCode = 3) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    INVALID_REQUEST_FORMAT(httpCode = 400, grpcCode = 3) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    UNAUTHORIZED_REQUEST(httpCode = 401, grpcCode = 16) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    REQUEST_NOT_FOUND(httpCode = 404, grpcCode = 8) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    CONFLICT_ADDING_RESOURCE(httpCode = 409, grpcCode = 10) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    },
    DUPLICATE_DATA(httpCode = 409, grpcCode = 11) {
        override fun getErrorName(): String { return name }
        override fun getErrorDomain(): String { return domain }
        override fun getErrorHttpCode(): Int { return httpCode }
        override fun getErrorGrpcCode(): Int { return grpcCode }
    };

    companion object {
        private val map = HashMap<String, EnumErrorCatalogCodes>()
        private const val DEFAULT_ERROR_ID = "GENERIC_FAILURE"

        init {
            for (enumErrorCode in EnumErrorCatalogCodes.values()) {
                map[enumErrorCode.name] = enumErrorCode
            }
        }

        fun valueOf(value: String): EnumErrorCatalogCodes {
            return if (map.containsKey(value)) map[value]!! else map[DEFAULT_ERROR_ID]!!
        }
    }
}
