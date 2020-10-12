import {Component, OnInit} from '@angular/core';
import {InputActionAttribute, OutputActionAttribute} from './models/InputActionAttribute';
import {DesignerStore} from '../designer.store';

@Component({
    selector: 'app-action-attributes',
    templateUrl: './action-attributes.component.html',
    styleUrls: ['./action-attributes.component.css']
})
export class ActionAttributesComponent implements OnInit {

    inputs = [];
    outputs = [];
    actionAttributesSideBar: boolean;
    inputActionAttribute = new InputActionAttribute();
    outputActionAttribute = new OutputActionAttribute();
    isInputOtherType: boolean;
    isOutputOtherType: boolean;
    outputOtherType = '';
    inputOtherType = '';

    constructor(private designerStore: DesignerStore) {

    }

    ngOnInit() {
    }

    addInput(input: InputActionAttribute) {
        if (input && input.type && input.name) {
            const insertedInputActionAttribute = Object.assign({}, input);
            this.inputs.push(insertedInputActionAttribute);
        }
    }

    addOutput(output: OutputActionAttribute) {
        if (output && output.type && output.name) {
            const insertedOutputActionAttribute = Object.assign({}, output);
            this.outputs.push(insertedOutputActionAttribute);
        }
    }

    setInputType(type: string) {
        this.inputActionAttribute.type = type;
        this.isInputOtherType = this.checkIfTypeIsOther(type);
    }

    setInputRequired(isRequired) {
        this.inputActionAttribute.required = isRequired;
    }

    setOutputRequired(isRequired) {
        this.outputActionAttribute.required = isRequired;
    }

    setOutputType(type: string) {
        this.outputActionAttribute.type = type;
        this.isOutputOtherType = this.checkIfTypeIsOther(type);
    }

    checkIfTypeIsOther(type) {
        return type.includes('Other');
    }

    submitAttributes() {
        console.log(this.inputActionAttribute);
        console.log(this.outputActionAttribute);
        this.addInput(this.inputActionAttribute);
        this.addOutput(this.outputActionAttribute);
        this.clearFormInputs();
        console.log(this.storeInputs(this.inputs));
        this.designerStore.setInputsToSpecificWorkflow(this.storeInputs(this.inputs));
        console.log(this.storeOutputs(this.outputs));
    }

    private clearFormInputs() {
        this.inputActionAttribute = new InputActionAttribute();
        this.outputActionAttribute = new OutputActionAttribute();
        this.outputOtherType = '';
        this.inputOtherType = '';
    }

    private storeInputs(InputActionAttributes: InputActionAttribute[]) {

        let inputs = '';
        InputActionAttributes.forEach(input => {
            inputs += this.appendAttributes(input);

        });
        const returnedInputMap = new Map<string, string>();
        returnedInputMap.set('inputs', inputs);
        return returnedInputMap;
    }

    private storeOutputs(OutputActionAttributes: OutputActionAttribute[]) {
        let outputs = '';
        OutputActionAttributes.forEach(output => {
            outputs += this.appendAttributes(output);
        });
        const returnedOutputMap = new Map<string, string>();
        returnedOutputMap.set('outputs', outputs);
        return returnedOutputMap;
    }

    private appendAttributes(output: OutputActionAttribute) {
        return '"' + output.name + '":{\n' +
            '                \'required\': ' + output.required + ',\n' +
            '                \'type\': "' + output.type + '",\n' +
            '                \'description\': "' + output.description + '"\n' +
            '            }' + '\n';
    }
}
