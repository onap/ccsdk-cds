/*
 * Copyright © 2019-2020 Orange.
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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.HealthCheckProperties
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.EndPointExecution
import org.springframework.stereotype.Service

/**
 * Service for checking health for other CDS services .
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Service
open class BlueprintProcessorHealthCheck(
    private val endPointExecution: EndPointExecution,
    private val healthCheckProperties: HealthCheckProperties
) :
    AbstractHealthCheck(endPointExecution) {

    override fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return healthCheckProperties.getBlueprintServiceInformation()
    }
}
