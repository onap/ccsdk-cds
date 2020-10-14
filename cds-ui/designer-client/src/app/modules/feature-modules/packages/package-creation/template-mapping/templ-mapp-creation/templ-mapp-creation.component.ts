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
import { XmlParser } from '../utils/ParserFactory/XmlParser';
import { TourService } from 'ngx-tour-md-menu';
import { PackageCreationService } from '../../package-creation.service';
import { ParserFactory } from '../utils/ParserFactory/ParserFactory';
import { TemplateType, FileExtension } from '../utils/TemplateType';
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
    templateExt = 'vtl';
    dependancies = new Map<string, Array<string>>();
    dependanciesSource = new Map<string, string>();
    mappingRes = [];
    currentTemplate: any;
    currentMapping: any;
    edit = false;
    templatesExist = false;
    fileToDelete: any = {};
    parserFactory: ParserFactory;
    selectedProps: Set<string>;

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
        this.selectedProps = new Set<string>();
        this.parserFactory = new ParserFactory();
        this.templateStore.state$.subscribe(templateInfo => {
            // init Template&mapping vars
            console.log('Oninit');
            console.log(templateInfo);
            this.templateInfo = templateInfo;
            this.fileToDelete = templateInfo.fileName;
            this.fileName = templateInfo.fileName.split('/')[1];
            if (this.fileName) {
                this.fileName = this.fileName.substr(0, this.fileName.lastIndexOf('-'));
            }
            if (templateInfo.type === 'mapping' || templateInfo.type.includes('mapping')) {
                this.mappingRes = templateInfo.mapping;
                this.currentMapping = Object.assign({}, templateInfo);
                this.resourceDictionaryRes = [];
                this.resTableDtTrigger.next();
            } else {
                this.mappingRes = [];
                this.currentMapping = Any;
                this.resourceDictionaryRes = [];
            }
            this.templateFileContent = templateInfo.fileContent;
            this.templateExt = this.templateInfo.ext || this.templateExt;
            this.currentTemplate = Object.assign({}, templateInfo);

            if (templateInfo.type === 'template' || templateInfo.type.includes('template')) {
                console.log('template extension ' + this.templateExt);
                this.currentTemplate.fileName = 'Templates/' + this.fileName + '-template.' + this.templateExt;
                console.log(this.currentTemplate.fileName);
            } else {
                this.currentTemplate = Any;
            }

        });


        this.sharedService.isEdit().subscribe(res => {
            console.log('------------------------....');
            this.templatesExist = this.packageCreationStore.state.templates.files.size > 0
                || this.packageCreationStore.state.mapping.files.size > 0;
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
            columnDefs: [
                {
                    targets: [0, 1, 2], // column or columns numbers
                    orderable: false, // set orderable for selected columns
                    searchable: false,
                },

            ],
        };
        this.dtOptions = {
            pagingType: 'full_numbers',
            pageLength: 25,
            destroy: true,
            retrieve: true,
        };
    }

    setProp(e, propName, index) {
        this.resourceDictionaryRes[index][propName] = e.checked;
        console.log(this.resourceDictionaryRes[index]);
    }
    selectProp(value) {
        console.log(value);
        if (this.selectedProps.has(value)) {
            this.selectedProps.delete(value);
        } else {
            this.selectedProps.add(value);
        }
    }

    removeProps() {
        console.log(this.selectedProps);
        this.selectedProps.forEach(prop => {
            this.resourceDictionaryRes.forEach((res, index) => {
                if (res.name === prop) {
                    console.log('delete...');
                    this.resourceDictionaryRes.splice(index, 1);
                    this.selectedProps.delete(prop);
                }
            });
        });
    }
    selectAllProps() {
        if (this.resourceDictionaryRes.length === this.selectedProps.size) {
            this.selectedProps = new Set<string>();
        } else {
            this.resourceDictionaryRes.forEach(prop => {
                console.log(prop);
                this.selectedProps.add(prop.name);
            });
        }

    }
    reMap() {
        let currentResDictionary = [];
        if (this.selectedProps && this.selectedProps.size > 0) {
            console.log('base');
            this.packageCreationService.getTemplateAndMapping([...this.selectedProps]).subscribe(res => {
                let message = 'Re-Auto mapping';
                this.mappingRes = [];
                currentResDictionary = res;
                console.log(currentResDictionary);
                if (currentResDictionary && currentResDictionary.length <= 0) {
                    message = 'No values for those attributes';
                }

                // Replcae new values with the old ones
                currentResDictionary.forEach(curr => {
                    for (let i = 0; i < this.resourceDictionaryRes.length; i++) {
                        if (this.resourceDictionaryRes[i].name === curr.name) {
                            this.resourceDictionaryRes[i] = curr;
                        }
                    }
                });
                this.rerender();
                this.toastr.success(message, 'Success');
            }, err => {
                this.toastr.error('Error');
            });
        }

    }

    getFileExtension() {
        switch (this.templateExt) {
            case 'vtl':
                return '.vtl';
            case 'kt':
                return '.ktl';
            case 'j2':
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
        // TODO: implement factory Pattern for parser
        console.log('start parsing........ ' + this.templateExt);
        const parser = this.parserFactory.getParser(fileContent, this.templateExt);
        return parser.getVariables(fileContent);
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
                    console.log('variables = ' + this.variables);
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
                    // this.variables = this.getTemplateVariable(this.templateFileContent);
                    // console.log(this.variables);

                };
                fileReader.readAsText(file);
            });
        }
        this.uploadedFiles = [];
    }

    textChanges(code: any, fileName: string) {
        //  this.packageCreationStore.addTemplate(fileName, code);
        // this.templateFileContent = code;
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

    identify(index, item) {
        return item.name;
    }
    setVelocity(index, value) {
        // console.log('velocity value = ' + value);
        // console.log(this.resourceDictionaryRes[index]);
        // tslint:disable-next-line: no-string-literal
        this.resourceDictionaryRes[index].definition.property['metadata'] = {
            'transform-template': value
        };
        console.log(this.resourceDictionaryRes[index]);
    }

    getMappingTableFromTemplate(e) {
        console.log('-' + this.templateFileContent + '-');
        this.resourceDictionaryRes = [];
        if (e) {
            e.preventDefault();
        }
        this.variables = this.getTemplateVariable(this.templateFileContent);
        console.log('variables = ' + this.variables);
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
        } else {
            this.toastr.error('Empty or Invalid file format. Validate your file first');
        }
    }

    initMap(key, map) {
        if (!this.dependanciesSource.has(key)) {
            this.dependanciesSource.set(key, map.key);
        }
        return map.key;
    }
    clear() {
        this.fileName = '';
        this.templateFileContent = '';
        this.resourceDictionaryRes = [];
        this.mappingRes = [];
        this.currentMapping = {};
        this.currentTemplate = {};
        //   this.closeCreationForm();
    }
    cancel() {
        this.openListView();
    }
    saveToStore() {
        const filename = this.fileName;
        if (filename) {
            // check file duplication
            console.log('----------- mode ' + this.edit);
            const fileContent = this.templateFileContent;
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
                this.packageCreationStore.addTemplate('Templates/' + filename + '-template' + this.getFileExtension(),
                    fileContent);
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
    }

    ngOnDestroy(): void {
        // Do not forget to unsubscribe the event
        this.dtTrigger.unsubscribe();
        this.resTableDtTrigger.unsubscribe();
        // this.templateStore.unsubscribe();
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
