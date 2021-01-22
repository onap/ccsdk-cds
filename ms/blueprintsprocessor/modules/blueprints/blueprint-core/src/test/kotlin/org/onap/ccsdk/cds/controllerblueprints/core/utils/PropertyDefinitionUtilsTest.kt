/*
 * Copyright © 2019 Bell Canada.
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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants.LOG_PROTECT
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
