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

package org.onap.ccsdk.cds.sdclistener.actuator.indicator;

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus;
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.SDCListenerHealthCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health Indicator for SDCListener.
 *
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Component
public class SDCListenerCustomIndicator implements HealthIndicator {

    @Autowired
    private SDCListenerHealthCheck sDCListenerHealthCheck;

    @Override
    public Health health() {
        HealthApiResponse healthAPIResponse = sDCListenerHealthCheck.retrieveEndpointExecutionStatus();
        if (healthAPIResponse.getStatus() == HealthCheckStatus.UP) {

            return Health.up().withDetail("Services", healthAPIResponse.getChecks()).build();
        }
        return Health.down().build();
    }
}
