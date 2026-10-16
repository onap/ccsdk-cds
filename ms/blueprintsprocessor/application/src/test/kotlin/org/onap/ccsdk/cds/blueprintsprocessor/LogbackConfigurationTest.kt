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

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import ch.qos.logback.core.status.Status
import com.fasterxml.jackson.databind.ObjectMapper
import net.logstash.logback.encoder.LogstashEncoder
import org.junit.Test
import org.slf4j.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogbackConfigurationTest {

    private fun configure(logFormat: String?): LoggerContext {
        val context = LoggerContext()
        logFormat?.let { context.putProperty("CDS_LOG_FORMAT", it) }
        val configurator = JoranConfigurator()
        configurator.context = context
        configurator.doConfigure(javaClass.classLoader.getResource("logback.xml"))
        val errors = context.statusManager.copyOfStatusList.filter { it.level == Status.ERROR }
        assertTrue(errors.isEmpty(), "logback.xml reported errors: $errors")
        return context
    }

    @Test
    fun `plain text console logging is the default`() {
        val root = configure(null).getLogger(Logger.ROOT_LOGGER_NAME)

        val appender = root.getAppender("text") as ConsoleAppender
        assertTrue(appender.encoder is LayoutWrappingEncoder)
        assertNull(root.getAppender("json"))
    }

    @Test
    fun `any other log format falls back to plain text`() {
        listOf("", "text", "plain", "JSON").forEach { logFormat ->
            val root = configure(logFormat).getLogger(Logger.ROOT_LOGGER_NAME)

            val appender = root.getAppender("text") as ConsoleAppender
            assertTrue(appender.encoder is LayoutWrappingEncoder, "CDS_LOG_FORMAT=$logFormat")
            assertNull(root.getAppender("json"), "CDS_LOG_FORMAT=$logFormat")
        }
    }

    @Test
    fun `json console logging includes the MDC`() {
        val context = configure("json")
        val root = context.getLogger(Logger.ROOT_LOGGER_NAME)
        assertNull(root.getAppender("text"))
        val encoder = (root.getAppender("json") as ConsoleAppender).encoder as LogstashEncoder

        val event = LoggingEvent("test", root, Level.INFO, "hello", null, null)
        event.mdcPropertyMap = mapOf("RequestID" to "request-1", "trace_id" to "4bf92f3577b34da6a3ce929d0e0e4736")
        val json = ObjectMapper().readTree(encoder.encode(event))

        assertEquals("hello", json["message"].asText())
        assertEquals("request-1", json["RequestID"].asText())
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", json["trace_id"].asText())
    }
}
