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
    steps: string[];
    suggestedInputs: string[] = [];
    suggestedOutputs: string[] = [];

    tempInputs: string[] = [];
    tempOutputs: string[] = [];
    currentInterfaceName: string;

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
                    this.steps = steps;
                    this.suggestedOutputs = [];
                    this.suggestedInputs = [];
                    /*steps.forEach(step => {
                        const target = action.steps[step].target;
                        this.getInputs(target);
                    });*/
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
        console.log(output);
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

    setInputAndOutputs(targetName) {
        console.log(targetName);
        const nodeTemplate = this.designerState.template.node_templates[targetName];
        console.log(this.designerState.template.node_templates);
        console.log(nodeTemplate);
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
                        console.log(interfaceName);
                        this.currentInterfaceName = interfaceName;

                        if (nodeTemplate['interfaces'] &&
                            nodeTemplate['interfaces'][interfaceName]['operations'] &&
                            nodeTemplate['interfaces'][interfaceName]['operations']['process']
                        ) {
                            console.log('here');
                            if (nodeTemplate['interfaces'][interfaceName]['operations']['process']['inputs']) {
                                /* tslint:disable:no-string-literal */
                                this.suggestedInputs = Object.keys(nodeTemplate['interfaces']
                                    [interfaceName]['operations']['process']['inputs']);
                                console.log(this.suggestedInputs);
                            }
                            if (nodeTemplate['interfaces'][interfaceName]['operations']['process']['outputs']) {
                                /* tslint:disable:no-string-literal */
                                this.suggestedOutputs = Object.keys(nodeTemplate['interfaces']
                                    [interfaceName]['operations']['process']['outputs']);
                                console.log(this.suggestedInputs);
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

    addTempInput(suggestedInput: string) {
        this.addAttribute(this.tempInputs, suggestedInput);
        this.deleteAttribute(this.suggestedInputs, suggestedInput);
    }

    addTempOutput(suggestedOutput: string) {
        this.addAttribute(this.tempOutputs, suggestedOutput);
        this.deleteAttribute(this.suggestedOutputs, suggestedOutput);
    }

    deleteAttribute(container: string[], suggestedAttribute: string) {
        if (container && suggestedAttribute && container.includes(suggestedAttribute)) {
            const index: number = container.indexOf(suggestedAttribute);
            if (index !== -1) {
                container.splice(index, 1);
            }
        }
    }

    addAttribute(container: string[], suggestedAttribute: string) {
        if (container && suggestedAttribute && !container.includes(suggestedAttribute)) {
            container.push(suggestedAttribute);
        }
    }


    submitTempAttributes() {
        if (this.tempInputs && this.tempInputs.length > 0) {
            let newInputs = '';
            this.tempInputs.forEach(tempAttribute => {
                const currentInputNode: string = this.designerState.template.node_templates[this.actionName]['interfaces']
                    [this.currentInterfaceName]['operations']['process']['inputs'][tempAttribute];
                const currentInputNodeAsString = JSON.stringify(currentInputNode);
                newInputs += '"' + tempAttribute + '": ' + currentInputNodeAsString + ',';

            });
            if (newInputs.endsWith(',')) {
                newInputs = newInputs.substr(0, newInputs.length - 1);
            }
            const originalInputs = JSON.stringify(this.designerState.template.workflows[this.actionName]['inputs']);
            console.log(originalInputs.substr(0, originalInputs.length - 1) + ',' + newInputs + '}');
            this.designerState.template.workflows[this.actionName]['inputs'] =
                JSON.parse(originalInputs.substr(0, originalInputs.length - 1) + ',' + newInputs + '}');
        }

        if (this.tempOutputs && this.tempOutputs.length > 0) {
            this.tempOutputs.forEach(tempAttribute => {
                const currentOutputNode = this.designerState.template.node_templates[this.actionName]['interfaces']
                    [this.currentInterfaceName]['operations']['process']['outputs'][tempAttribute];
                console.log(currentOutputNode);
            });
        }
    }
}
