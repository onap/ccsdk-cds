import { Component, OnDestroy, OnInit } from '@angular/core';
import { DesignerStore } from '../designer.store';
import { PackageCreationStore } from '../../package-creation/package-creation.store';
import { Subject } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { CBAPackage } from '../../package-creation/mapping-models/CBAPacakge.model';
import { TemplateAndMapping } from '../../package-creation/template-mapping/TemplateAndMapping';
import { FunctionsStore } from '../functions.store';

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
    fileToDelete: string;
    requiredInputs = [];
    requiredOutputs = [];
    OptionalInputs = [];
    optionalOutputs = [];
    artifactPrefix = false;

    constructor(
        private designerStore: DesignerStore,
        private packageCreationStore: PackageCreationStore,
        private functionStore: FunctionsStore
    ) {
    }

    ngOnInit() {
        this.designerStore.state$
            .pipe(
                distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntil(this.ngUnsubscribe))
            .subscribe(designerDashboardState => {
                this.designerDashboardState = designerDashboardState;
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
        this.getNodeType('component-resource-resolution');

    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
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

    addTemplates() { }

    setTemplate(file: string) {
        if (this.selectedTemplates.has(file)) {
            this.selectedTemplates.delete(file);
        } else {
            this.selectedTemplates.set(file, this.templateAndMappingMap.get(file));
        }
        console.log(this.selectedTemplates);
    }

    getKeys(templateAndMappingMap: Map<string, TemplateAndMapping>) {
        return Array.from(templateAndMappingMap.keys());
    }
    getValue(file: string) {
        return this.templateAndMappingMap.get(file);
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
                const functions = state.serverFunctions;
                // tslint:disable-next-line: prefer-for-of
                for (let i = 0; i < functions.length; i++) {
                    if (functions[i].modelName === nodeName) {
                        // tslint:disable: no-string-literal
                        console.log(functions[i].definition['interfaces']);
                        this.getInputFields(functions[i].definition['interfaces'], 'ResourceResolutionComponent', 'inputs');
                        this.getInputFields(functions[i].definition['interfaces'], 'ResourceResolutionComponent', 'outputs');
                        break;
                    }
                }
            });
    }

    getInputFields(interfaces, nodeName, type) {
        console.log(interfaces[nodeName]['operations']['process'][type]);
        const fields = interfaces[nodeName]['operations']['process'][type];

        for (const [key, value] of Object.entries(fields)) {
            const object = {};
            object[key] = value;

            if (key === 'artifact-prefix-names') {
                this.artifactPrefix = true;
            } else if (value['required']) {
                console.log('This field is required = ' + key);
                if (type === 'inputs') {
                    this.requiredInputs.push(Object.assign({}, object));
                } else {
                    this.requiredOutputs.push(Object.assign({}, object));
                }
            } else {
                console.log('This field is Optional ' + key);
                if (type === 'inputs') {
                    this.OptionalInputs.push(Object.assign({}, object));
                } else {
                    this.optionalOutputs.push(Object.assign({}, object));
                }
            }
        }

        // console.log(this.requiredOutputs);
    }


}
