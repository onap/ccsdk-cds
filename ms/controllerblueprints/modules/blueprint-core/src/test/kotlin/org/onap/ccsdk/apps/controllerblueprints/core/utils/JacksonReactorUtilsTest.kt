/*
 *  Copyright © 2019 IBM.
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
import kotlinx.coroutines.runBlocking
import org.junit.Test

class JacksonReactorUtilsTest {

    private val log: EELFLogger = EELFManager.getInstance().getLogger(this::class.toString())

    @Test
    fun testJsonNodeFromClassPathFile() {
        runBlocking {
            val filePath = "data/default-context.json"
            JacksonReactorUtils.jsonNodeFromClassPathFile(filePath)
        }
    }

    @Test
    fun testJsonNodeFromFile() {
        runBlocking {
            val filePath = "src/test/resources/data/default-context.json"
            JacksonReactorUtils.jsonNodeFromFile(filePath)
        }
    }
}