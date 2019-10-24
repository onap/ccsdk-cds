package org.onap.ccsdk.cds.blueprintsprocessor.actuator.indicator

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.HealthCheckStatus

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.SDCListenerHealthCheck
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.health.AbstractHealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.stereotype.Component

@Component
open class BluePrintCustomIndicator : AbstractHealthIndicator() {

    @Autowired
    private val sDCListenerHealthCheck: SDCListenerHealthCheck? = null

    @Throws(Exception::class)
    override fun doHealthCheck(builder: Health.Builder) {
        val (status, checks) = sDCListenerHealthCheck!!.retrieveEndpointExecutionStatus()
        if (status == HealthCheckStatus.UP) {
            builder.up()
        } else {
            builder.down()
        }
        builder.withDetail("Services", checks)
    }
}
