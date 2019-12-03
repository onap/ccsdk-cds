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

package org.onap.ccsdk.cds.controllerblueprints.core

import org.junit.Test
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import kotlin.test.assertNotNull

class GraphExtensionFunctionsTest {

    @Test
    fun testGraph() {
        val graph = "[p>q/SUCCESS, m>q/SUCCESS, k, p>m/FAILURE, o>p/SUCCESS]".toGraph()
        assertNotNull(graph, "failed to create graph")
        assertNotNull(graph.toAdjacencyList(), "failed to adjacency list from graph")

        val neighbors = graph.nodes["p"]!!.neighbors()
        assertNotNull(neighbors, "failed to neighbors from graph for 'p' node")

        val nodePath = graph.nodes["p"]!!.neighbors(EdgeLabel.SUCCESS)
        assertNotNull(nodePath, "failed to nodePath from graph for 'p' node 'SUCCESS' label")
    }
}
