/*
 * Copyright © 2018-2019 AT&T Intellectual Property.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onap.ccsdk.cds.error.catalog.core

object ErrorCatalogCodes {

    const val GENERIC_FAILURE = "GENERIC_FAILURE"
    const val GENERIC_PROCESS_FAILURE = "GENERIC_PROCESS_FAILURE"
    const val INVALID_FILE_EXTENSION = "INVALID_FILE_EXTENSION"
    const val RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND"
    const val RESOURCE_PATH_MISSING = "RESOURCE_PATH_MISSING"
    const val RESOURCE_WRITING_FAIL = "RESOURCE_WRITING_FAIL"
    const val IO_FILE_INTERRUPT = "IO_FILE_INTERRUPT"
    const val INVALID_REQUEST_FORMAT = "INVALID_REQUEST_FORMAT"
    const val UNAUTHORIZED_REQUEST = "UNAUTHORIZED_REQUEST"
    const val REQUEST_NOT_FOUND = "REQUEST_NOT_FOUND"
    const val CONFLICT_ADDING_RESOURCE = "CONFLICT_ADDING_RESOURCE"
    const val DUPLICATE_DATA = "DUPLICATE_DATA"
}

object HttpErrorCodes {

    private val store: MutableMap<String, Int> = mutableMapOf()

    init {
        store[ErrorCatalogCodes.GENERIC_FAILURE] = 500
        store[ErrorCatalogCodes.GENERIC_PROCESS_FAILURE] = 500
        store[ErrorCatalogCodes.INVALID_FILE_EXTENSION] = 415
        store[ErrorCatalogCodes.RESOURCE_NOT_FOUND] = 404
        store[ErrorCatalogCodes.RESOURCE_PATH_MISSING] = 503
        store[ErrorCatalogCodes.RESOURCE_WRITING_FAIL] = 503
        store[ErrorCatalogCodes.IO_FILE_INTERRUPT] = 503
        store[ErrorCatalogCodes.INVALID_REQUEST_FORMAT] = 400
        store[ErrorCatalogCodes.UNAUTHORIZED_REQUEST] = 401
        store[ErrorCatalogCodes.REQUEST_NOT_FOUND] = 404
        store[ErrorCatalogCodes.CONFLICT_ADDING_RESOURCE] = 409
        store[ErrorCatalogCodes.DUPLICATE_DATA] = 409
    }

    fun register(type: String, code: Int) {
        store[type] = code
    }

    fun code(type: String): Int {
        return store[type] ?: store[ErrorCatalogCodes.GENERIC_FAILURE]!!
    }
}

object GrpcErrorCodes {

    private val store: MutableMap<String, Int> = mutableMapOf()

    init {
        store[ErrorCatalogCodes.GENERIC_FAILURE] = 2
        store[ErrorCatalogCodes.GENERIC_PROCESS_FAILURE] = 2
        store[ErrorCatalogCodes.INVALID_FILE_EXTENSION] = 3
        store[ErrorCatalogCodes.RESOURCE_NOT_FOUND] = 5
        store[ErrorCatalogCodes.RESOURCE_PATH_MISSING] = 3
        store[ErrorCatalogCodes.RESOURCE_WRITING_FAIL] = 9
        store[ErrorCatalogCodes.IO_FILE_INTERRUPT] = 3
        store[ErrorCatalogCodes.INVALID_REQUEST_FORMAT] = 3
        store[ErrorCatalogCodes.UNAUTHORIZED_REQUEST] = 16
        store[ErrorCatalogCodes.REQUEST_NOT_FOUND] = 8
        store[ErrorCatalogCodes.CONFLICT_ADDING_RESOURCE] = 10
        store[ErrorCatalogCodes.DUPLICATE_DATA] = 11
    }

    fun register(type: String, code: Int) {
        store[type] = code
    }

    fun code(type: String): Int {
        return store[type] ?: store[ErrorCatalogCodes.GENERIC_FAILURE]!!
    }
}
