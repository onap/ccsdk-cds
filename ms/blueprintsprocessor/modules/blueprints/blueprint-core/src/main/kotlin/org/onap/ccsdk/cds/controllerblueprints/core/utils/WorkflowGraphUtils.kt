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

import org.onap.ccsdk.cds.controllerblueprints.core.BlueprintConstants
import org.onap.ccsdk.cds.controllerblueprints.core.data.EdgeLabel
import org.onap.ccsdk.cds.controllerblueprints.core.data.Graph
import org.onap.ccsdk.cds.controllerblueprints.core.data.Workflow
import org.onap.ccsdk.cds.controllerblueprints.core.endNodes
import org.onap.ccsdk.cds.controllerblueprints.core.startNodes

object WorkflowGraphUtils {

    fun workFlowToGraph(workflow: Workflow): Graph {
        val graph = Graph()
        workflow.steps?.forEach { (stepName, step) ->
            step.onSuccess?.forEach { successTarget ->
                graph.addEdge(stepName, successTarget, EdgeLabel.SUCCESS)
            }
            step.onFailure?.forEach { failureTarget ->
                graph.addEdge(stepName, failureTarget, EdgeLabel.FAILURE)
            }
        }
        graph.startNodes().forEach { rootNode ->
            graph.addEdge(BlueprintConstants.GRAPH_START_NODE_NAME, rootNode.id, EdgeLabel.SUCCESS)
        }
        graph.endNodes().forEach { endNode ->
            graph.addEdge(endNode.id, BlueprintConstants.GRAPH_END_NODE_NAME, EdgeLabel.SUCCESS)
        }
        return graph
    }
}
