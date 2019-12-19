package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BluePrintConstants.LOG_PROTECT
import org.onap.ccsdk.cds.controllerblueprints.core.data.PropertyDefinition
import org.onap.ccsdk.cds.controllerblueprints.core.utils.PropertyDefinitionUtils.Companion.hasLogProtect
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PropertyDefinitionUtilsTest {

    @Test
    fun testLogProtectMetadata() {
        val metadata = mutableMapOf<String, String>()

        assertFalse { hasLogProtect(metadata) }

        metadata[LOG_PROTECT] = "true"
        assertTrue { hasLogProtect(metadata) }
        metadata.clear()

        metadata[LOG_PROTECT] = "yes"
        assertTrue { hasLogProtect(metadata) }
        metadata.clear()

        metadata[LOG_PROTECT] = "y"
        assertTrue { hasLogProtect(metadata) }
        metadata.clear()

        metadata[LOG_PROTECT] = "false"
        assertFalse { hasLogProtect(metadata) }
        metadata.clear()

        val nullMetadata: MutableMap<String, String>? = null
        assertFalse { hasLogProtect(nullMetadata) }
    }

    @Test
    fun testHasLogProtectPropertyDefinition() {
        var propertyDefinition: PropertyDefinition? = null
        assertFalse { hasLogProtect(propertyDefinition) }

        propertyDefinition = PropertyDefinition()
        assertFalse { hasLogProtect(propertyDefinition) }

        val metadata = mutableMapOf<String, String>()
        metadata[LOG_PROTECT] = "TRUE"
        propertyDefinition.metadata = metadata

        assertTrue { hasLogProtect(propertyDefinition) }
    }
}
