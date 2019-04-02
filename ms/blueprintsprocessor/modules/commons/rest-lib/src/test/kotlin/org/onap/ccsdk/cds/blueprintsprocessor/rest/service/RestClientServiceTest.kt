/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * Copyright (C) 2019 Nordix Foundation
 * Modifications Copyright © 2019 Huawei.
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

package org.onap.ccsdk.cds.blueprintsprocessor.rest.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.apache.catalina.connector.Connector
import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.core.BluePrintProperties
import org.onap.ccsdk.cds.blueprintsprocessor.core.BlueprintPropertyConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.rest.BluePrintRestLibConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory
import org.springframework.boot.web.servlet.server.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@EnableAutoConfiguration(exclude = [DataSourceAutoConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = [BluePrintRestLibConfiguration::class,
    BlueprintPropertyConfiguration::class,
    SampleController::class, BluePrintProperties::class,
    BluePrintProperties::class])
@TestPropertySource(properties =
[
    "server.port=8443",
    "server.ssl.enabled=true",
    "server.ssl.key-store=classpath:keystore.p12",
    "server.ssl.key-store-password=changeit",
    "server.ssl.keyStoreType=PKCS12",
    "server.ssl.keyAlias=tomcat",
    "blueprintsprocessor.restclient.sample.type=basic-auth",
    "blueprintsprocessor.restclient.sample.url=http://127.0.0.1:8080",
    "blueprintsprocessor.restclient.sample.username=admin",
    "blueprintsprocessor.restclient.sample.password=jans",
    "blueprintsprocessor.restclient.test.type=ssl-basic-auth",
    "blueprintsprocessor.restclient.test.url=https://localhost:8443",
    "blueprintsprocessor.restclient.test.username=admin",
    "blueprintsprocessor.restclient.test.password=jans",
    "blueprintsprocessor.restclient.test.keyStoreInstance=PKCS12",
    "blueprintsprocessor.restclient.test.sslTrust=src/test/resources/keystore.p12",
    "blueprintsprocessor.restclient.test.sslTrustPassword=changeit"
])
class RestClientServiceTest {

    @Autowired
    lateinit var bluePrintRestLibPropertyService: BluePrintRestLibPropertyService

    @Test
    fun testPatch() {
        val restClientService = bluePrintRestLibPropertyService
                .blueprintWebClientService("sample")
        val response = restClientService.exchangeResource(
                HttpMethod.PATCH.name, "/sample/name", "")
        assertEquals("Patch request successful", response,
                "failed to get patch response")
    }

    @Test
    fun testBaseAuth() {
        val restClientService = bluePrintRestLibPropertyService
                .blueprintWebClientService("sample")
        val headers = mutableMapOf<String, String>()
        headers["X-Transaction-Id"] = "1234"
        val response = restClientService.exchangeResource(HttpMethod.GET.name,
                "/sample/name", "")
        assertNotNull(response, "failed to get response")
    }

    @Test
    fun testSimpleBasicAuth() {
        val json: String = "{\n" +
                "  \"type\" : \"basic-auth\",\n" +
                "  \"url\" : \"http://localhost:8080\",\n" +
                "  \"username\" : \"admin\",\n" +
                "  \"password\" : \"jans\"\n" +
                "}"
        val mapper = ObjectMapper()
        val actualObj: JsonNode = mapper.readTree(json)
        val restClientService = bluePrintRestLibPropertyService
                .blueprintWebClientService(actualObj)
        lateinit var res:String
        runBlocking {
            val get = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.GET.name,
                        "/sample/basic", "")}
            get.start()
            res = get.await()
        }
        assertNotNull(res, "failed to get response")
        assertEquals(res, "Basic request arrived successfully")
    }

    @Test
    fun testSampleAaiReq() {
        val restClientService = bluePrintRestLibPropertyService
                .blueprintWebClientService("test")
        val headers = mutableMapOf<String, String>()
        headers["X-TransactionId"] = "9999"
        headers["X-FromAppId"] = "AAI"
        val post1 = "{\n" +
                "  \"customer\": {\n" +
                "    \"global-customer-id\": \"ONSDEMOBJHKCustomer\",\n" +
                "    \"subscriber-name\": \"ONSDEMOBJHKCustomer\",\n" +
                "    \"subscriber-type\": \"CUST\",\n" +
                "    \"resource-version\": \"1552985011163\"\n" +
                "  }\n" +
                "}"
        lateinit var res1: Customer
        lateinit var res2: Customer
        lateinit var res3: String
        lateinit var res4: String
        lateinit var res5: String
        lateinit var res6: String
        runBlocking {
            val get1 = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.GET.name,
                        "/sample/aai/v14/business/customers", "", headers,
                        Customer::class.java)}

            val get2 = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.GET.name,
                        "/sample/aai/v14/business/customers", "", headers,
                        Customer::class.java)}

            val post = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.POST.name,
                        "/sample/aai/v14/business/customers", post1, headers,
                        String::class.java)}

            val put = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.PUT.name,
                        "/sample/aai/v14/business/customers", post1, headers,
                        String::class.java)}

            val patch = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.PATCH.name,
                        "/sample/aai/v14/business/customers", post1, headers,
                        String::class.java)}

            val delete = async(start = CoroutineStart.LAZY) {
                restClientService.exchangeNB(HttpMethod.DELETE.name,
                        "/sample/aai/v14/business/customers", "", headers,
                        String::class.java)}

            get1.start()
            get2.start()
            post.start()
            put.start()
            patch.start()
            delete.start()
            res1 = get1.await()
            res2 = get2.await()
            res3 = post.await()
            res4 = put.await()
            res5 = patch.await()
            res6 = delete.await()
        }
        assertNotNull(res1, "failed to get response")
        assertNotNull(res2, "failed to get response")
        assertEquals(res1.id, "ONSDEMOBJHKCustomer")
        assertEquals(res1.name, "ONSDEMOBJHKCustomer")
        assertEquals(res1.type, "CUST")
        assertEquals(res1.resource, "1552985011163")
        assertEquals(res2.id, "ONSDEMOBJHKCustomer")
        assertEquals(res2.name, "ONSDEMOBJHKCustomer")
        assertEquals(res2.type, "CUST")
        assertEquals(res2.resource, "1552985011163")
        assertEquals(res3, "The message is successfully posted")
        assertEquals(res4, "The put request is success")
        assertEquals(res5, "The patch request is success")
        assertEquals(res6, "The message is successfully deleted")
    }
}

