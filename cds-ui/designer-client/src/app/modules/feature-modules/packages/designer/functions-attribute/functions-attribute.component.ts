import { Component, OnDestroy, OnInit } from '@angular/core';
import { DesignerStore } from '../designer.store';
import { PackageCreationStore } from '../../package-creation/package-creation.store';
import { Subject } from 'rxjs';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { CBAPackage } from '../../package-creation/mapping-models/CBAPacakge.model';
import { TemplateAndMapping } from '../../package-creation/template-mapping/TemplateAndMapping';

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

    constructor(
        private designerStore: DesignerStore,
        private packageCreationStore: PackageCreationStore
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
            .pipe(
                distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntil(this.ngUnsubscribe))
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
}
