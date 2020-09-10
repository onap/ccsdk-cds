import { Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
import { FileSystemFileEntry, NgxFileDropEntry } from 'ngx-file-drop';
import { PackageCreationStore } from '../../package-creation.store';
import { TemplateInfo, TemplateStore } from '../../template.store';
import { Subject } from 'rxjs';
import { ResourceDictionary } from '../../mapping-models/ResourceDictionary.model';
import { DataTableDirective } from 'angular-datatables';
import { Mapping, MappingAdapter } from '../../mapping-models/mappingAdapter.model';
import { PackageCreationUtils } from '../../package-creation.utils';
import { JsonConvert, Any } from 'json2typescript';
import { ToastrService } from 'ngx-toastr';
import { SharedService } from '../shared-service';
import { XmlParser } from '../utils/XmlParser';
import { TourService } from 'ngx-tour-md-menu';
import { PackageCreationService } from '../../package-creation.service';
declare var $: any;

@Component({
    selector: 'app-templ-mapp-creation',
    templateUrl: './templ-mapp-creation.component.html',
    styleUrls: ['./templ-mapp-creation.component.css']
})
export class TemplMappCreationComponent implements OnInit, OnDestroy {
    @Output() showListView = new EventEmitter<any>();
    @Output() showCreationView = new EventEmitter<any>();
    public uploadedFiles: FileSystemFileEntry[] = [];
    fileNames: Set<string> = new Set();
    jsonConvert = new JsonConvert();
    public files: NgxFileDropEntry[] = [];
    fileName: any;
    templateInfo = new TemplateInfo();
    variables: string[] = [];
    dtOptions: DataTables.Settings = {};
    initDtOptions: DataTables.Settings = {};
    // We use this trigger because fetching the list of persons can be quite long,
    // thus we ensure the data is fetched before rendering
    dtTrigger = new Subject();
    resTableDtTrigger = new Subject();
    resourceDictionaryRes: ResourceDictionary[] = [];
    allowedExt = ['.vtl'];
    @ViewChild(DataTableDirective, { static: false })
    dtElement: DataTableDirective;
    MappingAdapter: MappingAdapter;
    mapping = new Map();
    templateFileContent: string;
    templateExt = 'Velcoity';
    dependancies = new Map<string, Array<string>>();
    dependanciesSource = new Map<string, string>();
    mappingRes = [];
    currentTemplate: any;
    currentMapping: any;
    edit = false;
    fileToDelete: any = {};

    constructor(
        private packageCreationStore: PackageCreationStore,
        private templateStore: TemplateStore,
        private packageCreationUtils: PackageCreationUtils,
        private toastr: ToastrService,
        private sharedService: SharedService,
        private packageCreationService: PackageCreationService,
        private tourService: TourService,
    ) {
    }

    ngOnInit() {
        this.templateStore.state$.subscribe(templateInfo => {
            // init Template&mapping vars
            console.log('Oninit');
            console.log(templateInfo);
            this.templateInfo = templateInfo;
            this.fileToDelete = templateInfo.fileName;
            this.fileName = templateInfo.fileName.split('/')[1];
            if (this.fileName) {
                this.fileName = this.fileName.split('-')[0];
            }
            if (templateInfo.type === 'mapping' || templateInfo.type.includes('mapping')) {
                this.mappingRes = templateInfo.mapping;
                this.currentMapping = Object.assign({}, templateInfo);
                this.resourceDictionaryRes = [];
                this.resTableDtTrigger.next();
            } else {
                this.mappingRes = [];
                this.currentMapping = Any;
            }
            this.templateFileContent = templateInfo.fileContent;
            this.currentTemplate = Object.assign({}, templateInfo);

            if (templateInfo.type === 'template' || templateInfo.type.includes('template')) {
                this.currentTemplate.fileName = 'Templates/' + this.fileName + '-template.vtl';
            } else {
                this.currentTemplate = Any;
            }

        });

        this.sharedService.isEdit().subscribe(res => {
            console.log('------------------------');
            console.log(res);
            this.edit = res;

            if (!this.edit) {
                console.log('remove ----');
                this.currentMapping = {};
                this.currentTemplate = {};
                this.fileName = '';
                this.templateFileContent = '';
                this.resourceDictionaryRes = [];
                this.mappingRes = [];
            }
        });

        this.initDtOptions = {
            pagingType: 'full_numbers',
            pageLength: 25,
            destroy: true,
            retrieve: true,
        };
        this.dtOptions = {
            pagingType: 'full_numbers',
            pageLength: 25,
            destroy: true,
            retrieve: true,
        };
    }

    getFileExtension() {
        switch (this.templateExt) {
            case 'Velcoity':
                return '.vtl';
            case 'Koltin':
                return '.ktl';
            case 'Jinja':
                return '.j2';
            default:
                return '.vtl';
        }
    }

    fileExtensionFromString(filename: string): string {
        const fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
        return fileExtension;
    }

    public getTemplateVariable(fileContent: string) {
        const variables: string[] = [];
        const stringsSlittedByBraces = fileContent.split('${');
        const stringsDefaultByDollarSignOnly = fileContent.split('"$');

        for (let i = 1; i < stringsSlittedByBraces.length; i++) {
            const element = stringsSlittedByBraces[i];
            if (element) {
                const firstElement = element.split('}')[0];
                if (!variables.includes(firstElement)) {
                    variables.push(firstElement);
                } else {
                    console.log(firstElement);
                }
            }
        }

        for (let i = 1; i < stringsDefaultByDollarSignOnly.length; i++) {
            const element = stringsDefaultByDollarSignOnly[i];
            if (element && !element.includes('$')) {
                const firstElement = element.split('"')[0]
                    .replace('{', '')
                    .replace('}', '').trim();
                if (!variables.includes(firstElement)) {
                    variables.push(firstElement);
                }
            }
        }
        return variables;
    }

    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {
            // Is it a file? & Not added before
            if (droppedFile.fileEntry.isFile) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);
                this.fileNames.add(fileEntry.name);

            }
        }
    }
    removeFile(index) {
        this.uploadedFiles.splice(index, 1);
    }

    confirmDelete() {
        // Delete from templates
        this.sharedService.deleteFromList(this.fileName);
        this.packageCreationStore.state.templates.files.delete(this.fileToDelete);
        // Delete from Mapping
        this.packageCreationStore.state.mapping.files.delete(this.fileToDelete);
        if (
            this.packageCreationStore.state.templates.files.size > 0 ||
            this.packageCreationStore.state.mapping.files.size > 0
        ) {
            this.openListView();
        }

    }
    uploadFile() {
        this.dependancies.clear();
        this.dependanciesSource.clear();
        if (this.allowedExt.includes('.csv') || this.allowedExt.includes('.xml')) {
            this.fetchkeysfromfile();
        } else {
            this.setTemplateFilesToStore();
        }
        $('.btn-cancel').click();
    }

    fetchkeysfromfile() {
        for (const droppedFile of this.uploadedFiles) {
            droppedFile.file((file: File) => {
                const fileReader = new FileReader();
                fileReader.onload = (e) => {
                    const fileExt = this.fileExtensionFromString(droppedFile.name);
                    if (fileExt === 'csv') {
                        this.variables = fileReader.result.toString().split(',');
                    } else {
                        const parser = new XmlParser();
                        this.variables = parser.getVariables(fileReader.result.toString());
                    }
                    console.log(this.variables);
                    this.getMappingTableFromTemplate(null);

                };
                fileReader.readAsText(file);
            });
        }
        this.uploadedFiles = [];
    }

    private convertDictionaryToMap(resourceDictionaries: ResourceDictionary[]): Mapping[] {
        const mapArray: Mapping[] = [];
        for (const resourceDictionary of resourceDictionaries) {
            this.MappingAdapter = new MappingAdapter(resourceDictionary, this.dependancies, this.dependanciesSource);
            mapArray.push(this.MappingAdapter.ToMapping());
        }
        console.log(mapArray);
        return mapArray;
    }

    setTemplateFilesToStore() {
        for (const droppedFile of this.uploadedFiles) {
            droppedFile.file((file: File) => {
                const fileReader = new FileReader();
                fileReader.onload = (e) => {
                    this.templateFileContent = fileReader.result.toString();
                    this.variables = this.getTemplateVariable(this.templateFileContent);

                };
                fileReader.readAsText(file);
            });
        }
        this.uploadedFiles = [];
    }

    textChanges(code: any, fileName: string) {
        //  this.packageCreationStore.addTemplate(fileName, code);
        this.templateFileContent = code;
    }

    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }
    //
    resetTheUploadedFiles() {
        this.uploadedFiles = [];
    }

    openListView() {
        console.log('open List view');
        this.showListView.emit('tell parent to open create views');
    }

    openCreationView() {
        console.log('close creation view');
        this.showCreationView.emit('close create form and open list');
    }

    getMappingTableFromTemplate(e) {
        console.log('-' + this.templateFileContent + '-');
        this.resourceDictionaryRes = [];
        if (e) {
            e.preventDefault();
        }
        if (this.variables && this.variables.length > 0) {
            console.log('base');
            this.packageCreationService.getTemplateAndMapping(this.variables).subscribe(res => {
                let message = 'Attributes are Fetched';
                this.mappingRes = [];
                this.resourceDictionaryRes = res;
                console.log(this.resourceDictionaryRes);
                this.rerender();
                if (this.resourceDictionaryRes && this.resourceDictionaryRes.length <= 0) {
                    message = 'No values for those attributes';
                }
                this.toastr.success(message, 'Success');
            }, err => {
                this.toastr.error('Error');
            });
        }
    }

    initMap(key, map) {
        if (!this.dependanciesSource.has(key)) {
            this.dependanciesSource.set(key, map.key);
        }
        return map.key;
    }
    cancel() {
        this.fileName = '';
        this.templateFileContent = '';
        this.resourceDictionaryRes = [];
        this.mappingRes = [];
        this.currentMapping = {};
        this.currentTemplate = {};
        //   this.closeCreationForm();
    }
    saveToStore() {
        if (this.fileName) {
            // check file duplication
            console.log('----------- mode ' + this.edit);
            if (
                (!(this.packageCreationStore.fileExist('Templates/' + this.fileName + '-mapping.json')
                    || this.packageCreationStore.fileExist('Templates/' + this.fileName + '-template' + this.getFileExtension())))
                || this.edit
            ) {
                // Save Mapping to Store
                if (this.resourceDictionaryRes && this.resourceDictionaryRes.length > 0) {
                    const mapArray = this.convertDictionaryToMap(this.resourceDictionaryRes);
                    this.packageCreationStore.addMapping('Templates/' + this.fileName + '-mapping.json',
                        this.packageCreationUtils.transformToJson(this.jsonConvert.serialize(mapArray)));
                    this.resourceDictionaryRes = [];
                }
                // Save Template to store
                // if (this.templateFileContent) {
                this.packageCreationStore.addTemplate('Templates/' + this.fileName + '-template' + this.getFileExtension(),
                    this.templateFileContent);
                this.templateFileContent = '';
                //  }
                this.fileName = '';
                this.toastr.success('File is created', 'success');
                this.openListView();
                if (localStorage.getItem('tour-guide') !== 'end' && localStorage.getItem('tour-guide') !== 'false') {
                    this.tourService.goto('tm-templateEdit');
                }
            } else {
                console.log('this file already exist');
                this.toastr.error('File name already exist', 'Error');
            }
        } else {
            this.toastr.error('Add the file name', 'Error');
        }
    }


    selectSource(dict, e) {
        const source = e.target.value;
        let keyDepend = null;
        this.dependancies.set(dict.name, null);
        try {
            keyDepend = dict.definition.sources[source].properties['key-dependencies'] || null;
        } catch (e) { }
        console.log(dict);
        console.log(source);
        if (keyDepend) {
            this.dependancies.set(dict.name, keyDepend);
        } else {
            // this.dependancies.delete(dict.name);
            // this.dependanciesSource.delete(dict.name);
        }
        this.dependanciesSource.set(dict.name, source);
        console.log(this.dependancies);
        console.log(this.dependanciesSource);
    }

    getKeys(map: Map<string, any>) {
        return Array.from(map.keys());
    }

    getValue(key) {
        return this.dependancies.get(key);
    }

    rerender(): void {
        this.dtTrigger.next();

        // if (this.dtElement.dtInstance) {
        //     console.log('rerender');
        //     this.dtElement.dtInstance.then((dtInstance: DataTables.Api) => {
        //         dtInstance.destroy();
        //         this.dtElement.dtOptions = this.dtOptions;
        //         this.dtElement.dtTrigger.next();
        //         dtInstance.draw();
        //     });
        // } else {
        //     this.dtTrigger.next();
        // }
    }

    ngOnDestroy(): void {
        // Do not forget to unsubscribe the event
        this.dtTrigger.unsubscribe();
        this.resTableDtTrigger.unsubscribe();
    }
}

class DependancyVal {
    source: string;
    keyDepend: any;
    constructor(
        source: string,
        keyDepend: any
    ) {
        this.source = source;
        this.keyDepend = keyDepend;
    }
}
