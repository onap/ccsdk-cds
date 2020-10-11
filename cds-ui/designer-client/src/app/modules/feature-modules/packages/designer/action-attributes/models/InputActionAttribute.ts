export class InputActionAttribute {
    name: string;
    description: string;
    type: string;
    required: boolean;

    constructor() {
        this.name = '';
        this.description = '';
        this.type = '';
        this.required = true;
    }
}

export class OutputActionAttribute extends InputActionAttribute {

}
