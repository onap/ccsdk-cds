import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {PackageCreationStore} from '../../package-creation.store';
import {Mapping, Template} from '../../mapping-models/CBAPacakge.model';
import {TemplateInfo, TemplateStore} from '../../template.store';
import {TemplateAndMapping} from '../TemplateAndMapping';

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

    constructor(private packageCreationStore: PackageCreationStore, private templateStore: TemplateStore) {
    }

    ngOnInit() {
        this.packageCreationStore.state$.subscribe(cba => {
            if (cba.templates) {
                this.templates = cba.templates;
                this.mapping = cba.mapping;
                let templateAndMapping;
                this.templateAndMappingMap.clear();
                this.templates.files.forEach((value, key) => {
                    templateAndMapping = new TemplateAndMapping();
                    templateAndMapping.isTemplate = true;
                    const isFromTemplate = true;
                    this.setIsMappingOrTemplate(key, templateAndMapping, isFromTemplate);
                });
                this.mapping.files.forEach((value, key) => {
                    console.log('there is a mapping here' + key);
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
        if (this.templateAndMappingMap.has(nameOfFile)) {
            console.log('found' + nameOfFile);

            const templateAndMappingExisted = this.templateAndMappingMap.get(nameOfFile);
            console.log('is template' + templateAndMappingExisted.isTemplate);
            console.log('is mapping' + templateAndMappingExisted.isMapping);
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
        this.packageCreationStore.state$.subscribe(cba => {
            if (cba.templates) {
                const fileContent = cba.templates.getValue(key);
                const templateInfo = new TemplateInfo();
                templateInfo.fileContent = fileContent;
                templateInfo.fileName = key;
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
