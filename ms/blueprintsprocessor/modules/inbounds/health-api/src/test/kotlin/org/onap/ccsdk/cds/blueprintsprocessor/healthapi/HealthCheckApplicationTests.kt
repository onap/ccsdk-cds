package org.onap.ccsdk.cds.blueprintsprocessor.healthapi



import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintCoreConfiguration
import org.onap.ccsdk.cds.controllerblueprints.core.interfaces.BluePrintCatalogService
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.domain.ServiceEndpoint
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.SecurityProperties
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.client.RestTemplate
import java.util.*


@RunWith(SpringRunner::class)
@WebFluxTest
@ContextConfiguration(classes = [BluePrintCoreConfiguration::class,
    BluePrintCatalogService::class, SecurityProperties::class])
@ComponentScan(basePackages = ["org.onap.ccsdk.cds.blueprintsprocessor", "org.onap.ccsdk.cds.controllerblueprints"])
@TestPropertySource(locations = ["classpath:application-test.properties"])
class HealthCheckApplicationTests {


    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun testHealthApiUp() {
      val result =  webTestClient.get().uri("/api/v1/health-api/health")
                .exchange()
                .expectStatus().is2xxSuccessful
        println(result)
    }
}
