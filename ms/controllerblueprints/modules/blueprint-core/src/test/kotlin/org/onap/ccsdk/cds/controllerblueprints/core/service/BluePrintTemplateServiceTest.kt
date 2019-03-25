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

package org.onap.ccsdk.cds.controllerblueprints.core.service

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.utils.JacksonUtils
import kotlin.test.assertNotNull

class BluePrintTemplateServiceTest {

    @Test
    fun testGenerateContent() {

        val template = JacksonUtils.getClassPathFileContent("templates/base-config-template.vtl")
        val json = JacksonUtils.getClassPathFileContent("templates/base-config-data.json")

        val content = BluePrintTemplateService.generateContent(template, json)
        assertNotNull(content, "failed to generate content for velocity template")

    }
}