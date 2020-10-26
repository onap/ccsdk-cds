import {Component, OnInit} from '@angular/core';
import {InputActionAttribute, OutputActionAttribute} from './models/InputActionAttribute';
import {DesignerStore} from '../designer.store';
import {DesignerDashboardState} from '../model/designer.dashboard.state';

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
    actionName = '';
    designerState: DesignerDashboardState;

    constructor(private designerStore: DesignerStore) {

    }

    ngOnInit() {
        this.designerStore.state$.subscribe(designerState => {
            this.designerState = designerState;
            if (this.designerState && this.designerState.actionName) {
                this.actionName = this.designerState.actionName;
                const action = this.designerState.template.workflows[this.actionName];
            }
        });
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
        this.addInput(this.inputActionAttribute);
        this.addOutput(this.outputActionAttribute);
        this.clearFormInputs();
        this.designerStore.setInputsAndOutputsToSpecificWorkflow(this.storeInputs(this.inputs)
            , this.storeOutputs(this.outputs), this.actionName);
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
        if (inputs.endsWith(',')) {
            inputs = inputs.substr(0, inputs.length - 1);
        }
        return JSON.parse('{' + inputs + '}');
    }

    private storeOutputs(OutputActionAttributes: OutputActionAttribute[]) {
        let outputs = '';
        OutputActionAttributes.forEach(output => {
            outputs += this.appendAttributes(output);
        });
        if (outputs.endsWith(',')) {
            outputs = outputs.substr(0, outputs.length - 1);
        }
        return JSON.parse('{' + outputs + '}');
    }

    private appendAttributes(output: OutputActionAttribute) {
        return '"' + output.name + '" : {\n' +
            '            "required" : ' + output.required + ',\n' +
            '            "type" : "' + output.type + '",\n' +
            '            "description" : "' + output.description + '"\n' +
            '          },';
    }
}
