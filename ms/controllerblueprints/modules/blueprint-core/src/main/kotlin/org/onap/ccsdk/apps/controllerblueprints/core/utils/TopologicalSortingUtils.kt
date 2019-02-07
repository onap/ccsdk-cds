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

import java.util.*

/**
 *
 *
 * @author Brinda Santh
 */
class TopologicalSortingUtils<V> {

    private val neighbors: MutableMap<V, MutableList<V>> = hashMapOf()

    val isDag: Boolean
        get() = topSort() != null

    override fun toString(): String {
        val s = StringBuffer()
        for (v in neighbors.keys)
            s.append("\n    " + v + " -> " + neighbors[v])
        return s.toString()
    }

    fun getNeighbors(): Map<V, List<V>> {
        return neighbors
    }

    fun add(vertex: V) {
        if (neighbors.containsKey(vertex))
            return
        neighbors[vertex] = arrayListOf()
    }

    operator fun contains(vertex: V): Boolean {
        return neighbors.containsKey(vertex)
    }

    fun add(from: V, to: V) {
        this.add(from)
        this.add(to)
        neighbors[from]?.add(to)
    }

    fun remove(from: V, to: V) {
        if (!(this.contains(from) && this.contains(to)))
            throw IllegalArgumentException("Nonexistent vertex")
        neighbors[from]?.remove(to)
    }

    fun outDegree(): Map<V, Int> {
        val result: MutableMap<V, Int> = hashMapOf()
        for (v in neighbors.keys)
            result[v] = neighbors[v]!!.size
        return result
    }


    fun inDegree(): MutableMap<V, Int> {
        val result = HashMap<V, Int>()
        for (v in neighbors.keys)
            result[v] = 0       // All in-degrees are 0
        for (from in neighbors.keys) {
            for (to in neighbors[from]!!) {
                result[to] = result[to]!! + 1           // Increment in-degree
            }
        }
        return result
    }

    fun topSort(): List<V>? {
        val degree = inDegree()
        // Determine all vertices with zero in-degree
        val zeroVerts = Stack<V>()        // Stack as good as any here
        for (v in degree.keys) {
            if (degree[v] == 0) zeroVerts.push(v)
        }
        // Determine the topological order
        val result = ArrayList<V>()
        while (!zeroVerts.isEmpty()) {
            val v = zeroVerts.pop()                  // Choose a vertex with zero in-degree
            result.add(v)                          // Vertex v is next in topol order
            // "Remove" vertex v by updating its neighbors
            for (neighbor in neighbors[v]!!) {
                degree[neighbor] = degree[neighbor]!! - 1
                // Remember any vertices that now have zero in-degree
                if (degree[neighbor] == 0) zeroVerts.push(neighbor)
            }
        }
        // Check that we have used the entire graph (if not, there was a cycle)
        return if (result.size != neighbors.size) null else result
    }


    fun bfsDistance(start: V): Map<*, *> {
        val distance: MutableMap<V, Int> = hashMapOf()
        // Initially, all distance are infinity, except start node
        for (v in neighbors.keys)
            distance[v] = -1
        distance[start] = 0
        // Process nodes in queue order
        val queue = LinkedList<V>()
        queue.offer(start)                                    // Place start node in queue
        while (!queue.isEmpty()) {
            val v = queue.remove()
            val vDist = distance[v]!!
            // Update neighbors
            for (neighbor in neighbors[v]!!) {
                if (distance[neighbor] != null) continue  // Ignore if already done
                distance[neighbor] = vDist + 1
                queue.offer(neighbor)
            }
        }
        return distance
    }
}