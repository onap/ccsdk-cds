import {Component, OnInit} from '@angular/core';
import {InputActionAttribute, OutputActionAttribute} from './models/InputActionAttribute';

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
    outputOtherType: string = '';
    inputOtherType: string = '';

    constructor() {

    }

    ngOnInit() {
    }

    _toggleSidebar2() {
        this.actionAttributesSideBar = !this.actionAttributesSideBar;
    }

    addInput(input: InputActionAttribute) {
        if (input && input.type && input.name) {
            const insertedInputActionAttribute = new InputActionAttribute();
            insertedInputActionAttribute.type = input.type;
            insertedInputActionAttribute.name = input.name;
            insertedInputActionAttribute.required = input.required;
            insertedInputActionAttribute.description = input.description;
            this.inputs.push(insertedInputActionAttribute);
        }
    }

    addOutput(output: OutputActionAttribute) {
        if (output && output.type && output.name) {
            const insertedOutputActionAttribute = new OutputActionAttribute();
            insertedOutputActionAttribute.type = output.type;
            insertedOutputActionAttribute.name = output.name;
            insertedOutputActionAttribute.required = output.required;
            insertedOutputActionAttribute.description = output.description;
            this.outputs.push(insertedOutputActionAttribute);
        }
    }

    setInputType(type: string) {
        this.inputActionAttribute.type = type;
        this.isInputOtherType = this.checkIfTypeIsOther(type);
        /*if (this.inputActionAttribute) {
            this.inputActionAttribute.type = this.inputOtherType;
        }*/
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
    }

    private clearFormInputs() {
        this.inputActionAttribute = new InputActionAttribute();
        this.outputActionAttribute = new OutputActionAttribute();
        this.outputOtherType = '';
        this.inputOtherType = '';
    }
}
