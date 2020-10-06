import {Component, OnInit} from '@angular/core';
import {InputActionAttribute, OutputActionAttribute} from './models/InputActionAttribute';

@Component({
    selector: 'app-action-attributes',
    templateUrl: './action-attributes.component.html',
    styleUrls: ['./action-attributes.component.css']
})
export class ActionAttributesComponent implements OnInit {

    inputs: [InputActionAttribute];
    outputs: [OutputActionAttribute];
    actionAttributesSideBar: boolean;

    constructor() {
    }

    ngOnInit() {
    }

    addInput(input: InputActionAttribute) {
        this.inputs.push(input);
    }

    addOutput(output: OutputActionAttribute) {
        this.outputs.push(output);
    }
}
