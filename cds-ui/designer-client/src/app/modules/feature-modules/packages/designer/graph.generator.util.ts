/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/
import {TopologyTemplate} from './model/designer.topologyTemplate.model';
import {Injectable} from '@angular/core';
import {GraphUtil} from './graph.util';

@Injectable({
    providedIn: 'root'
})
export class GraphGenerator {

    constructor(private graphUtil: GraphUtil) {
    }

    clear(boardGraph: joint.dia.Graph) {
        boardGraph.clear();
    }

    /**
     * loops over workflows
     * create action element
     * from steps --> create function element
     * add function element to action element
     * example toplogyTemplate
     *
     * {
     * "workflows": {
     *   "Action1": {
     *       "steps": {
     *           "STEP_NAME": {
     *               "target": "NODE_TEMPLATE_NAME",
     *                   "description": ""
     *           }
     *       }
     *   }
     * },
     * "node_templates": {
     *    "NODE_TEMPLATE_NAME": {
     *        "type": "dg-generic",
     *            "properties": {
     *            "dependency-node-templates": [
     *                "component-config-snapshots-executor",
     *                "component-jython-executor"
     *            ]
     *        }
     *    },
     *    "component-config-snapshots-executor": {
     *        "type": "component-config-snapshots-executor",
     *            "properties": { }
     *    },
     *    "component-jython-executor": {
     *        "type": "component-jython-executor",
     *            "properties": { }
     *    }
     * }
     * }
     */

    public populate(topologyTempalte: TopologyTemplate,
                    boardGraph: joint.dia.Graph) {

        Object.keys(topologyTempalte.workflows).forEach(workFlowName => {
            console.log('drawing workflow item --> ', workFlowName);

            // create action element
            const actionElement =
                this.graphUtil.createCustomActionWithName(workFlowName, boardGraph);

            // create board function elements
            const workflow = topologyTempalte.workflows[workFlowName].steps;
            const stepName = Object.keys(workflow)[0];
            if (stepName) {
                const nodeTemplateName = workflow[stepName].target;
                const functionType = topologyTempalte.node_templates[nodeTemplateName].type;
                console.log('draw function with ', stepName, functionType);

                this.graphUtil.dropFunctionOverActionRelativeToParent(
                    actionElement,
                    stepName, functionType, boardGraph);

                // TODO handle dg-generic case (multi-step in the same action)
                if (functionType === 'dg-generic') {
                    const props = topologyTempalte.node_templates[nodeTemplateName].properties;
                    console.log('dg props', props);
                    props['dependency-node-templates'].forEach(dependencyStepName => {
                        const dependencyType = topologyTempalte.node_templates[dependencyStepName].type;
                        console.log('dependencyType', dependencyType);
                        this.graphUtil.dropFunctionOverActionRelativeToParent(
                            actionElement,
                            dependencyStepName, dependencyType, boardGraph);

                    });
                }
            }
        });

    }
}
