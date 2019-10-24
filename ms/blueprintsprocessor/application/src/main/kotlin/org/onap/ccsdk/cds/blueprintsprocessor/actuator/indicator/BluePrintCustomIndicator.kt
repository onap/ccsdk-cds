package org.onap.ccsdk.cds.blueprintsprocessor.actuator.indicator

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthApiResponse
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.BluePrintProcessorHealthCheck
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Component

/**
 * Health Indicator for BluePrintProcessor.
 * @author Shaaban Ebrahim
 * @version 1.0
 */
@Component
open class BluePrintCustomIndicator (private val bluePrintProcessorHealthCheck: BluePrintProcessorHealthCheck)
    : AbstractHealthIndicator() {

    private var result: HealthApiResponse? = null

    @Throws(Exception::class)
    override fun doHealthCheck(builder: Health.Builder) {
       // if (result == null)
        result = bluePrintProcessorHealthCheck!!.retrieveEndpointExecutionStatus()

        if (result?.status == HealthCheckStatus.UP) {
            builder.up()
        } else {
            builder.down()
        }
        builder.withDetail("Services", result?.checks)
    }


}
