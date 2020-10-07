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

    constructor() {

    }

    ngOnInit() {
    }

    _toggleSidebar2() {
        this.actionAttributesSideBar = !this.actionAttributesSideBar;
    }

    addInput(input: InputActionAttribute) {
        this.inputs.push(input);
    }

    addOutput(output: OutputActionAttribute) {
        this.outputs.push(output);
    }

    setInputType(type) {
        this.inputActionAttribute.type = type;
    }

    setInputRequired(isRequired) {
        this.inputActionAttribute.required = isRequired;
    }

    setOutputRequired(isRequired) {
        this.outputActionAttribute.required = isRequired;
    }

    setOutputType(type) {
        this.outputActionAttribute.type = type;
    }

    submitAttributes() {
        console.log(this.inputActionAttribute);
        console.log(this.outputActionAttribute);
        this.inputs.push(this.inputActionAttribute);
        this.outputs.push(this.outputActionAttribute);
    }
}
