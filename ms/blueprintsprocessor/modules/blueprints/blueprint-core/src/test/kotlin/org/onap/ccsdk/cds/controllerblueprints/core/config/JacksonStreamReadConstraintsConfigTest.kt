package org.onap.ccsdk.cds.controllerblueprints.core.config

import com.fasterxml.jackson.core.StreamReadConstraints
import com.fasterxml.jackson.core.exc.StreamConstraintsException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Test
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

import kotlin.test.assertEquals

class JacksonStreamReadConstraintsConfigTest {

    private companion object {
        const val MAX_STRING_LENGTH = 100
    }

    @AfterEach
    fun resetStreamReadConstraints() {
        StreamReadConstraints.overrideDefaultStreamReadConstraints(
            StreamReadConstraints.defaults()
        )
    }

    @Test
    fun `should apply maxStringLength globally`() {
        JacksonStreamReadConstraintsConfig(
            JacksonStreamReadConstraintsProperties(
                maxStringLength = MAX_STRING_LENGTH
            )
        ).configureJackson()

        val mapper = ObjectMapper()

        assertEquals(
            MAX_STRING_LENGTH,
            mapper.factory.streamReadConstraints().maxStringLength
        )
    }

    @Test
    fun `should allow string within configured limit`() {
        JacksonStreamReadConstraintsConfig(
            JacksonStreamReadConstraintsProperties(
                maxStringLength = MAX_STRING_LENGTH
            )
        ).configureJackson()

        val mapper = ObjectMapper()

        val json = """{"value":"${"a".repeat(MAX_STRING_LENGTH - 1)}"}"""
        assertDoesNotThrow {
            mapper.readValue(json, JsonNode::class.java)
        }
    }

    @Test
    fun `should reject string exceeding configured limit`() {
        JacksonStreamReadConstraintsConfig(
            JacksonStreamReadConstraintsProperties(
                maxStringLength = MAX_STRING_LENGTH
            )
        ).configureJackson()

        val mapper = ObjectMapper()

        val json = """{"value":"${"a".repeat(MAX_STRING_LENGTH + 1)}"}"""
        assertThrows<StreamConstraintsException> {
            mapper.readValue(json, JsonNode::class.java)
        }
    }
}
