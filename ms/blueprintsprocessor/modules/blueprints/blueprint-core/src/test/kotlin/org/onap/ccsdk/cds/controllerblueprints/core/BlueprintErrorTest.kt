package org.onap.ccsdk.cds.controllerblueprints.core

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BlueprintErrorTest {

    @Test
    fun testBlueprintErrorIsCreatedWithemptyList() {
        val bluePrintError = BlueprintError()

        assertTrue(bluePrintError.errors.isEmpty())
    }

    @Test
    fun testAddErrorWith3Params() {
        val bluePrintError = BlueprintError()

        bluePrintError.addError("type", "name", "error")

        assertEquals("type : name : error", bluePrintError.errors[0])
    }

    @Test
    fun testAddErrorWith1Params() {
        val bluePrintError = BlueprintError()

        bluePrintError.addError("error")

        assertEquals("error", bluePrintError.errors[0])
    }
}
