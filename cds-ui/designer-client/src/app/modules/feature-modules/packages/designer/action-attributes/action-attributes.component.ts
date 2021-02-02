import {Component, OnInit} from '@angular/core';
import {InputActionAttribute, OutputActionAttribute} from './models/InputActionAttribute';
import {DesignerStore} from '../designer.store';
import {DesignerDashboardState} from '../model/designer.dashboard.state';
import {Action} from './models/Action';
import {FunctionsStore} from '../functions.store';
import {FunctionsState} from '../model/functions.state';
import {PackageCreationStore} from '../../package-creation/package-creation.store';
import {CBAPackage} from '../../package-creation/mapping-models/CBAPacakge.model';

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
    isNotComponentResourceResolution = true;
    currentArtifacts: string[] = [];
    isParametersHidden = true;
    cbaPackage: CBAPackage;
    suggestedMappingParameters: string[] = [];
    selectedParameterList: string[] = [];
    currentSuggestedArtifact: string;
    suggestedDeletedInput: any = {};
    suggestedEditedAttribute: any = {};

    constructor(private designerStore: DesignerStore,
                private functionsStore: FunctionsStore,
                private packageCreationStore: PackageCreationStore) {

    }

    ngOnInit() {
        console.log('is paramters hidden' + this.isParametersHidden);
        console.log('is artifacts hidden' + this.isNotComponentResourceResolution);
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

        this.packageCreationStore.state$
            .subscribe(cbaPackage => {
                this.cbaPackage = cbaPackage;

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
            fieldAttribute.value = container[nameOutput].value;
            console.log(fieldAttribute.value);
            const insertedOutputActionAttribute = Object.assign({}, fieldAttribute);
            fields.push(insertedOutputActionAttribute);
        }
        return fields;
    }

    addInput(input: InputActionAttribute) {
        console.log(input);
        if (input && input.type && input.name) {
            const insertedInputActionAttribute = Object.assign({}, input);
            if (!this.newInputs.some(obj => obj.name === input.name)) {
                this.newInputs.push(insertedInputActionAttribute);
            }
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
            console.log(this.getValue());
            this.outputActionAttribute.value =
                this.getValue();
        }
        this.addOutput(this.outputActionAttribute);
        this.clearFormInputs();
        this.storeOutputs(this.newOutputs);
        this.storeInputs((this.newInputs));
        this.newInputs.forEach(input => {
            if (!this.inputs.includes(input.name)) {
                this.inputs.push(input);
            }
        });

        this.newOutputs.forEach(output => {
            if (!this.outputs.includes(output)) {
                this.outputs.push(output);
            }
        });
        this.newInputs = [];
        this.newOutputs = [];
    }

    private getValue() {
        let value = '["' + this.selectedFunctionName + '", "' + '","' + this.selectedAttributeName;

        if (!this.isParametersHidden) {
            let currentSelected = '';
            for (const selectedParameter of this.selectedParameterList) {
                currentSelected += '"' + selectedParameter + '",';
            }
            value += '","' + this.currentSuggestedArtifact + '",'
                + currentSelected.substr(0, currentSelected.length - 2) + '';
        } else if (!this.isNotComponentResourceResolution && this.currentSuggestedArtifact) {
            value += '","' + this.currentSuggestedArtifact + '';

        }

        return value += '"]';
    }

    public clearFormInputs() {
        console.log('trying to clear ');
        this.inputActionAttribute = new InputActionAttribute();
        this.outputActionAttribute = new OutputActionAttribute();
        this.outputOtherType = '';
        this.inputOtherType = '';
    }

    private storeInputs(InputActionAttributes: InputActionAttribute[]) {

        let inputs = '';
        InputActionAttributes.forEach(input => {
            inputs += this.appendAttributes(input);
            console.log(inputs);
        });
        if (inputs.length > 0) {
            this.writeAttribute(inputs, 'inputs');
        }
    }

    private storeOutputs(OutputActionAttributes: OutputActionAttribute[]) {
        let outputs = '';
        OutputActionAttributes.forEach(output => {
            outputs += this.appendOutputAttributes(output);
        });
        if (outputs.length > 0) {
            this.writeAttribute(outputs, 'outputs');
        }

    }

    private appendAttributes(inputActionAttribute: InputActionAttribute) {
        const entrySchema: string = this.getEntrySchema(inputActionAttribute);
        const input = '"' + inputActionAttribute.name + '" : {\n' +
            '            "required" : ' + inputActionAttribute.required + ',\n' +
            '            "type" : "' + inputActionAttribute.type + '",\n' +
            '            "description" : "' + inputActionAttribute.description + '"';
        return input + entrySchema;
    }

    private getEntrySchema(inputActionAttribute: InputActionAttribute) {
        return inputActionAttribute.type.includes('list') ?
            ',\n\t' + '"entry_schema" : {\n' +
            '              "type" : "string"\n' +
            '            }\n},'
            :
            '\n },';
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
                                this.filterTwoCollections(this.suggestedInputs, this.inputs);

                            }
                            if (nodeTemplate['interfaces'][interfaceName]['operations']['process']['outputs']) {
                                /* tslint:disable:no-string-literal */
                                this.suggestedOutputs = Object.keys(nodeTemplate['interfaces']
                                    [interfaceName]['operations']['process']['outputs']);
                                this.filterTwoCollections(this.suggestedOutputs, this.outputs);
                            }

                        }
                    }
                }
            });
        console.log(nodeTemplate);
    }

    private filterTwoCollections(suggestedInputs: string[], inputs: any[]) {
        inputs.forEach(input => {
            if (suggestedInputs.includes(input.name)) {
                this.deleteAttribute(suggestedInputs, input.name);
            }
        });
    }

    printSomethings() {
        this.setInputAndOutputs(
            this.designerState.template.workflows[this.actionName]['steps'][this.steps[0]]['target']
        );
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
            console.log(index);
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
        console.log(newAttributes);
        let originalAttributes = this.convertToString(this.designerState.template.workflows[this.actionName]
            [attributeType]);
        this.createAttributeTypeIfNotExisted(originalAttributes, attributeType);
        originalAttributes = this.convertToString(this.designerState.template.workflows[this.actionName]
            [attributeType]);
        if (originalAttributes.length > 2) {
            this.designerState.template.workflows[this.actionName][attributeType] =
                this.convertToObject(originalAttributes.substr(0, originalAttributes.length - 1)
                    + ',' + newAttributes + '}');
        } else {
            this.designerState.template.workflows[this.actionName][attributeType] =
                this.convertToObject(originalAttributes.substr(0, originalAttributes.length - 1)
                    + newAttributes + '}');
        }

    }

    private removeTheLastComma(newInputs: string): string {
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
        if (nodeTemplate['type'].includes('component-resource-resolution')) {
            this.isNotComponentResourceResolution = false;
            this.isParametersHidden = true;
        } else {
            this.isNotComponentResourceResolution = true;
            this.isParametersHidden = true;
        }
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
        console.log('ssss');
        this.selectedAttributeName = suggestedOutputAndAttribute;
        this.currentArtifacts = [];
        const nodeTemplate = this.designerState.template.node_templates[this.selectedFunctionName];
        if (nodeTemplate['artifacts']
        ) {
            Object.keys(nodeTemplate['artifacts']).forEach(key => {
                const mappingName = key.split('-')[0];
                if (!this.currentArtifacts.includes(mappingName)) {
                    this.currentArtifacts.push(mappingName);
                }
            });
        }
        console.log('happend');

    }


    private appendOutputAttributes(outputActionAttribute: OutputActionAttribute) {
        const entrySchema: string = this.getEntrySchema(outputActionAttribute);
        const output = '"' + outputActionAttribute.name + '" : {\n' +
            '            "required" : ' + outputActionAttribute.required + ',\n' +
            '            "type" : "' + outputActionAttribute.type + '",\n' +
            '            "description" : "' + outputActionAttribute.description + '",\n' +
            '            "value\" :' + '{\n' +
            '             "get_attribute" : ' + outputActionAttribute.value + '\n' +
            '            }\n';

        return output + entrySchema;

    }

    addArtifactFile(suggestedArtifact: string) {
        this.currentSuggestedArtifact = suggestedArtifact;
        this.isParametersHidden = !this.selectedAttributeName.includes('assignment-map');
        if (!this.isParametersHidden) {
            this.suggestedMappingParameters = this.getSuggestedMappingParameters(suggestedArtifact);
        }
    }

    private getSuggestedMappingParameters(suggestedArtifact: string) {
        const suggestedMappingParameters = [];

        this.cbaPackage.mapping.files.forEach(((value, key) => {
            if (key.includes(suggestedArtifact)) {

                JSON.parse(value).forEach(value2 => {
                    suggestedMappingParameters.push(value2['name']);
                });
            }
        }));
        return suggestedMappingParameters;
    }

    addSuggestedMappingParameter(suggestedMappingParameter: string) {
        this.addAttribute(this.selectedParameterList, suggestedMappingParameter);
        this.deleteAttribute(this.suggestedMappingParameters, suggestedMappingParameter);

    }

    editAttribute(input: any) {
        this.suggestedEditedAttribute = input;
    }

    private createAttributeTypeIfNotExisted(originalAttributes: string, attributeType: string) {
        if (!originalAttributes) {
            this.designerState.template.workflows[this.actionName][attributeType] = {};
        }
    }

    private checkIfTypeIsList(type: string) {
        return type.includes('list');
    }

    markDeletedInput(input: any) {
        this.suggestedDeletedInput = input;
    }

    deleteActionAttribute() {
        delete this.designerState.template.workflows[this.actionName]
            ['inputs'][this.suggestedDeletedInput.name];
        this.deleteAttribute(this.inputs, this.suggestedDeletedInput);

        delete this.designerState.template.workflows[this.actionName]
            ['outputs'][this.suggestedDeletedInput.name];
        this.deleteAttribute(this.outputs, this.suggestedDeletedInput);
    }
}
