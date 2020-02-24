export class Workflow {
    inputs: {};
    outputs?: {};
}

export class DeclarativeWorkflow implements Workflow {
    steps: {};
    inputs: {};
    outputs?: {};

    constructor() {
        this.steps = {};
    }
}
