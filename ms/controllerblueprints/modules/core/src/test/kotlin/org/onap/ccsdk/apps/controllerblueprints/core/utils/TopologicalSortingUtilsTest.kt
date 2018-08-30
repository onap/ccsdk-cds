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

package org.onap.ccsdk.apps.controllerblueprints.core.utils

import org.junit.Test

class TopologicalSortingUtilsTest {
    
    @Test
    fun testSorting(): Unit {
        val graph: TopologicalSortingUtils<String> = TopologicalSortingUtils()
        graph.add("bundle-id", "bundle-mac")
        graph.add("bundle-id", "bundle-ip")
        graph.add("bundle-mac", "bundle-ip")
        graph.add("bundle-ip", "bundle-mac")

        println("The current graph: " + graph)
        println("In-degrees: " + graph.inDegree())
        println("Out-degrees: " + graph.outDegree())
        println("A topological sort of the vertices: " + graph.topSort())
    }
}