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

import {Injectable} from '@angular/core';
import {Store} from '../../../../common/core/stores/Store';
import {DesignerService} from './designer.service';
import {DesignerDashboardState} from './model/designer.dashboard.state';
import {DeclarativeWorkflow} from './model/designer.workflow';
import {NodeTemplate} from './model/desinger.nodeTemplate.model';


@Injectable({
    providedIn: 'root'
})
export class DesignerStore extends Store<DesignerDashboardState> {

    constructor(private designerService: DesignerService) {
        super(new DesignerDashboardState());
    }

    /**
     * adds empty workflow with name only.
     * called when blank action is added to the board
     * declarative workflow just contain the steps but its order is determind by dg-graph
     */
    addDeclarativeWorkFlow(workflowName: string) {
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                workflows: {
                    ...this.state.template.workflows,
                    [workflowName]: new DeclarativeWorkflow()
                }
            }
        });
    }

    addStepToDeclarativeWorkFlow(workflowName: string, stepName: string, stepType: string) {
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                workflows: {
                    ...this.state.template.workflows,
                    [workflowName]: {
                        ...this.state.template.workflows[workflowName],
                        steps: {
                            [stepName]: {
                                target: stepType,
                                description: ''
                            }
                        }
                    }
                }
            }
        });
    }

    saveSourceContent(code: string) {
        console.log(code);
        if (code) {
            const topologyTemplate = JSON.parse(code);
            this.setState({
                ...this.state,
                sourceContent: code,
                template: topologyTemplate
            });
        }
    }


    /**
     * adding node tempates is a separate action of adding the steps to the workflow
     * you can add node template and don't add workflow step when you add dependencies for the
     * dg-generic function for example
     */
    addNodeTemplate(nodeTemplateName: string, type: string) {
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                node_templates: {
                    ...this.state.template.node_templates,
                    [nodeTemplateName]: new NodeTemplate(type)
                }
            }
        });
    }

    addDgGenericNodeTemplate(nodeTemplateName: string) {
        const node = new NodeTemplate('dg-generic');
        node.properties = {
            'dependency-node-templates': []
        };
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                node_templates: {
                    ...this.state.template.node_templates,
                    [nodeTemplateName]: node
                }
            }
        });
    }

    addDgGenericDependency(dgGenericNodeName: string, dependency: string) {
        const props = this.state.template.node_templates[dgGenericNodeName].properties;
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                node_templates: {
                    ...this.state.template.node_templates,
                    [dgGenericNodeName]: {
                        ...this.state.template.node_templates[dgGenericNodeName],
                        properties: {
                            'dependency-node-templates': [
                                ...props['dependency-node-templates'],
                                dependency
                            ]
                        }
                    }
                }
            }
        });
    }

    setInputsToSpecificWorkflow(inputs: Map<string, string>) {
        /* tslint:disable:no-string-literal */
        let mapOfWorkflows = this.state.template.workflows['Action1']['steps'];
        mapOfWorkflows += inputs;
        /*mapOfWorkflows.forEach(((value, key) => {
            if (value.includes('resource-assignment')) {
                value += inputs;
            }
        }));*/
        console.log('the new workflows');
        console.log(mapOfWorkflows);
    }

    clear() {
        this.setState(new DesignerDashboardState());
    }

}
