package org.onap.ccsdk.cds.blueprintsprocessor.uat.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonMatcherTest {
    @Test
    fun `matches easy case`() {
        val expected = """
         {
            "a": "b"
         }   
        """.trimIndent()
        val actual = """
         {
            "a": "b"
         }   
        """.trimIndent()
        assertTrue(JsonMatcher(expected).matches(actual))
    }

    @Test
    fun `matches fails easy case`() {
        val expected = """
         {
            "a": "b"
         }   
        """.trimIndent()
        val actual = """
         {
            "a": "c"
         }   
        """.trimIndent()
        assertFalse(JsonMatcher(expected).matches(actual))
    }

    @Test
    fun `matches easy case (actual is lenient aka Extensible)`() {
        val expected = """
         {
            "a": "b"
         }   
        """.trimIndent()
        val actual = """
         {
            "a": "b",
            "c": "d"
         }   
        """.trimIndent()
        assertTrue(JsonMatcher(expected).matches(actual))

        assertFalse(JsonMatcher(actual).matches(expected))
    }

    @Test
    fun `matches null`() {
        assertTrue(JsonMatcher(null).matches(null))
    }

    @Test
    fun `matches null and "null"`() {
        val expected: String? = null
        val actual: String? = null
        assertTrue(JsonMatcher("null").matches(null))
    }

    @Test
    fun `matches ""`() {
        val expected: String? = null
        val actual: String? = null
        assertTrue(JsonMatcher("").matches(""))
    }
}
