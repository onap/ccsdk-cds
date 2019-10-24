package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.service

import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.MetricsInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
open  class CombinedMetricsService {


    @Autowired
    private val metricsService: MetricsService? = null

    open fun getMetricsInfo(): MetricsInfo? {
       return metricsService?.metricsInfo
    }


}
