/*
 *  Copyright © 2018 IBM.
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

import org.junit.Test
import org.onap.ccsdk.apps.controllerblueprints.core.asJsonPrimitive
import kotlin.test.assertEquals

class JsonParserUtilsTest {

    @Test
    fun `test parse Node`() {
        val dataNode = JacksonUtils.jsonNodeFromClassPathFile("data/default-context.json")

        val parsedNode = JsonParserUtils.parse(dataNode, "$.request-id")

        assertEquals(parsedNode, "12345".asJsonPrimitive(), "failed to parse json request-id")
    }
}