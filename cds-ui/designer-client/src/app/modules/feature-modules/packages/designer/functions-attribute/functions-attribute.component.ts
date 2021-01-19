import { Component, EventEmitter, OnDestroy, OnInit, Output } from '@angular/core';
import { DesignerStore } from '../designer.store';
import { PackageCreationStore } from '../../package-creation/package-creation.store';
import { Subject } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { CBAPackage } from '../../package-creation/mapping-models/CBAPacakge.model';
import { TemplateAndMapping } from '../../package-creation/template-mapping/TemplateAndMapping';
import { FunctionsStore } from '../functions.store';
import { NodeProcess, NodeTemplate } from '../model/desinger.nodeTemplate.model';
import { DesignerDashboardState } from '../model/designer.dashboard.state';
import { Action } from '../action-attributes/models/Action';

@Component({
    selector: 'app-functions-attribute',
    templateUrl: './functions-attribute.component.html',
    styleUrls: ['./functions-attribute.component.css']
})
export class FunctionsAttributeComponent implements OnInit, OnDestroy {

    ngUnsubscribe = new Subject();
    designerDashboardState: DecodeSuccessCallback;
    cbaPackage: CBAPackage;
    templateAndMappingMap = new Map<string, TemplateAndMapping>();
    selectedTemplates = new Map<string, TemplateAndMapping>();
    finalTemplates = new Map<string, TemplateAndMapping>();
    fileToDelete: string;
    requiredInputs = new Map<string, {}>();
    requiredOutputs = new Map<string, {}>();
    OptionalInputs = new Map<string, {}>();
    optionalOutputs = new Map<string, {}>();
    artifactPrefix = false;
    currentFuncion = new NodeProcess();
    nodeTemplates = new NodeTemplate('');
    designerState: DesignerDashboardState;
    actionName = '';
    functionName = '';
    interfaceChildName = '';
    @Output() saveEvent = new EventEmitter<string>();


    constructor(
        private designerStore: DesignerStore,
        private packageCreationStore: PackageCreationStore,
        private functionStore: FunctionsStore
    ) {
    }

    ngOnInit() {
        this.designerStore.state$.subscribe(designerDashboardState => {
            this.designerState = designerDashboardState;
            this.actionName = this.designerState.actionName;
            const action = this.designerState.template.workflows[this.actionName] as Action;
            this.currentFuncion = new NodeProcess();
            try {
                console.log(action);
                if (action) {
                    // this.designerState.functionName
                    const child = Object.keys(action.steps)[0];
                    this.functionName = this.designerState.functionName;
                    console.log(this.designerState.template.node_templates);
                    console.log(this.designerState);
                    console.log(this.designerState.template.node_templates[this.functionName]);
                    //  this.currentFuncion = this.designerState.template.node_templates[this.functionName];
                    // reset inouts&outputs
                    this.requiredInputs = new Map<string, {}>();
                    this.requiredOutputs = new Map<string, {}>();
                    this.OptionalInputs = new Map<string, {}>();
                    this.optionalOutputs = new Map<string, {}>();
                    this.toNodeProcess(this.designerState.template.node_templates[this.functionName], this.functionName);
                    const type = this.designerState.template.node_templates[this.functionName].type;
                    this.getNodeType(type);
                    this.onInitMapping();
                }
            } catch (e) { }
        });

        this.packageCreationStore.state$
            .subscribe(cbaPackage => {
                this.cbaPackage = cbaPackage;
                console.log('File name =>================== ');
                console.log(this.cbaPackage.templates.files);
                this.cbaPackage.templates.files.forEach((value, key) => {
                    console.log('File name => ' + key);
                    const templateAndMapping = new TemplateAndMapping();
                    templateAndMapping.isTemplate = true;
                    const isFromTemplate = true;
                    this.setIsMappingOrTemplate(key, templateAndMapping, isFromTemplate);
                });

                this.cbaPackage.mapping.files.forEach((value, key) => {
                    const templateAndMapping = new TemplateAndMapping();
                    templateAndMapping.isMapping = true;
                    const isFromTemplate = false;
                    this.setIsMappingOrTemplate(key, templateAndMapping, isFromTemplate);
                });
            });

    }



    onInitMapping() {
        // selectedTemplates , templateAndMappingMap
        // this.selectedTemplates = new Map<string, TemplateAndMapping>();
        try {
            const functionMap = this.designerState.template.node_templates[this.functionName].artifacts;
            console.log(this.templateAndMappingMap);

            Object.keys(functionMap).forEach((file) => {
                const filename = file.substring(0, file.lastIndexOf('-'));
                console.log(filename);
                if (this.templateAndMappingMap.has(filename)) {
                    this.selectedTemplates.set(filename, this.templateAndMappingMap.get(filename));
                    this.finalTemplates.set(filename, this.templateAndMappingMap.get(filename));
                }
            });


        } catch (e) { }
    }

