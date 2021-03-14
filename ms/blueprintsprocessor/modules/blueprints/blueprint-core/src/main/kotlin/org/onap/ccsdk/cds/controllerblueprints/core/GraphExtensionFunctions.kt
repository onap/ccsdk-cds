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

import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.utils.WorkflowGraphUtils
import java.util.regex.Pattern

private val graphTokenSeparators = Pattern.compile("[->/]")

/** Convert Blueprint workflow to graph data structure */
fun Workflow.asGraph(): Graph {
    return WorkflowGraphUtils.workFlowToGraph(this)
}

fun String.toGraph(): Graph {
    if (!startsWith('[') || !endsWith(']')) {
        throw IllegalArgumentException("Expected string starting '[' and ending with ']' but it was '$")
    }
    val tokens = substring(1, length - 1).replace("\n", "").split(", ").map { it.trim().split(graphTokenSeparators) }
    val nodes = tokens.flatMap { it.take(2) }.toCollection(LinkedHashSet())
    val edges = tokens.filter { it.size == 3 }.map { Graph.TermForm.Term(it[0], it[1], EdgeLabel.valueOf(it[2])) }
    return Graph.labeledDirectedTerms(Graph.TermForm(nodes, edges))
}

fun Graph.toAdjacencyList(): Graph.AdjacencyList<String, EdgeLabel> {
    val entries = nodes.values.map { node ->
        val links = node.edges.map { Graph.AdjacencyList.Link(it.target.id, it.label) }
        Graph.AdjacencyList.Entry(node = node.id, links = links)
    }
    return Graph.AdjacencyList(entries)
}

fun Graph.findAllPaths(from: String, to: String, path: List<String> = emptyList()): List<List<String>> {
    if (from == to) return listOf(path + to)
    return nodes[from]!!.neighbors()
        .filter { !path.contains(it.id) }
        .flatMap { findAllPaths(it.id, to, path + from) }
}

fun Graph.isAcyclic(): Boolean {
    val startNodes = startNodes()
    if (startNodes.isEmpty())
        return false

    val adj: Map<String, Set<String>> = toAdjacencyList().entries
        .associate { it.node to it.links }
        .mapValues { it.value.map { x -> x.node }.toSet() }

    fun hasCycle(node: String, visited: MutableSet<String> = mutableSetOf()): Boolean {
        if (visited.contains(node))
            return true
        visited.add(node)

        if (adj[node]!!.isEmpty()) {
            visited.remove(node)
            return false
        }

        if (adj[node]!!.any { hasCycle(it, visited) })
            return true

        visited.remove(node)
        return false
    }

    return startNodes.none { n -> hasCycle(n.id) }
}

fun Graph.startNodes() = this.nodes.values.filter {
    val incomingEdges = incomingEdges(it.id)
    incomingEdges.isEmpty()
}

fun Graph.endNodes(): Set<Graph.Node> = this.nodes.values.filter {
    outgoingEdges(it.id).isEmpty()
}.toSet()

fun Graph.node(node: String) = this.nodes[node]

fun Graph.edge(label: EdgeLabel) =
    this.edges.filter { it.label == label }

fun Graph.incomingEdges(node: String) =
    this.edges.filter { it.target.id == node }

fun Graph.incomingNodes(node: String) =
    this.incomingEdges(node).map { it.source }

fun Graph.outgoingEdges(node: String) =
    this.edges.filter { it.source.id == node }

fun Graph.outgoingNodes(node: String) =
    this.outgoingEdges(node).map { it.target }

fun Graph.outgoingEdges(node: String, label: EdgeLabel) =
    this.edges.filter { it.source.id == node && it.label == label }

fun Graph.outgoingNodes(node: String, label: EdgeLabel) =
    this.outgoingEdges(node, label).map { it.target }

fun Graph.outgoingNodesNotInEdgeLabels(node: String, labels: List<EdgeLabel>) =
    this.outgoingEdgesNotInLabels(node, labels).map { it.target }

fun Graph.outgoingEdges(node: String, labels: List<EdgeLabel>) =
    this.edges.filter { it.source.id == node && labels.contains(it.label) }

fun Graph.outgoingEdgesNotInLabels(node: String, labels: List<EdgeLabel>) =
    this.edges.filter { it.source.id == node && !labels.contains(it.label) }

fun Graph.outgoingNodes(node: String, labels: List<EdgeLabel>) =
    this.outgoingEdges(node, labels).map { it.target }

fun Graph.isEndNode(node: Graph.Node): Boolean {
    return this.endNodes().contains(node)
}

fun <T> List<T>.tail(): List<T> = drop(1)
