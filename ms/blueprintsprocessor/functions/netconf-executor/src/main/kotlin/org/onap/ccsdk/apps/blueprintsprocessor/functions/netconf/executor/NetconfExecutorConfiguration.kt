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

package org.onap.ccsdk.apps.blueprintsprocessor.functions.netconf.executor

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties
open class NetconfExecutorConfiguration


class NetconfExecutorConstants {
    companion object {
        const val CONTEX_PARAM_MESSAGE = "message"
        const val COMPONENT_SCRIPT_PATH = "component-scripts"

        const val REQ_NETCONF_CONNECTION = "netconf-connection"
        const val NETCONF_CONNECTION_SOURCE = "source"
        const val NETCONF_CONNECTION_LOGIN_KEY = "login-key"
        const val NETCONF_CONNECTION_LOGIN_ACCOUNT = "login-account"
        const val NETCONF_CONNECTION_TARGET_IP = "target-ip-address"
        const val NETCONF_CONNECTION_MESSAGE_PORT = "port-number"
        const val NETCONF_CONNECTION_TIMEOUT = "connection-time-out"

        const val INPUT_PARAM_REQUEST_ID = "request-id"
        const val INPUT_PARAM_RESOURCE_ID = "resource-id"
        const val INPUT_PARAM_RESERVATION_ID = "reservation-id"
        const val INPUT_PARAM_RESOURCE_TYPE = "resource-type"
        const val INPUT_PARAM_ACTION_NAME = "action-name"
        const val INPUT_PARAM_TEMPLATE_NAME = "template-name"
        const val INPUT_PARAM_ASSIGNMENT_ACTION_NAME = "assignment-action-name"

        const val SCRIPT_OUTPUT_RESPONSE_DATA = "responseData"
        const val SCRIPT_OUTPUT_ERROR_MESSAGE = "errorMessage"

        const val OUTPUT_PARAM_RESPONSE_DATA = "response-data"
        const val OUTPUT_PARAM_ERROR_MESSAGE = "error-message"
        const val OUTPUT_PARAM_STATUS = "status"
        const val OUTPUT_STATUS_SUCCESS = "success"
        const val OUTPUT_STATUS_FAILURE = "failure"

        const val CONFIG_DATA_TYPE_XML = "XML"
        const val CONFIG_DATA_TYPE_JSON = "JSON"

        const val CONFIG_TARGET_RUNNING = "running"
        const val CONFIG_TARGET_CANDIDATE = "candidate"
        const val CONFIG_DEFAULT_OPERATION_MERGE = "merge"
        const val CONFIG_DEFAULT_OPERATION_REPLACE = "replace"
        const val DEFAULT_NETCONF_SESSION_MANAGER_TYPE = "DEFAULT_NETCONF_SESSION"
    }
}