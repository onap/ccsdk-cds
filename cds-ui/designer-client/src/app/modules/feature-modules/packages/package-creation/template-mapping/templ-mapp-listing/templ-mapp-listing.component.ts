import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { PackageCreationStore } from '../../package-creation.store';
import { Mapping, Template } from '../../mapping-models/CBAPacakge.model';
import { TemplateInfo, TemplateStore } from '../../template.store';
import { TemplateAndMapping } from '../TemplateAndMapping';
import { ActivatedRoute } from '@angular/router';


@Component({
    selector: 'app-templ-mapp-listing',
    templateUrl: './templ-mapp-listing.component.html',
    styleUrls: ['./templ-mapp-listing.component.css']
})
export class TemplMappListingComponent implements OnInit {
    @Output() showCreationViewParentNotification = new EventEmitter<any>();
    private templateAndMappingMap = new Map<string, TemplateAndMapping>();
    private templates: Template;
    private mapping: Mapping;
    isCreate = true;

    constructor(
        private packageCreationStore: PackageCreationStore,
        private templateStore: TemplateStore,
        private route: ActivatedRoute
    ) {
    }

    ngOnInit() {
        if (this.route.snapshot.paramMap.has('id')) {
            this.isCreate = false;
        }
        this.packageCreationStore.state$.subscribe(cba => {
            if (cba.templates) {
                this.templates = cba.templates;
                this.mapping = cba.mapping;
                console.log(this.mapping);
                let templateAndMapping;
                this.templateAndMappingMap.clear();
                this.templates.files.forEach((value, key) => {
                    templateAndMapping = new TemplateAndMapping();
                    templateAndMapping.isTemplate = true;
                    const isFromTemplate = true;
                    this.setIsMappingOrTemplate(key, templateAndMapping, isFromTemplate);
                });
                this.mapping.files.forEach((value, key) => {
                    templateAndMapping = new TemplateAndMapping();
                    templateAndMapping.isMapping = true;
                    const isFromTemplate = false;
                    this.setIsMappingOrTemplate(key, templateAndMapping, isFromTemplate);
                });
                console.log('hello there ');
                console.log(this.templateAndMappingMap);
            }
        });
    }

    private setIsMappingOrTemplate(key: string, templateAndMapping: TemplateAndMapping, isFromTemplate: boolean) {
        const nameOfFile = key.split('/')[1].split('.')[0].split('-')[0];
        // const fullName = nameOfFile + ',' + key.split('.');
        if (this.templateAndMappingMap.has(nameOfFile)) {
            const templateAndMappingExisted = this.templateAndMappingMap.get(nameOfFile);
            !isFromTemplate ? templateAndMappingExisted.isMapping = true : templateAndMappingExisted.isTemplate = true;
            this.templateAndMappingMap.set(nameOfFile, templateAndMappingExisted);
        } else {
            this.templateAndMappingMap.set(nameOfFile, templateAndMapping);
        }

    }

    openCreationView() {
        this.showCreationViewParentNotification.emit('tell parent to open create views');
    }

    setSourceCodeEditor(key: string) {
        const templateKey = 'Templates/' + key + '-template.vtl';
        this.packageCreationStore.state$.subscribe(cba => {
            console.log('cba ------');
            console.log(cba);
            console.log(key);
            console.log(this.templateAndMappingMap);
            if (cba.templates && cba.templates.files.has(templateKey)) {
                const fileContent = cba.templates.getValue(templateKey.trim());
                console.log(fileContent);
                const templateInfo = new TemplateInfo();
                templateInfo.fileContent = fileContent;
                templateInfo.fileName = templateKey;
                this.templateStore.changeTemplateInfo(templateInfo);
            }
            const mappingKey = 'Templates/' + key + '-mapping.json';
            if (cba.mapping && cba.mapping.files.has(mappingKey)) {
                const obj = JSON.parse(cba.mapping.getValue(mappingKey));
                const templateInfo = new TemplateInfo();
                templateInfo.mapping = obj;
                templateInfo.fileName = mappingKey;
                templateInfo.type = 'mapping';
                this.templateStore.changeTemplateInfo(templateInfo);
            }
        });
    }

    getKeys(templateAndMappingMap: Map<string, TemplateAndMapping>) {
        return Array.from(this.templateAndMappingMap.keys());
    }

    getValue(file: string) {
        return this.templateAndMappingMap.get(file);
    }

}
