/*-
 * ============LICENSE_START=======================================================
 * ONAP - CCSDK
 * ================================================================================
 * Copyright (C) 2022 Tech Mahindra
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.cds.controllerblueprints.core.service

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.service.BluePrintVelocityTemplateService
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.BeforeTest
import java.io.File
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull


class BlueprintVelocityTemplateTest {
    private val velocityHome = System.getenv("velocity_path")


    @BeforeTest
    fun setup() {
        val properties = Properties()
        properties["file.resource.loader.path"] = velocityHome
    }
    @Test
    fun testVelocityGeneratedContent() {
        runBlocking {
            val template = JacksonUtils.getContent("Templates/base-config-velocity-template.vtl")
            val json = JacksonUtils.getContent("Templates/base-config-data-velocity.json")
            val content = BluePrintVelocityTemplateService.generateContent(template, json)
            assertNotNull(content, "failed to generate content for velocity template")
        }
    }
    @Test
    fun `no value variable should evaluate to default value - standalone template mesh test`() {
        runBlocking {
            val template = JacksonUtils.getContent("Templates/default-variable-value-velocity-template.vtl")
            val json = JacksonUtils.getContent("Templates/default-variable-value-data.json")
            val content = BluePrintVelocityTemplateService.generateContent(template, json)
            val expected = "sample-hostname\n\${node0_backup_router_address}"
            assertEquals(expected, content, "No value variable should use default value")
        }
    }
    @Test
    fun `Expected value variable from file`() {
        runBlocking {
            val template = JacksonUtils.getContent("Templates/default-variable-value-velocity-template.vtl")
            val json = JacksonUtils.getContent("Templates/default-variable-value-data.json")
            val content = BluePrintVelocityTemplateService.generateContent(template, json)
            val expected = File("Tests/kotlin/default-variable-value-data.txt").readText()
            assertEquals(expected, content, "expected value variable from file")
        }

    }
}