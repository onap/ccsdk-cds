/*
 *  Copyright © 2017-2018 AT&T Intellectual Property.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.onap.ccsdk.apps.controllerblueprints.core.utils

import com.att.eelf.configuration.EELFLogger
import com.att.eelf.configuration.EELFManager
import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.data.ServiceTemplate
import java.io.FileNotFoundException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JacksonReactorUtilsTest {
    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())
    @Test
    fun testReadValues() {

        val serviceTemplate = JacksonReactorUtils.readValueFromFile("load/blueprints/baseconfiguration/Definitions/activation-blueprint.json",
                ServiceTemplate::class.java).block()

        assertNotNull(serviceTemplate, "Failed to simple transform Service Template")
        assertEquals(true, serviceTemplate is ServiceTemplate, "failed to get Service Template instance")

        val jsonContent = JacksonReactorUtils.getJson(serviceTemplate, true).block()
        assertNotNull(jsonContent, "Failed to get json content")

        val jsonNode = JacksonReactorUtils.jsonNodeFromFile("load/blueprints/baseconfiguration/Definitions/activation-blueprint.json")
                .block()
        assertNotNull(jsonContent, "Failed to get json Node")
    }

    @Test(expected = FileNotFoundException::class)
    fun testReadValuesFailure() {
        JacksonReactorUtils.jsonNodeFromFile("load/blueprints/not-found.json")
                .block()
    }
}