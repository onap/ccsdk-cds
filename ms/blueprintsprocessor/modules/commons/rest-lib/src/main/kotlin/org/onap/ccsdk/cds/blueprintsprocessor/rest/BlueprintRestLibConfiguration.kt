/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Modifications Copyright © 2019 IBM.
 * Modifications Copyright © 2019 Huawei.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest

import com.fasterxml.jackson.databind.JsonNode
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintRestLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BlueprintDependencyService
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
@EnableConfigurationProperties
open class BlueprintRestLibConfiguration

/**
 * Exposed Dependency Service by this Rest Lib Module
 */
fun BlueprintDependencyService.restLibPropertyService(): BlueprintRestLibPropertyService =
    instance(RestLibConstants.SERVICE_BLUEPRINT_REST_LIB_PROPERTY)

fun BlueprintDependencyService.restClientService(selector: String): BlueprintWebClientService {
    return restLibPropertyService().blueprintWebClientService(selector)
}

fun BlueprintDependencyService.restClientService(jsonNode: JsonNode): BlueprintWebClientService {
    return restLibPropertyService().blueprintWebClientService(jsonNode)
}

class RestLibConstants {
    companion object {

        const val SERVICE_BLUEPRINT_REST_LIB_PROPERTY = "blueprint-rest-lib-property-service"
        const val PROPERTY_REST_CLIENT_PREFIX = "blueprintsprocessor.restclient."
        const val PROPERTY_TYPE = "type"
        const val TYPE_TOKEN_AUTH = "token-auth"
        const val TYPE_BASIC_AUTH = "basic-auth"
        const val TYPE_SSL_BASIC_AUTH = "ssl-basic-auth"
        const val TYPE_SSL_TOKEN_AUTH = "ssl-token-auth"
        const val TYPE_SSL_NO_AUTH = "ssl-no-auth"
        const val TYPE_POLICY_MANAGER = "policy-manager"
    }
}
