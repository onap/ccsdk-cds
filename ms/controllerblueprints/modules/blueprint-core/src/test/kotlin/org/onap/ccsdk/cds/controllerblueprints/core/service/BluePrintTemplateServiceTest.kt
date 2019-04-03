/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 *
 * Modifications Copyright © 2019 IBM, Bell Canada.
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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
class BluePrintTemplateServiceTest {

    @Test
    fun testVelocityGeneratedContent() {

        val template = JacksonUtils.getClassPathFileContent("templates/base-config-velocity-template.vtl")
        val json = JacksonUtils.getClassPathFileContent("templates/base-config-data-velocity.json")

        val content = BluePrintVelocityTemplateService.generateContent(template, json)
        assertNotNull(content, "failed to generate content for velocity template")

    }

    @Test
    fun testJinjaGeneratedContent() {

        val template = JacksonUtils.getClassPathFileContent("templates/base-config-jinja-template.jinja")
        val json = JacksonUtils.getClassPathFileContent("templates/base-config-data-jinja.json")

        val element: MutableMap<String, Any> = mutableMapOf()
        element["additional_array"] = arrayListOf(hashMapOf("name" to "Element1", "location" to "Region0"), hashMapOf("name" to "Element2", "location" to "Region1"))

        val content = BluePrintJinjaTemplateService.generateContent(template, json, false, element)
        assertNotNull(content, "failed to generate content for velocity template")

    }
}