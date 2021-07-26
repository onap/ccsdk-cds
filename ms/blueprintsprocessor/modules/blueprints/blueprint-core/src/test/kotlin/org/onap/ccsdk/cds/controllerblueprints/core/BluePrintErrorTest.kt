package org.onap.ccsdk.cds.controllerblueprints.core

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BluePrintErrorTest {

    @Test
    fun testBluePrintErrorIsCreatedWithemptyList() {
        val bluePrintError = BluePrintError()

        assertTrue(bluePrintError.allErrors().isEmpty())
    }

    @Test
    fun testAddErrorWith3Params() {
        val bluePrintError = BluePrintError()

        bluePrintError.addError("type", "name", "error", "step")

        assertEquals("type : name : error", bluePrintError.stepErrors("step")!![0])
    }

    @Test
    fun testAddErrorWith2Params() {
        val bluePrintError = BluePrintError()

        bluePrintError.addError("error", "step")

        assertEquals("error", bluePrintError.stepErrors("step")!![0])
    }
}
