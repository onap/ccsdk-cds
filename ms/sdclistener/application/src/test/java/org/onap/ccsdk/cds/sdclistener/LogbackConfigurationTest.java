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

package org.onap.ccsdk.cds.sdclistener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.Test;

public class LogbackConfigurationTest {

    private LoggerContext configure(String logFormat) throws JoranException {
        LoggerContext context = new LoggerContext();
        if (logFormat != null) {
            context.putProperty("CDS_LOG_FORMAT", logFormat);
        }
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        configurator.doConfigure(getClass().getClassLoader().getResource("logback.xml"));
        List<Status> errors = context.getStatusManager().getCopyOfStatusList().stream()
                .filter(status -> status.getLevel() == Status.ERROR).collect(Collectors.toList());
        assertTrue("logback.xml reported errors: " + errors, errors.isEmpty());
        return context;
    }

    @Test
    public void plainTextConsoleLoggingIsTheDefault() throws JoranException {
        Logger root = configure(null).getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

        ConsoleAppender<?> appender = (ConsoleAppender<?>) root.getAppender("text");
        assertTrue(appender.getEncoder() instanceof LayoutWrappingEncoder);
        assertNull(root.getAppender("json"));
    }

    @Test
    public void anyOtherLogFormatFallsBackToPlainText() throws JoranException {
        for (String logFormat : List.of("", "text", "plain", "JSON")) {
            Logger root = configure(logFormat).getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);

            ConsoleAppender<?> appender = (ConsoleAppender<?>) root.getAppender("text");
            assertTrue("CDS_LOG_FORMAT=" + logFormat, appender.getEncoder() instanceof LayoutWrappingEncoder);
            assertNull("CDS_LOG_FORMAT=" + logFormat, root.getAppender("json"));
        }
    }

    @Test
    public void jsonConsoleLoggingIncludesTheMdc() throws JoranException, IOException {
        Logger root = configure("json").getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        assertNull(root.getAppender("text"));
        LogstashEncoder encoder = (LogstashEncoder) ((ConsoleAppender<?>) root.getAppender("json")).getEncoder();

        LoggingEvent event = new LoggingEvent("test", root, Level.INFO, "hello", null, null);
        event.setMDCPropertyMap(Map.of("trace_id", "4bf92f3577b34da6a3ce929d0e0e4736"));
        JsonNode json = new ObjectMapper().readTree(encoder.encode(event));

        assertEquals("hello", json.get("message").asText());
        assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", json.get("trace_id").asText());
    }
}
