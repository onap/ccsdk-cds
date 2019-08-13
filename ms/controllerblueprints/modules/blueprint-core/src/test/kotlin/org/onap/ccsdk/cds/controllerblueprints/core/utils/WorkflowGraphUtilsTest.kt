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

package org.onap.ccsdk.cds.controllerblueprints.core.utils

import org.onap.ccsdk.cds.controllerblueprints.core.dsl.workflow
import kotlin.test.Test
import kotlin.test.assertNotNull

class WorkflowGraphUtilsTest {

    @Test
    fun testWorkFlowToGraph() {

        val workflow = workflow("sample", "") {
            step("A", "A", "") {
                success("B")
            }
            step("B", "B", "") {
                success("C")
                failure("D")
            }
            step("C", "C", "")
            step("D", "D", "")
        }
        val graph = WorkflowGraphUtils.workFlowToGraph(workflow)
        assertNotNull(graph, "failed to create graph")
    }
}