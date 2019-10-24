package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain


data class HealthApiResponse(val status: HealthCheckStatus, val checks: List<ServicesCheckResponse>
)


