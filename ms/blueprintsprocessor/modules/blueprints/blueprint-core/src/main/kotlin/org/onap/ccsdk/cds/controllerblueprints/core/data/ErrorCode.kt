/*
 * Copyright © 2018-2019 Bell Canada Intellectual Property.
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
package org.onap.ccsdk.cds.controllerblueprints.core.data

import java.util.HashMap

/**
 * ErrorCode.java Purpose: Maintain a list of HTTP status codes
 *
 * @author Steve Siani
 * @version 1.0
 */
enum class ErrorCode(val value: Int, val httpCode: Int) {

    // / TODO: Add more attribute for each needed application protocol
    // TODO: Example: INVALID_FILE_EXTENSION(2, 415, 25)
    GENERIC_FAILURE(1, 500) {

        override fun message(detailMsg: String): String {
            return "Generic failure. Details : {$detailMsg}"
        }
    },
    INVALID_FILE_EXTENSION(2, 415) {

        override fun message(detailMsg: String): String {
            return "Unexpected file extension. Details : {$detailMsg}"
        }
    },
    BLUEPRINT_PATH_MISSING(3, 503) {

        override fun message(detailMsg: String): String {
            return "Blueprint path missing or wrong. Details : {$detailMsg}"
        }
    },
    BLUEPRINT_WRITING_FAIL(4, 503) {

        override fun message(detailMsg: String): String {
            return "Fail to write blueprint files. Details : {$detailMsg}"
        }
    },
    IO_FILE_INTERRUPT(5, 503) {

        override fun message(detailMsg: String): String {
            return "IO file system interruption. Details : {$detailMsg}"
        }
    },
    INVALID_REQUEST_FORMAT(6, 400) {

        override fun message(detailMsg: String): String {
            return "Bad request. Details : {$detailMsg}"
        }
    },
    UNAUTHORIZED_REQUEST(7, 401) {

        override fun message(detailMsg: String): String {
            return "The request requires user authentication. Details : {$detailMsg}"
        }
    },
    REQUEST_NOT_FOUND(8, 404) {

        override fun message(detailMsg: String): String {
            return "Request mapping doesn't exist. Details : {$detailMsg}"
        }
    },
    RESOURCE_NOT_FOUND(9, 404) {

        override fun message(detailMsg: String): String {
            return "No response was found for this request in the server. Details : {$detailMsg}"
        }
    },
    CONFLICT_ADDING_RESOURCE(10, 409) {

        override fun message(detailMsg: String): String {
            return "Duplicated entry while saving Blueprint. Details : {$detailMsg}"
        }
    },
    DUPLICATE_DATA(11, 409) {

        override fun message(detailMsg: String): String {
            return "Duplicated data - was expecting one result, got more than one. Details : {$detailMsg}"
        }
    };

    abstract fun message(detailMsg: String): String

    companion object {

        private val map = HashMap<Int, ErrorCode>()

        init {
            for (errorCode in ErrorCode.values()) {
                map[errorCode.value] = errorCode
            }
        }

        fun valueOf(value: Int): ErrorCode? {
            return if (map.containsKey(value)) map[value] else map[1]
        }
    }
}
