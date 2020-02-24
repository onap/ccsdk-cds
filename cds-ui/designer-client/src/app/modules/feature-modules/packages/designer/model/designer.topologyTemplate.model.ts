import { DeclarativeWorkflow } from './designer.workflow';
import { NodeTemplate } from './desinger.nodeTemplate.model';

export class TopologyTemplate {

    workflows: Map<string, DeclarativeWorkflow>;
    'node_templates': Map<string, NodeTemplate>;

    constructor() {
        this.workflows = new Map();
        this.node_templates = new Map();
    }
}