/**
 * Sample controller code for testing both http and https requests.
 */
@RestController
@RequestMapping("/sample")
open class SampleController {

    @GetMapping("/name")
    fun getName(): String = "Sample Controller"

    @PatchMapping("/name")
    fun patchName(): String = "Patch request successful"

    @GetMapping("/basic")
    fun getBasic(): String = "Basic request arrived successfully"


    @GetMapping("/aai/v14/business/customers")
    fun getAaiCustomers(
            @RequestHeader(name = "X-TransactionId", required = true)
            transId: String,
            @RequestHeader(name = "X-FromAppId", required = true)
            appId: String) : String {
        if (transId != "9999" || appId != "AAI") {
            return ""
        }
        return "{\n" +
                "  \"id\": \"ONSDEMOBJHKCustomer\",\n" +
                "  \"name\": \"ONSDEMOBJHKCustomer\",\n" +
                "  \"type\": \"CUST\",\n" +
                "  \"resource\": \"1552985011163\"\n" +
                "}"
    }

    @PostMapping("/aai/v14/business/customers")
    fun postAaiCustomers(
            @RequestHeader(name = "X-TransactionId", required = true)
            transId: String,
            @RequestHeader(name = "X-FromAppId", required = true)
            appId: String) : String {
        if (transId != "9999" || appId != "AAI") {
            return ""
        }
        return "The message is successfully posted"
    }


    @PutMapping("/aai/v14/business/customers")
    fun putAaiCustomers(
            @RequestHeader(name = "X-TransactionId", required = true)
            transId: String,
            @RequestHeader(name = "X-FromAppId", required = true)
            appId: String) : String {
        if (transId != "9999" || appId != "AAI") {
            return ""
        }
        return "The put request is success"
    }

    @PatchMapping("/aai/v14/business/customers")
    fun patchAaiCustomers(
            @RequestHeader(name = "X-TransactionId", required = true)
            transId: String,
            @RequestHeader(name = "X-FromAppId", required = true)
            appId: String) : String {
        if (transId != "9999" || appId != "AAI") {
            return ""
        }
        return "The patch request is success"
    }

    @DeleteMapping("/aai/v14/business/customers")
    fun deleteAaiCustomers(
            @RequestHeader(name = "X-TransactionId", required = true)
            transId: String,
            @RequestHeader(name = "X-FromAppId", required = true)
            appId: String) : String {
        if (transId != "9999" || appId != "AAI") {
            return ""
        }
        return "The message is successfully deleted"
    }
}

/**
 * Security configuration required for basic authentication with username and
 * password for any request in the server.
 */
@Configuration
@EnableWebSecurity
open class SecurityConfig : WebSecurityConfigurerAdapter() {

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http
                .csrf().disable()
                .authorizeRequests().anyRequest().authenticated()
                .and()
                .httpBasic()
    }

    @Autowired
    @Throws(Exception::class)
    open fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication()
                .withUser("admin")
                .password(passwordEncoder().encode("jans"))
                .roles("USER")
    }

    @Bean
    open fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}

/**
 * Http server required for http request to be processed along with the https
 * server.
 */
@Component
class HttpServer {
    @Bean
    fun servletContainer(): ServletWebServerFactory {

        val connector = Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL)
        connector.setPort(8080)

        val tomcat = TomcatServletWebServerFactory()
        tomcat.addAdditionalTomcatConnectors(connector)
        return tomcat
    }
}

/**
 * Data class required for response
 */
data class Customer(
       val id: String,
       val name: String,
       val type: String,
       val resource: String)