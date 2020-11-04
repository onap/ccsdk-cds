export class NodeTemplate {
    type: string;
    properties?: {
        'dependency-node-templates'?: string[]
    };
    artifacts?: {};
    cabapilities?: {};
    requirements?: {};
    interfaces?: {};

    constructor(type) {
        this.type = type;
        this.properties = {};
        this.artifacts = {};
        this.interfaces = {};
    }
}

export class NodeProcess {
    inputs: {} = {};
    outputs: {} = {};
}

