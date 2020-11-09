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
    newInputs = [];
    newOutputs = [];
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
    functionAndAttributesInput: Map<string, string[]> = new Map<string, string[]>();
    private currentTargetFunctionName: any;
    private functionAndAttributesOutput: Map<string, string[]> = new Map<string, string[]>();
    suggestedAttributes: string[] = [];
    selectedFunctionName = '';
    selectedAttributeName = '';

    constructor(private designerStore: DesignerStore, private functionsStore: FunctionsStore) {

    }

    ngOnInit() {
        this.designerStore.state$.subscribe(designerState => {
            this.designerState = designerState;
            if (this.designerState && this.designerState.actionName) {
                this.actionName = this.designerState.actionName;
                console.log(this.actionName);
                const action = this.designerState.template.workflows[this.actionName] as Action;
                if (action.steps) {
                    const steps = Object.keys(action.steps);
                    this.isFunctionAttributeActive = steps && steps.length > 0;
                    this.steps = steps;
                    this.suggestedOutputs = [];
                    this.suggestedInputs = [];
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
            this.newInputs.push(insertedInputActionAttribute);
        }
    }

    addOutput(output: OutputActionAttribute) {
        console.log(output);
        if (output && output.type && output.name) {
            const insertedOutputActionAttribute = Object.assign({}, output);
            this.newOutputs.push(insertedOutputActionAttribute);
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
        if (this.selectedFunctionName && this.selectedAttributeName) {
            this.outputActionAttribute.value =
                '["' + this.selectedFunctionName + '","' + this.selectedAttributeName + '"]';
        }
        this.addOutput(this.outputActionAttribute);
        this.clearFormInputs();
        this.storeOutputs(this.newOutputs);
        this.storeInputs((this.newInputs));
        this.newInputs.forEach(input => {
            this.inputs.push(input);
        });

        this.newOutputs.forEach(output => {
            this.outputs.push(output);
        });
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
        this.writeAttribute(inputs, 'inputs');
    }

    private storeOutputs(OutputActionAttributes: OutputActionAttribute[]) {
        let outputs = '';
        OutputActionAttributes.forEach(output => {
            outputs += this.appendOutputAttributes(output);
        });
        this.writeAttribute(outputs, 'outputs');
    }

    private appendAttributes(inputActionAttribute: InputActionAttribute) {
        return '"' + inputActionAttribute.name + '" : {\n' +
            '            "required" : ' + inputActionAttribute.required + ',\n' +
            '            "type" : "' + inputActionAttribute.type + '",\n' +
            '            "description" : "' + inputActionAttribute.description + '"\n' +
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

                        if (!this.functionAndAttributesInput.has(targetName)) {
                            this.currentTargetFunctionName = targetName;
                            this.functionAndAttributesInput.set(targetName, []);
                        }

                        if (!this.functionAndAttributesOutput.has(targetName)) {
                            this.currentTargetFunctionName = targetName;
                            this.functionAndAttributesOutput.set(targetName, []);
                        }

                        if (nodeTemplate['interfaces'] &&
                            nodeTemplate['interfaces'][interfaceName]['operations'] &&
                            nodeTemplate['interfaces'][interfaceName]['operations']['process']
                        ) {
                            console.log('here');
                            if (nodeTemplate['interfaces'][interfaceName]['operations']['process']['inputs']) {
                                /* tslint:disable:no-string-literal */
                                this.suggestedInputs = Object.keys(nodeTemplate['interfaces']
                                    [interfaceName]['operations']['process']['inputs']);
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
        this.addAttribute(this.functionAndAttributesInput.get(this.currentTargetFunctionName), suggestedInput);
    }

    addTempOutput(suggestedOutput: string) {
        this.addAttribute(this.tempOutputs, suggestedOutput);
        this.deleteAttribute(this.suggestedOutputs, suggestedOutput);
        this.addAttribute(this.functionAndAttributesOutput.get(this.currentTargetFunctionName), suggestedOutput);
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
        this.writeSelectedAttribute(this.functionAndAttributesInput, 'inputs');
        this.writeSelectedAttribute(this.functionAndAttributesOutput, 'outputs');
    }

    private writeSelectedAttribute(map: Map<string, string[]>, attributeType: string) {
        map.forEach((value, key) => {
            const nodeTemplate = this.getNodeTemplate(key);
            this.functions.serverFunctions
                /* tslint:disable:no-string-literal */
                .filter(currentFunction => currentFunction.modelName.includes(nodeTemplate['type']))
                .forEach(currentFunction => {

                    if (currentFunction['definition'] && currentFunction['definition']['interfaces']
                        [Object.keys(currentFunction['definition'] && currentFunction['definition']['interfaces'])]
                        ['operations']['process'][attributeType]) {
                        let newAttributes = '';
                        const attributes = currentFunction['definition'] && currentFunction['definition']['interfaces']
                            [Object.keys(currentFunction['definition'] && currentFunction['definition']['interfaces'])]
                            ['operations']['process'][attributeType];
                        value.forEach(attribute => {
                            newAttributes += '"' + attribute + '": ' + this.convertToString(attributes[attribute]) + ',';
                        });
                        if (value.length > 0) {
                            this.writeAttribute(newAttributes, attributeType);
                        }
                    }
                });
        });
    }

    private writeAttribute(newAttributes: string, attributeType: string) {
        newAttributes = this.removeTheLastComma(newAttributes);
        const originalAttributes = this.convertToString(this.designerState.template.workflows[this.actionName]
            [attributeType]);
        console.log(originalAttributes.substr(0, originalAttributes.length - 1) + ',' + newAttributes + '}');
        this.designerState.template.workflows[this.actionName][attributeType] =
            this.convertToObject(originalAttributes.substr(0, originalAttributes.length - 1)
                + ',' + newAttributes + '}');
    }

    private removeTheLastComma = (newInputs: string) => {
        if (newInputs.endsWith(',')) {
            newInputs = newInputs.substr(0, newInputs.length - 1);
        }
        return newInputs;
    }

    private convertToString = object => JSON.stringify(object);

    private convertToObject = stringValue => JSON.parse(stringValue);

    private getNodeTemplate = (value: string) => this.designerState.template.node_templates[value];

    getAttributesAndOutputs(functionName: string) {
        this.suggestedAttributes = [];
        console.log(functionName);
        const nodeTemplate = this.designerState.template.node_templates[functionName];
        console.log(this.designerState.template.node_templates);
        console.log(nodeTemplate);
        /* tslint:disable:no-string-literal */
        console.log(nodeTemplate['type']);
        this.functions.serverFunctions
            /* tslint:disable:no-string-literal */
            .filter(currentFunction => currentFunction.modelName.includes(nodeTemplate['type']))
            .forEach(currentFunction => {
                if (currentFunction.definition['attributes']) {
                    Object.keys(currentFunction.definition['attributes']).forEach(attribute => {
                        this.suggestedAttributes.push(attribute);
                    });
                }
                console.log(this.suggestedAttributes);
                this.selectedFunctionName = functionName;
            });
    }

    addTempOutputAttr(suggestedOutputAndAttribute: string) {
        this.selectedAttributeName = suggestedOutputAndAttribute;
    }


    private appendOutputAttributes(output: OutputActionAttribute) {
        return '"' + output.name + '" : {\n' +
            '            "required" : ' + output.required + ',\n' +
            '            "type" : "' + output.type + '",\n' +
            '            "description" : "' + output.description + '",\n' +
            '            "value\" :' + '{\n' +
            '             "get_attribute" : ' + output.value + '\n' +
            '            }\n' +
            '          },';

    }
}
