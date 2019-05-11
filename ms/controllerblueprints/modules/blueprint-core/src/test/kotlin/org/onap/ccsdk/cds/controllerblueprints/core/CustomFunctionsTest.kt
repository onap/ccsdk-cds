/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
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

package org.onap.ccsdk.cds.controllerblueprints.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import org.junit.Test
import kotlin.test.*

/**
 *
 *
 * @author Brinda Santh
 */
class CustomFunctionsTest {
    @Test
    fun testFormat() {
        val returnValue : String = format("This is {} for times {}", "test", 2)
        assertEquals("This is test for times 2", returnValue, "Failed to format String")

        val returnValue1 : String = format("This is test for times 2")
        assertEquals("This is test for times 2", returnValue1, "Failed to format empty args")
    }

    @Test
    fun testStringAsJsonPrimitive() {
        val returnValue: TextNode = "hello".asJsonPrimitive()
        assertEquals("hello", returnValue.textValue())
    }

    @Test
    fun testIntAsJsonPrimitive() {
        val returnValue: IntNode = 1.asJsonPrimitive()
        assertEquals(1, returnValue.intValue())
    }

    @Test
    fun testBooleanAsJsonPrimitive() {
        val returnValue: BooleanNode = false.asJsonPrimitive()
        assertFalse(returnValue.asBoolean())
    }

    @Test
    fun testAsJsonType() {
        val nullReturnValue: JsonNode = null.asJsonType()
        assertEquals(NullNode.instance, nullReturnValue)

        val returnValueString: JsonNode = "hello".asJsonType()
        assertEquals("hello", returnValueString.textValue())

        val returnValueJsonNode: JsonNode = returnValueString.asJsonType()
        assertEquals(returnValueString, returnValueJsonNode)

        val returnValueInt: JsonNode = 1.asJsonType()
        assertEquals(1, returnValueInt.intValue())

        val returnValueBool: JsonNode = false.asJsonType()
        assertFalse(returnValueBool.asBoolean())

        val returnValue: JsonNode = BluePrintError().asJsonType()
        assertEquals(JsonNodeType.OBJECT, returnValue.getNodeType())
    }

    @Test
    fun testMapAsObjectNode() {
        val returnValue: ObjectNode = hashMapOf("test" to BluePrintError()).asObjectNode()
        assertNotNull(returnValue.get("test"))
    }

    @Test
    fun testCastOptionalValue() {
        val initMap: Map<String, *> = hashMapOf("test" to 1.1, "test2" to null)
        val returnValue = initMap.castOptionalValue("test", Number::class)

        assert(returnValue is Number)

        val returnValueNull = initMap.castOptionalValue("test1", Number::class)

        assertNull(returnValueNull)

        val returnValueString: String? = initMap.castOptionalValue("test2", String::class)

        assertNull(returnValueString)
    }

    @Test(expected = BluePrintException::class)
    fun testCastValue() {
        val initMap: Map<String, Double> = hashMapOf("test" to 1.1)
        val returnValue = initMap.castValue("test", Number::class)

        assertNotNull(returnValue)

        initMap.castValue("test1", Number::class)
    }

    @Test
    fun testAsListOfString() {
        val arrayNode: ArrayNode = ObjectMapper().createObjectNode().putArray("array")

        val result: List<String> = arrayNode.asListOfString()

        assertTrue(result.isEmpty())
    }

    @Test
    fun testReturnNullIfMissing() {
        val valueExist = "hello".asJsonType().returnNullIfMissing()
        assertNotNull(valueExist)

        val valueNull = NullNode.instance.returnNullIfMissing()
        assertNull(valueNull)

        val missingValue = MissingNode.getInstance().returnNullIfMissing()
        assertNull(missingValue)
    }

    @Test(expected = BluePrintException::class)
    fun testRootFieldsToMap() {
        1.asJsonType().rootFieldsToMap()
    }

    @Test
    fun testPutJsonElement() {
        val mutMap = mutableMapOf("test" to 2.asJsonType())

        mutMap.putJsonElement("hello", 3)

        assertEquals(3, mutMap["hello"]?.asInt())
    }

    @Test(expected = BluePrintException::class)
    fun testMapGetAsString() {
        val initMap = hashMapOf("test" to "hello".asJsonType())

        assertEquals("hello", initMap.getAsString("test"))

        initMap.getAsString("test2")
    }

    @Test(expected = BluePrintException::class)
    fun testMapGetAsBoolean() {
        val initMap = hashMapOf("test" to true.asJsonType())

        assertTrue(initMap.getAsBoolean("test"))

        initMap.getAsBoolean("test2")
    }

    @Test(expected = BluePrintException::class)
    fun testMapGetAsInt() {
        val initMap = hashMapOf("test" to 1.asJsonType())

        assertEquals(1, initMap.getAsInt("test"))

        initMap.getAsInt("test2")
    }

    @Test(expected = BluePrintException::class)
    fun testCheckEquals() {
        assertTrue(checkEquals("hello", "hello", { -> "error"}))

        checkEquals("hello", "test", { -> "error"})
    }

    @Test(expected = IllegalStateException::class)
    fun testCheckNotEmpty() {
        assertEquals("hello", checkNotEmpty("hello", { -> "error"}))

        checkNotEmpty("", { -> "error"})
    }

    @Test(expected = IllegalStateException::class)
    fun testCheckNotBlank() {
        assertEquals("hello", checkNotBlank("hello", { -> "error"}))

        checkNotBlank("  ", { -> "error"})
    }

    @Test
    fun testNullToEmpty() {
        assertEquals("", nullToEmpty(null))

        assertEquals("hello", nullToEmpty("hello"))
    }
}