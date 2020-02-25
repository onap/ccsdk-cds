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
import {ModelType} from './model/ModelType.model';
import {DesignerDashboardState} from './model/designer.dashboard.state';
import { DeclarativeWorkflow } from './model/designer.workflow';
import { NodeTemplate } from './model/desinger.nodeTemplate.model';


@Injectable({
    providedIn: 'root'
})
export class DesignerStore extends Store<DesignerDashboardState> {

    constructor(private designerService: DesignerService) {
        super(new DesignerDashboardState());
    }

    public retrieveFuntions() {
        const modelDefinitionType = 'node_type';
        this.designerService.getFunctions(modelDefinitionType).subscribe(
            (modelTypeList: ModelType[]) => {
                console.log(modelTypeList);
                this.setState({
                    ...this.state,
                    serverFunctions: modelTypeList,
                });
            });
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
                workflows:
                    this.state.template.workflows.set(workflowName, new DeclarativeWorkflow())
            }
        });
    }

    addStepToDeclarativeWorkFlow(workflowName: string, stepType: string) {
        const currentWorkflow: DeclarativeWorkflow = this.state.template.workflows.get(workflowName);
        currentWorkflow.steps = {
                target: stepType,
                description: ''
            };
        const allNewWorkflowsMap =
            this.state.template.workflows.set(workflowName, currentWorkflow);
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                workflows: allNewWorkflowsMap
            }
        });
    }


    addNodeTemplate(nodeTemplateName: string) {
        this.setState({
            ...this.state,
            template: {
                ...this.state.template,
                node_templates:
                    this.state.template.node_templates.set(nodeTemplateName, new NodeTemplate())
            }
        });
    }
}
