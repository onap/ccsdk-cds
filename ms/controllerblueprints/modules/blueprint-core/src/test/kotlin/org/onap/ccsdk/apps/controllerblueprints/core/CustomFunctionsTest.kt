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

package org.onap.ccsdk.apps.controllerblueprints.core

import org.junit.Test
import kotlin.test.assertEquals
/**
 *
 *
 * @author Brinda Santh
 */
class CustomFunctionsTest {
    @Test
    fun testFormat(): Unit {
        val returnValue : String = format("This is {} for times {}", "test", 2)
        assertEquals("This is test for times 2", returnValue, "Failed to format String")

        val returnValue1 : String = format("This is test for times 2")
        assertEquals("This is test for times 2", returnValue1, "Failed to format empty args")
    }
}