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

package org.onap.ccsdk.cds.controllerblueprints.core.data

enum class EdgeLabel(val id: String) {
    SUCCESS("success"),
    FAILURE("failure"),
    DEFAULT("*")
}

enum class EdgeStatus(val id: String) {
    NOT_STARTED("not_started"),
    EXECUTED("executed"),
    SKIPPED("skipped")
}

enum class NodeStatus(val id: String) {
    NOT_STARTED("not_started"),
    READY("ready"),
    EXECUTING("executing"),
    EXECUTED("executed"),
    SKIPPED("skipped"),
    TERMINATED("terminated")
}

class Graph {

    val nodes: MutableMap<String, Node> = hashMapOf()
    val edges: MutableSet<Edge> = mutableSetOf()

    fun addNode(value: String): Node {
        val node = Node(value)
        nodes[value] = node
        return node
    }

    fun addEdge(source: String, destination: String, label: EdgeLabel) {
        if (!nodes.containsKey(source)) {
            addNode(source)
        }
        if (!nodes.containsKey(destination)) {
            addNode(destination)
        }
        val edge = Edge(nodes[source]!!, nodes[destination]!!, label)
        if (!edges.contains(edge)) {
            edges.add(edge)
            nodes[source]!!.edges.add(edge)
        }
    }

    override fun toString(): String {
        val standaloneNodes = nodes.values.filter { node -> edges.all { it.source != node && it.target != node } }
        val s = (edges.map { it.toString() } + standaloneNodes.map { it.toString() }).joinToString()
        return "[$s]"
    }

    fun print(): String {
        val buffer = StringBuffer("Nodes :")
        nodes.values.forEach {
            buffer.append("\n\t$it")
        }
        buffer.append("\nEdges :")
        edges.forEach {
            buffer.append("\n\t$it")
        }
        return buffer.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Graph
        return nodes == other.nodes && edges == other.edges
    }

    override fun hashCode() = 31 * nodes.hashCode() + edges.hashCode()

    fun equivalentTo(other: Graph): Boolean {
        return nodes == other.nodes && edges.all { edge -> other.edges.any { it.equivalentTo(edge) } }
    }

    data class Node(val id: String, var status: NodeStatus = NodeStatus.NOT_STARTED) {

        val edges: MutableList<Edge> = ArrayList()

        fun neighbors(): List<Node> = edges.map { edge -> edge.target(this) }

        fun neighbors(label: EdgeLabel): List<Node> = edges.filter { it.label == label }
            .map { edge -> edge.target(this) }

        fun labelEdges(label: EdgeLabel): List<Edge> = edges.filter { it.label == label }

        override fun toString() = "$id, Status($status)"
    }

    data class Edge(
        val source: Node,
        val target: Node,
        val label: EdgeLabel,
        var status: EdgeStatus = EdgeStatus.NOT_STARTED
    ) {

        fun target(node: Node): Node = target

        fun equivalentTo(other: Edge) =
            (source == other.source && target == other.target) ||
                (source == other.target && target == other.source)

        override fun toString() =
            "${source.id}>${target.id}/$label($status)"
    }

    data class TermForm(val nodes: Collection<String>, val edges: List<Term>) {

        data class Term(val source: String, val target: String, val label: EdgeLabel) {

            override fun toString() = "Term($source, $target, $label)"
        }
    }

    data class AdjacencyList<String, out EdgeLabel>(val entries: List<Entry<String, EdgeLabel>>) {
        constructor(vararg entries: Entry<String, EdgeLabel>) : this(entries.asList())

        override fun toString() = "AdjacencyList(${entries.joinToString()})"

        data class Entry<out String, out EdgeLabel>(val node: String, val links: List<Link<String, EdgeLabel>> = emptyList<Nothing>()) {
            constructor(node: String, vararg links: Link<String, EdgeLabel>) : this(node, links.asList())

            override fun toString() = "Entry($node, links[${links.joinToString()}])"
        }

        data class Link<out String, out EdgeLabel>(val node: String, val label: EdgeLabel) {

            override fun toString() = if (label == null) "$node" else "$node/$label"
        }
    }

    companion object {

        fun labeledDirectedTerms(termForm: TermForm): Graph =
            createFromTerms(termForm) { graph, n1, n2, value -> graph.addEdge(n1, n2, value) }

        fun labeledDirectedAdjacent(adjacencyList: AdjacencyList<String, EdgeLabel>): Graph =
            fromAdjacencyList(adjacencyList) { graph, n1, n2, value ->
                graph.addEdge(n1, n2, value)
            }

        private fun createFromTerms(
            termForm: TermForm,
            addFunction: (Graph, String, String, EdgeLabel) -> Unit
        ): Graph {
            val graph = Graph()
            termForm.nodes.forEach { graph.addNode(it) }
            termForm.edges.forEach { addFunction(graph, it.source, it.target, it.label) }
            return graph
        }

        private fun fromAdjacencyList(
            adjacencyList: AdjacencyList<String, EdgeLabel>,
            addFunction: (Graph, String, String, EdgeLabel) -> Unit
        ): Graph {
            val graph = Graph()
            adjacencyList.entries.forEach { graph.addNode(it.node) }
            adjacencyList.entries.forEach { (node, links) ->
                links.forEach { addFunction(graph, node, it.node, it.label) }
            }
            return graph
        }
    }
}
