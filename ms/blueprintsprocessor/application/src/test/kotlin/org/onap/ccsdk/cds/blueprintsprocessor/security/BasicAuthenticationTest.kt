/*
 * Copyright © 2026 Deutsche Telekom AG.
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

package org.onap.ccsdk.cds.blueprintsprocessor.security

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.WebConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.reactive.config.EnableWebFlux
import java.util.Base64

@RunWith(SpringRunner::class)
@ContextConfiguration(
    classes = [
        WebConfig::class,
        SecurityConfiguration::class,
        AuthenticationManager::class,
        SecurityContextRepository::class,
        BasicAuthenticationTest.TestWebConfiguration::class
    ]
)
@TestPropertySource(properties = ["security.user.name=walter.white", "security.user.password={noop}Heisenberg"])
class BasicAuthenticationTest {

    @Autowired
    lateinit var context: ApplicationContext

    private lateinit var webTestClient: WebTestClient

    @Before
    fun setup() {
        webTestClient = WebTestClient.bindToApplicationContext(context).build()
    }

    @Test
    fun testValidCredentialsAreAccepted() {
        webTestClient.get().uri("/ping")
            .header(HttpHeaders.AUTHORIZATION, basicAuth("walter.white:Heisenberg"))
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun testMissingCredentialsAreRejectedWith401() {
        webTestClient.get().uri("/ping")
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun testWrongCredentialsAreRejectedWith401() {
        webTestClient.get().uri("/ping")
            .header(HttpHeaders.AUTHORIZATION, basicAuth("walter.white:Pinkman"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun testMalformedCredentialsAreRejectedWith401() {
        webTestClient.get().uri("/ping")
            .header(HttpHeaders.AUTHORIZATION, basicAuth("walter.white"))
            .exchange()
            .expectStatus().isUnauthorized
    }

    private fun basicAuth(credentials: String) =
        "Basic " + Base64.getEncoder().encodeToString(credentials.toByteArray())

    @Configuration
    @EnableWebFlux
    @EnableWebFluxSecurity
    open class TestWebConfiguration {

        @Bean
        open fun pingController() = PingController()
    }

    @RestController
    class PingController {

        @GetMapping("/ping")
        fun ping() = "pong"
    }
}
