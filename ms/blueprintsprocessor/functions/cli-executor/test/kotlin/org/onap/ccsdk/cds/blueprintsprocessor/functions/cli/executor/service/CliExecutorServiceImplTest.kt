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

package org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor.service

import org.junit.Test
import org.junit.runner.RunWith
import org.onap.ccsdk.cds.blueprintsprocessor.functions.cli.executor.api.CliExecutorService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import kotlin.test.assertNotNull

@RunWith(SpringRunner::class)
@ContextConfiguration(classes = [CliExecutorServiceImpl::class])
class CliExecutorServiceImplTest {

    @Autowired
    lateinit var cliExecutorService: CliExecutorService

    @Test
    fun testCliExecutorService() {
        assertNotNull(cliExecutorService, "failed to intilise cliExecutorService")
    }
}