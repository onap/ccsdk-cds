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

package org.onap.ccsdk.cds.blueprintsprocessor.healthapi

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.BasicAuthRestClientServiceConfiguration
import org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration.SecurityEncryptionConfiguration
import org.slf4j.LoggerFactory
import org.springframework.test.util.ReflectionTestUtils

class BasicAuthRestClientServiceConfigurationTest {

    private val gcmCcsdkapps = "ZW5jcnlwdGlvbkluKku+TuXv7+dHH2yFXUTPM6N1Ui0p00OEkQ=="
    private val cbcCcsdkapps = "eHbVUbJAj4AG2522cSbrOQ=="

    private val logger = LoggerFactory.getLogger(BasicAuthRestClientServiceConfiguration::class.java) as Logger
    private val appender = ListAppender<ILoggingEvent>()

    @Before
    fun setup() {
        appender.start()
        logger.addAppender(appender)
    }

    @After
    fun tearDown() {
        logger.detachAppender(appender)
    }

    @Test
    fun testCredentialsAreDecryptedAndTimeoutsAreFinite() {
        val properties = configuration(gcmCcsdkapps, gcmCcsdkapps).getBasicAuthRestClientProperties()

        assertEquals("ccsdkapps", properties.username)
        assertEquals("ccsdkapps", properties.password)
        assertEquals("", properties.url)
        assertTrue(properties.connectTimeout > 0)
        assertTrue(properties.socketTimeout > 0)
        assertTrue(appender.list.none { it.level == Level.ERROR })
    }

    @Test
    fun testUndecryptableCredentialIsLoggedWithPropertyName() {
        val properties = configuration(gcmCcsdkapps, cbcCcsdkapps).getBasicAuthRestClientProperties()

        assertEquals("ccsdkapps", properties.username)
        assertEquals("", properties.password)
        val errors = appender.list.filter { it.level == Level.ERROR }
        assertEquals(1, errors.size)
        assertTrue(errors[0].formattedMessage.contains("endpoints.user.password"))
        assertTrue(errors[0].formattedMessage.contains("1.11.0"))
    }

    private fun configuration(username: String, password: String): BasicAuthRestClientServiceConfiguration {
        val configuration = BasicAuthRestClientServiceConfiguration(SecurityEncryptionConfiguration())
        ReflectionTestUtils.setField(configuration, "username", username)
        ReflectionTestUtils.setField(configuration, "password", password)
        return configuration
    }
}
