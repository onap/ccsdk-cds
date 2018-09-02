/*
 * Copyright © 2017-2018 AT&T Intellectual Property.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.onap.ccsdk.config.model.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

public class TopologicalSortingUtils<V> {
    
    /**
     * The implementation here is basically an adjacency list, but instead of an array of lists, a Map
     * is used to map each vertex to its list of adjacent vertices.
     */
    private Map<V, List<V>> neighbors = new HashMap<>();
    
    /**
     * String representation of graph.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        neighbors.forEach((v, vs) -> s.append("\n    " + v + " -> " + vs));
        return s.toString();
    }
    
    public Map<V, List<V>> getNeighbors() {
        return neighbors;
    }
    
    /**
     * Add a vertex to the graph. Nothing happens if vertex is already in graph.
     */
    public void add(V vertex) {
        if (neighbors.containsKey(vertex))
            return;
        neighbors.put(vertex, new ArrayList<V>());
    }
    
    /**
     * True iff graph contains vertex.
     */
    public boolean contains(V vertex) {
        return neighbors.containsKey(vertex);
    }
    
    /**
     * Add an edge to the graph; if either vertex does not exist, it's added. This implementation allows
     * the creation of multi-edges and self-loops.
     */
    public void add(V from, V to) {
        this.add(from);
        this.add(to);
        neighbors.get(from).add(to);
    }
    
    /**
     * Remove an edge from the graph. Nothing happens if no such edge.
     *
     * @throws IllegalArgumentException if either vertex doesn't exist.
     */
    public void remove(V from, V to) {
        if (!(this.contains(from) && this.contains(to)))
            throw new IllegalArgumentException("Nonexistent vertex");
        neighbors.get(from).remove(to);
    }
    
    /**
     * Report (as a Map) the out-degree of each vertex.
     */
    public Map<V, Integer> outDegree() {
        Map<V, Integer> result = new HashMap<>();
        neighbors.forEach((v, vs) -> result.put(v, vs.size()));
        return result;
    }
    
    /**
     * Report (as a Map) the in-degree of each vertex.
     */
    public Map<V, Integer> inDegree() {
        Map<V, Integer> result = new HashMap<>();
        for (V v : neighbors.keySet())
            result.put(v, 0); // All in-degrees are 0
            
        neighbors.forEach((from, vs) -> vs.forEach(to -> result.put(to, result.get(to) + 1) // Increment in-degree
        ));
        
        return result;
    }
    
    /**
     * Report (as a List) the topological sort of the vertices; null for no such sort.
     */
    @SuppressWarnings({"squid:S1149", "squid:S1168"})
    public List<V> topSort() {
        Map<V, Integer> degree = inDegree();
        // Determine all vertices with zero in-degree
        Stack<V> zeroVerts = new Stack<>(); // Stack as good as any here
        
        degree.forEach((v, vs) -> {
            if (vs == 0)
                zeroVerts.push(v);
        });
        
        // Determine the topological order
        List<V> result = new ArrayList<>();
        while (!zeroVerts.isEmpty()) {
            V v = zeroVerts.pop(); // Choose a vertex with zero in-degree
            result.add(v); // Vertex v is next in topol order
            // "Remove" vertex v by updating its neighbors
            for (V neighbor : neighbors.get(v)) {
                degree.put(neighbor, degree.get(neighbor) - 1);
                // Remember any vertices that now have zero in-degree
                if (degree.get(neighbor) == 0)
                    zeroVerts.push(neighbor);
            }
        }
        // Check that we have used the entire graph (if not, there was a cycle)
        if (result.size() != neighbors.size())
            return null;
        return result;
    }
    
    /**
     * True iff graph is a dag (directed acyclic graph).
     */
    public boolean isDag() {
        return topSort() != null;
    }
    
    /**
     * Report (as a Map) the bfs distance to each vertex from the start vertex. The distance is an
     * Integer; the value null is used to represent infinity (implying that the corresponding node
     * cannot be reached).
     */
    public Map bfsDistance(V start) {
        Map<V, Integer> distance = new HashMap<>();
        // Initially, all distance are infinity, except start node
        for (V v : neighbors.keySet())
            distance.put(v, null);
        distance.put(start, 0);
        // Process nodes in queue order
        Queue<V> queue = new LinkedList<>();
        queue.offer(start); // Place start node in queue
        while (!queue.isEmpty()) {
            V v = queue.remove();
            int vDist = distance.get(v);
            // Update neighbors
            for (V neighbor : neighbors.get(v)) {
                if (distance.get(neighbor) != null)
                    continue; // Ignore if already done
                distance.put(neighbor, vDist + 1);
                queue.offer(neighbor);
            }
        }
        return distance;
    }
    
    /**
     * Main program (for testing). public static void main (String[] args) { // Create a Graph with
     * Integer nodes TopologicalSortingUtils<String> graph = new TopologicalSortingUtils<String>();
     * graph.add("bundle-id", "bundle-mac"); graph.add("bundle-id", "bundle-ip");
     * graph.add("bundle-mac", "bundle-ip"); graph.add("bundle-ip", "bundle-mac");
     * System.out.println("The current graph: " + graph); System.out.println("In-degrees: " +
     * graph.inDegree()); System.out.println("Out-degrees: " + graph.outDegree()); System.out.println("A
     * topological sort of the vertices: " + graph.topSort()); System.out.println("The graph " +
     * (graph.isDag()?"is":"is not") + " a dag"); }
     */
}
