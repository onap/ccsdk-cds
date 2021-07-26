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

package org.onap.ccsdk.cds.blueprintsprocessor.nats

import org.onap.ccsdk.cds.blueprintsprocessor.nats.service.BluePrintNatsLibPropertyService
import org.onap.ccsdk.cds.blueprintsprocessor.nats.service.BluePrintNatsService
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintDependencyService
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan
open class BluePrintNatsLibConfiguration

/**
 * Exposed Dependency Service by this NATS Lib Module
 */
fun BluePrintDependencyService.natsLibPropertyService(): BluePrintNatsLibPropertyService =
    instance(NatsLibConstants.SERVICE_BLUEPRINT_NATS_LIB_PROPERTY)

fun BluePrintDependencyService.controllerNatsService(): BluePrintNatsService {
    return natsLibPropertyService().bluePrintNatsService(NatsLibConstants.DEFULT_NATS_SELECTOR)
}

class NatsLibConstants {
    companion object {

        const val SERVICE_BLUEPRINT_NATS_LIB_PROPERTY = "blueprint-nats-lib-property-service"
        const val DEFULT_NATS_SELECTOR = "cds-controller"
        const val PROPERTY_NATS_PREFIX = "blueprintsprocessor.nats."
        const val PROPERTY_NATS_CLUSTER_ID = "NATS_CLUSTER_ID"
        const val TYPE_TOKEN_AUTH = "token-auth"
        const val TYPE_TLS_AUTH = "tls-auth"
    }
}
