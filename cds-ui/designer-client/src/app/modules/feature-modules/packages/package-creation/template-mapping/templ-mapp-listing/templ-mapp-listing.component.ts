import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { PackageCreationStore } from '../../package-creation.store';
import { Mapping, Template } from '../../mapping-models/CBAPacakge.model';
import { TemplateInfo, TemplateStore } from '../../template.store';
import { TemplateAndMapping } from '../TemplateAndMapping';
import { ActivatedRoute } from '@angular/router';
import { SharedService } from '../shared-service';
import { TourService } from 'ngx-tour-md-menu';


@Component({
    selector: 'app-templ-mapp-listing',
    templateUrl: './templ-mapp-listing.component.html',
    styleUrls: ['./templ-mapp-listing.component.css']
})
export class TemplMappListingComponent implements OnInit {
    @Output() showCreationView = new EventEmitter<any>();
    @Output() showListView = new EventEmitter<any>();
    templateAndMappingMap = new Map<string, TemplateAndMapping>();
    templates: Template;
    mapping: Mapping;
    isCreate = true;
    currentFile: string;
    edit = false;
    fileToDelete: any = {};

    constructor(
        private packageCreationStore: PackageCreationStore,
        private templateStore: TemplateStore,
        private route: ActivatedRoute,
        private sharedService: SharedService,
        private tourService: TourService,

    ) {
    }

    ngOnInit() {
        if (this.route.snapshot.paramMap.has('id')) {
            this.isCreate = false;
            this.sharedService.isEdit().subscribe(res => {
                this.edit = res;
            });

        }
        this.packageCreationStore.state$.subscribe(cba => {
            if (this.packageCreationStore.state.mapping.files.size > 0 || this.packageCreationStore.state.templates.files.size > 0) {
                this.openListView();
            } else {
                this.openCreationView();
            }
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
                console.log(this.templateAndMappingMap);
            }
            this.deleteFromList();
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

    deleteFromList() {
        this.sharedService.listAction().subscribe(res => {
            console.log('response from actionList');
            console.log(res);
            if (res) {
                console.log('xccccccccccvvvvvv');
                this.templateAndMappingMap.delete(res);
                if (this.templateAndMappingMap.size <= 0) {
                    this.openCreationView();
                }
            }
        });
    }

    createNewTemplate() {
        this.openCreationView();
        this.sharedService.disableEdit();
        this.tourService.goto('tm-templateName');
    }
    openCreationView() {
        this.showCreationView.emit('tell parent to open create views');
        console.log('disable edit mode');
    }
    openListView() {
        console.log('open list view');
        this.showListView.emit('show full view');
    }

    setSourceCodeEditor(key: string) {
        this.currentFile = key;
        const templateKey = 'Templates/' + key + '-template.vtl';
        this.packageCreationStore.state$.subscribe(cba => {
            console.log('cba ------');
            console.log(cba);
            console.log(key);
            console.log(this.templateAndMappingMap);
            const templateInfo = new TemplateInfo();
            if (cba.templates && cba.templates.files.has(templateKey)) {
                const fileContent = cba.templates.getValue(templateKey.trim());
                console.log(fileContent);
                templateInfo.fileContent = fileContent;
                templateInfo.fileName = templateKey;
                templateInfo.type = 'template';
            }
            const mappingKey = 'Templates/' + key + '-mapping.json';
            if (cba.mapping && cba.mapping.files.has(mappingKey)) {
                const obj = JSON.parse(cba.mapping.getValue(mappingKey));
                templateInfo.mapping = obj;
                templateInfo.fileName = mappingKey;
                templateInfo.type += 'mapping';
            }
            this.templateStore.changeTemplateInfo(templateInfo);
            this.openCreationView();
            this.sharedService.enableEdit();
        });
    }

    getKeys(templateAndMappingMap: Map<string, TemplateAndMapping>) {
        return Array.from(this.templateAndMappingMap.keys());
    }

    getValue(file: string) {
        return this.templateAndMappingMap.get(file);
    }
    initDelete(file) {
        console.log(file);
        this.fileToDelete = file;
    }
    condifrmDelete() {
        console.log(this.templateAndMappingMap);
        this.templateAndMappingMap.delete(this.fileToDelete);
        if (this.templateAndMappingMap.size <= 0) {
            this.openCreationView();
        }
        // Delete from templates
        this.packageCreationStore.state.templates.files.delete('Templates/' + this.fileToDelete + '-template.vtl');
        // Delete from Mapping
        this.packageCreationStore.state.mapping.files.delete('Templates/' + this.fileToDelete + '-mapping.json');

    }

}
