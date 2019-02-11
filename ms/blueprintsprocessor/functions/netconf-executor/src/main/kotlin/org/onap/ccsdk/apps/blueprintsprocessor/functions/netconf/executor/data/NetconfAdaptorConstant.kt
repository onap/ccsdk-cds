/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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
package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor.data

class NetconfAdaptorConstant {
    companion object{
        const val STATUS_CODE_SUCCESS = "200"
        const val STATUS_CODE_FAILURE = "400"

        const val STATUS_SUCCESS = "success"
        const val STATUS_FAILURE = "failure"
        const val STATUS_SKIPPED = "skipped"
        const val LOG_MESSAGE_TYPE_LOG = "Log"

        const val CONFIG_TARGET_RUNNING = "running"
        const val CONFIG_TARGET_CANDIDATE = "candidate"
        const val CONFIG_DEFAULT_OPERATION_MERGE = "merge"
        const val CONFIG_DEFAULT_OPERATION_REPLACE = "replace"

        const val DEFAULT_NETCONF_SESSION_MANAGER_TYPE = "DEFAULT_NETCONF_SESSION"

        const val CONFIG_STATUS_PENDING = "pending"
        const val CONFIG_STATUS_FAILED = "failed"
        const val CONFIG_STATUS_SUCCESS = "success"

        const val DEFAULT_MESSAGE_TIME_OUT = 30


    }
}