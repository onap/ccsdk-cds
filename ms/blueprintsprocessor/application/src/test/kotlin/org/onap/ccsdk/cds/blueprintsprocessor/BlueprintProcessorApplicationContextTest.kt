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

package org.onap.ccsdk.cds.blueprintsprocessor

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.TestSecuritySettings
import org.onap.ccsdk.cds.blueprintsprocessor.uat.utils.WorkingFoldersInitializer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit4.SpringRunner

/**
 * Smoke test: verifies that the Spring application context starts up without errors.
 *
 * Among other things this catches missing runtime dependencies that would only manifest at
 * startup (e.g. a ClassNotFoundException for a class required by Hibernate / JPA during
 * EntityManagerFactory initialisation).
 *
 * The test is intentionally kept as lightweight as possible – it does not exercise any business
 * logic.  Its sole purpose is to fail fast when the assembled JAR is broken in a way that
 * prevents the application from starting.
 */
@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(
    initializers = [
        WorkingFoldersInitializer::class,
        TestSecuritySettings.ServerContextInitializer::class
    ]
)
@TestPropertySource(locations = ["classpath:application-test.properties"])
class BlueprintProcessorApplicationContextTest {

    /**
     * The fact that this (empty) test method is reached means the Spring ApplicationContext
     * was created successfully – all beans could be instantiated and the application is ready
     * to serve requests.
     *
     * Regression guard: prior to the fix that replaced javax.xml.bind:jaxb-api with
     * jakarta.xml.bind:jakarta.xml.bind-api, this test would fail with:
     *
     *   BeanCreationException … jakarta/xml/bind/JAXBException
     *   Caused by: java.lang.NoClassDefFoundError: jakarta/xml/bind/JAXBException
     *
     * because Hibernate 6.x references jakarta.xml.bind.* which lives in a different
     * artifact from the old javax.xml.bind:jaxb-api jar.
     */
    @Test
    fun `application context loads successfully`() {
        // no assertions needed – reaching this point means the context started without error
    }
}