    init() {
        this.selectedTemplates = new Map(this.finalTemplates);
    }


    toNodeProcess(nodeTemplate, functionName) {
        console.log(nodeTemplate);
        this.currentFuncion['instance-name'] = functionName;
        // tslint:disable-next-line: no-string-literal
        this.currentFuncion['type'] = nodeTemplate['type'];
        if (nodeTemplate.interfaces && Object.keys(nodeTemplate.interfaces).length > 0) {
            const nodeName = Object.keys(nodeTemplate.interfaces)[0];
            // console.log(Object.keys(nodeTemplate.interfaces));
            // tslint:disable-next-line: no-string-literal
            const inputs = nodeTemplate.interfaces[nodeName]['operations']['process']['inputs'];
            // tslint:disable-next-line: no-string-literal
            const outputs = nodeTemplate.interfaces[nodeName]['operations']['process']['outputs'];

            // console.log(inputs);

            if (inputs) {
                for (const [key, value] of Object.entries(inputs)) {
                    console.log(key + ' - ' + value);
                    /* if (typeof value === 'object' || this.isValidJson(value)) {
                         this.currentFuncion.inputs[key] = JSON.stringify(value);
                     } else {*/
                    this.currentFuncion.inputs[key] = value;
                    // }
                }
            }
            if (outputs) {
                for (const [key, value] of Object.entries(outputs)) {
                    console.log(key + '-' + value);
                    this.currentFuncion.outputs[key] = value;
                }
            }
        }
    }

    isValidJson(val) {
        console.log(val);
        try {
            JSON.parse(val + '');
            return true;
        } catch (e) {
            console.log(e);
        }
        return false;
    }

    jsonToStr(json) {
        return JSON.stringify(json);
    }

    bind(key, e) {
        const val = JSON.parse(e.target.value);
        this.currentFuncion.inputs[key] = {
            ...val
        };
    }
    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    addTemplates() {
        this.finalTemplates = new Map(this.selectedTemplates);
        this.saveFunctionData();
    }
    cancel() {
        this.selectedTemplates = new Map<string, TemplateAndMapping>();
    }

    setProcessAttr(isInput, key, e) {
        console.log(e.target.value);
        // tslint:disable-next-line: no-string-literal
        if (isInput) {
            this.currentFuncion['inputs'][key] = e.target.value;
        } else {
            this.currentFuncion['outputs'][key] = e.target.value;
        }


        this.saveFunctionData();
    }

    saveFunctionData() {
        this.nodeTemplates = new NodeTemplate('');
        // tslint:disable-next-line: variable-name
        const node_templates = {};
        const finalFunctionData = this.currentFuncion;
        // tslint:disable-next-line: no-string-literal
        const type = finalFunctionData['type'];
        const instanceName = finalFunctionData['instance-name'];
        finalFunctionData.inputs['artifact-prefix-names'] = [];

        // insert selected templates in nodeTemplates.artifacts
        this.selectedTemplates.forEach((value, key) => {
            console.log(key);
            console.log(value);
            console.log(finalFunctionData.inputs['artifact-prefix-names']);

            if (finalFunctionData.inputs['artifact-prefix-names'] === undefined) {
                finalFunctionData.inputs['artifact-prefix-names'] = [key];
            } else if (
                Array.isArray(finalFunctionData.inputs['artifact-prefix-names']) &&
                !finalFunctionData.inputs['artifact-prefix-names'].includes(key)
            ) {
                finalFunctionData.inputs['artifact-prefix-names'].push(key);
            }

            if (value.isMapping) {
                this.nodeTemplates.artifacts[key + '-mapping'] = {
                    type: 'artifact-mapping-resource',
                    file: 'Templates/' + key + '-mapping.json'
                };
            }

            if (value.isTemplate) {
                this.nodeTemplates.artifacts[key + '-template'] = {
                    type: 'artifact-template-velocity',
                    file: 'Templates/' + key + '-template.vtl'
                };
            }
        });
        // instantiate the final node_template object to save
        this.nodeTemplates.type = type;
        delete this.nodeTemplates.properties;
        node_templates[finalFunctionData['instance-name']] = this.nodeTemplates;

        delete finalFunctionData['instance-name'];
        // tslint:disable-next-line: no-string-literal
        delete finalFunctionData['type'];

        if (finalFunctionData.outputs === {} || Object.keys(finalFunctionData.outputs).length <= 0) {
            delete finalFunctionData.outputs;
        }

        this.nodeTemplates.interfaces = {
            [this.interfaceChildName]: {
                operations: {
                    process: {
                        ...finalFunctionData,
                    }
                }
            }
        };
        console.log(finalFunctionData);
        console.log(node_templates);
        // save function to store
        // tslint:disable-next-line: no-unused-expression
        this.designerStore.addNodeTemplate(instanceName, type, node_templates[instanceName]);
        // create a new package
        //  this.saveEvent.emit('save');
    }
    // Template logic
    private setIsMappingOrTemplate(key: string, templateAndMapping: TemplateAndMapping, isFromTemplate: boolean) {
        const nameOfFile = isFromTemplate ?
            key.split('/')[1].split('.')[0].split('-template')[0]
            : key.split('/')[1].split('.')[0].split('-mapping')[0];
        // const fullName = nameOfFile + ',' + key.split('.');
        if (this.templateAndMappingMap.has(nameOfFile)) {
            const templateAndMappingExisted = this.templateAndMappingMap.get(nameOfFile);
            !isFromTemplate ? templateAndMappingExisted.isMapping = true : templateAndMappingExisted.isTemplate = true;
            this.templateAndMappingMap.set(nameOfFile, templateAndMappingExisted);
        } else {
            this.templateAndMappingMap.set(nameOfFile, templateAndMapping);
        }

    }

