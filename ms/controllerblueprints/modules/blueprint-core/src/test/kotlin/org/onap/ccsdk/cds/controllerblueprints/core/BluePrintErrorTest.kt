package org.onap.ccsdk.cds.controllerblueprints.core

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BluePrintErrorTest {

    @Test
    fun testBluePrintErrorIsCreatedWithemptyList() {
        val bluePrintError = BluePrintError()

        assertTrue(bluePrintError.errors.isEmpty())
    }

    @Test
    fun testAddErrorWith3Params() {
        val bluePrintError = BluePrintError()

        bluePrintError.addError("type", "name", "error")

        assertEquals("type : name : error", bluePrintError.errors[0])
    }

    @Test
    fun testAddErrorWith1Params() {
        val bluePrintError = BluePrintError()

        bluePrintError.addError("error")

        assertEquals("error", bluePrintError.errors[0])
    }
}