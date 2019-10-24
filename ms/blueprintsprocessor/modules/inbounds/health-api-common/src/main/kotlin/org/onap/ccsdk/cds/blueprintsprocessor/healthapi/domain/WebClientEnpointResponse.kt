package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain

import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BlueprintWebClientService

data class WebClientEnpointResponse (val response:BlueprintWebClientService.WebClientResponse<String>?) {
}