    setArtifact(predefined: boolean) {
        if (predefined) {
            this.currentFuncion.inputs['artifact-prefix-names'] = [];
        } else {
            this.currentFuncion.inputs['artifact-prefix-names'] = { get_input: 'template-prefix' };
        }
    }
    addToInputs(optionalInput) {
        this.requiredInputs.set(optionalInput, this.OptionalInputs.get(optionalInput));
        this.OptionalInputs.delete(optionalInput);
    }

    setTemplate(file: string) {
        if (this.selectedTemplates.has(file)) {
            console.log('Not exist');
            this.selectedTemplates.delete(file);
            this.finalTemplates.delete(file);

        } else {
            this.selectedTemplates.set(file, this.templateAndMappingMap.get(file));
        }
        console.log(this.selectedTemplates);
    }

    getKeys(map: Map<string, any>) {
        return Array.from(map.keys());
    }
    getValue(file: string, map: Map<string, any>) {
        return map.get(file);
    }

    getObjectKey(object) {
        // console.log(object);
        return Object.keys(object);
    }
    getObjectValue(object) {
        return Object.values(object);
    }

    getNodeType(nodeName: string) {
        this.functionStore.state$
            .subscribe(state => {
                console.log(state);
                console.log(nodeName);
                const functions = state.serverFunctions;
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < functions.length; i++) {
                    if (functions[i].modelName === nodeName) {
                        // tslint:disable: no-string-literal
                        console.log(functions[i].definition['interfaces']);
                        this.getInputFields(functions[i].definition['interfaces'], 'outputs');
                        this.getInputFields(functions[i].definition['interfaces'], 'inputs');
                        break;
                    }
                }
            });
    }

    getInputFields(interfaces, type) {

        if (type === 'inputs') {
            this.requiredInputs = new Map<string, {}>();
            this.OptionalInputs = new Map<string, {}>();
        } else {
            this.requiredOutputs = new Map<string, {}>();
            this.optionalOutputs = new Map<string, {}>();

        }
        const nodeName = Object.keys(interfaces)[0];
        this.interfaceChildName = nodeName;
        console.log(nodeName + ' ------ ' + type);
        console.log(interfaces[nodeName]['operations']['process'][type]);
        const fields = interfaces[nodeName]['operations']['process'][type];
        this.artifactPrefix = false;
        for (const [key, value] of Object.entries(fields)) {
            if (key === 'artifact-prefix-names') {
                this.artifactPrefix = true;
                // in edit&view mode need to check first if this input||output already exists
            } else if (key in this.currentFuncion.inputs) {
                this.requiredInputs.set(key, Object.assign({}, value));
            } else if (key in this.currentFuncion.outputs) {
                this.requiredOutputs.set(key, Object.assign({}, value));
            } else if (value['required']) {
                console.log('This field is required = ' + key);
                if (type === 'inputs') {
                    this.requiredInputs.set(key, Object.assign({}, value));
                } else {
                    this.requiredOutputs.set(key, Object.assign({}, value));
                }
            } else {
                console.log('This field is Optional ' + key);
                if (type === 'inputs') {
                    this.OptionalInputs.set(key, Object.assign({}, value));
                } else {
                    this.optionalOutputs.set(key, Object.assign({}, value));
                }
            }
        }

        // console.log(this.requiredOutputs);
    }


}
