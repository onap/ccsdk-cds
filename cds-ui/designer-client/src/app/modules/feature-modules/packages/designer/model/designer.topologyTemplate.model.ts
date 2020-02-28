import { DeclarativeWorkflow } from './designer.workflow';
import { NodeTemplate } from './desinger.nodeTemplate.model';

export class TopologyTemplate {

    workflows: {};
    'node_templates': {};

    constructor() {
        this.workflows = {};
        this.node_templates = {};
    }
}
