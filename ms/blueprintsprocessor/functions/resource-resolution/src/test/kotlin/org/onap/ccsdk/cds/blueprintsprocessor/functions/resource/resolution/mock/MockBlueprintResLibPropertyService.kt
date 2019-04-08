package org.onap.ccsdk.cds.blueprintsprocessor.functions.resource.resolution.mock

import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.RestClientProperties
import org.onap.ccsdk.cds.blueprintsprocessor.rest.service.BluePrintRestLibPropertyService

class MockBluePrintRestLibPropertyService(bluePrintProperties: BluePrintProperties) :
        BluePrintRestLibPropertyService(bluePrintProperties) {

    fun mockBlueprintWebClientService (selector: String):
            MockBlueprintWebClientService {
        val prefix = "blueprintsprocessor.restclient.$selector"
        val restClientProperties = restClientProperties(prefix)
        return mockBlueprintWebClientService(restClientProperties)
    }

    private fun mockBlueprintWebClientService(restClientProperties: RestClientProperties):
            MockBlueprintWebClientService {
        val blueprintWebClientService = MockBlueprintWebClientService(restClientProperties)
        blueprintWebClientService.host(restClientProperties.url)
        return blueprintWebClientService
    }
}