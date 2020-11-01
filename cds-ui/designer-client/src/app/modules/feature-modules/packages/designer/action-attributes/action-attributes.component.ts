import {Component, OnInit} from '@angular/core';
import {InputActionAttribute, OutputActionAttribute} from './models/InputActionAttribute';
import {DesignerStore} from '../designer.store';
import {DesignerDashboardState} from '../model/designer.dashboard.state';
import {Action} from './models/Action';
import {FunctionsStore} from '../functions.store';
import {FunctionsState} from '../model/functions.state';

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
    isFunctionAttributeActive = false;
    functions: FunctionsState;

    constructor(private designerStore: DesignerStore, private functionsStore: FunctionsStore) {

    }

    ngOnInit() {
        this.designerStore.state$.subscribe(designerState => {
            this.designerState = designerState;
            if (this.designerState && this.designerState.actionName) {
                this.actionName = this.designerState.actionName;
                const action = this.designerState.template.workflows[this.actionName] as Action;
                if (action.steps) {
                    const steps = Object.keys(action.steps);
                    if (steps && steps.length > 0) {
                        this.isFunctionAttributeActive = true;
                    } else {
                        this.isFunctionAttributeActive = false;
                    }
                    steps.forEach(step => {
                        const target = action.steps[step].target;
                        this.getInputs(target);
                    });
                }


                this.inputs = [];
                if (action.inputs) {
                    const namesOfInput = Object.keys(action.inputs);
                    this.inputs = this.extractFields(namesOfInput, action.inputs);
                }
                this.outputs = [];
                if (action.outputs) {
                    const namesOfOutput = Object.keys(action.outputs);
                    this.outputs = this.extractFields(namesOfOutput, action.outputs);
                }
            }
        });

        this.functionsStore.state$.subscribe(functions => {
            this.functions = functions;
        });
    }


    private extractFields(namesOfOutput: string[], container: {}) {
        const fields = [];
        for (const nameOutput of namesOfOutput) {
            const fieldAttribute = new OutputActionAttribute();
            fieldAttribute.name = nameOutput;
            fieldAttribute.description = container[nameOutput].description;
            fieldAttribute.required = container[nameOutput].required;
            fieldAttribute.type = container[nameOutput].type;
            const insertedOutputActionAttribute = Object.assign({}, fieldAttribute);
            fields.push(insertedOutputActionAttribute);
        }
        return fields;
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

    getInputs(targetName) {
        const nodeTemplate = this.designerState.template.node_templates[targetName];
        /* tslint:disable:no-string-literal */
        console.log(nodeTemplate['type']);
        this.functions.serverFunctions
            /* tslint:disable:no-string-literal */
            .filter(currentFunction => currentFunction.modelName.includes(nodeTemplate['type']))
            .forEach(currentFunction => {
                console.log(currentFunction);
                /* tslint:disable:no-string-literal */
                if (currentFunction['definition'] && currentFunction['definition']['interfaces']) {
                    const interfaces = Object.keys(currentFunction['definition']['interfaces']);
                    if (interfaces && interfaces.length > 0) {
                        const interfaceName = interfaces[0];
                        if (nodeTemplate['interfaces'][interfaceName]['operations'] &&
                            nodeTemplate['interfaces'][interfaceName]['operations']['process']
                        ) {
                            if (nodeTemplate['interfaces'][interfaceName]['operations']['process']['inputs']) {
                                /* tslint:disable:no-string-literal */
                                console.log(Object.keys(nodeTemplate['interfaces'][interfaceName]['operations']['process']['inputs']));
                            }
                            if (nodeTemplate['interfaces'][interfaceName]['operations']['process']['outputs']) {
                                /* tslint:disable:no-string-literal */
                                console.log(Object.keys(nodeTemplate['interfaces'][interfaceName]['operations']['process']['outputs']));
                            }

                        }
                    }
                }
            });
        console.log(nodeTemplate);
    }

    printSomethings() {
        console.log('something');
    }
}
