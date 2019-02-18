/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
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

package org.onap.ccsdk.apps.blueprintsprocessor.rest

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties
open class BluePrintRestLibConfiguration


class RestLibConstants {
    companion object {
        const val SERVICE_BLUEPRINT_REST_LIB_PROPERTY = "blueprint-rest-lib-property-service"
        const val TYPE_BASIC_AUTH = "basic-auth"
        const val TYPE_SSL_BASIC_AUTH = "ssl-basic-auth"
        const val TYPE_DME2_PROXY = "dme2-proxy"
        const val TYPE_POLICY_MANAGER = "policy-manager"
    }
}