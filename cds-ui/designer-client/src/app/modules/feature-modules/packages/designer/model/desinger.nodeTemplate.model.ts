export class NodeTemplate {
    type: string;
    properties?: {
        'dependency-node-template'?: string[]
    };
    interfaces?: {};
    artifacts?: {};
    cabapilities?: {};
    requirements?: {};

    constructor(type) {
        this.type = type;
        this.properties = {};
    }
}
