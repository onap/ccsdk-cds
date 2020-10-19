export class NodeTemplate {
    type: string;
    properties?: {
        'dependency-node-templates'?: string[]
    };
    interfaces?: {};
    artifacts?: {};
    cabapilities?: {};
    requirements?: {};

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

