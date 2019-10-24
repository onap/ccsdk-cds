package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.metrics

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service.health.AbstractHealthCheck
import org.springframework.stereotype.Service

@Service
open class SDCListenerMetricsHealthCheck : AbstractHealthCheck() {

    override fun setupServiceEndpoint(): List<ServiceEndpoint> {
        return listOf(
                ServiceEndpoint("SDCListenerMetrics", "http://cds-sdc-listener:8080/actuator/metrics")
        )
    }


}
