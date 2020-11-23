import { AfterViewInit, Component, EventEmitter, OnDestroy, OnInit, Output, ViewChild } from '@angular/core';
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
import { MatPaginator, MatSort, MatTableDataSource } from '@angular/material';
import { ChangeDetectorRef } from '@angular/core';
declare var $: any;


@Component({
    selector: 'app-templ-mapp-creation',
    templateUrl: './templ-mapp-creation.component.html',
    styleUrls: ['./templ-mapp-creation.component.css']
})
export class TemplMappCreationComponent implements OnInit, OnDestroy, AfterViewInit {
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
    mappingRes: Mapping[] = [];
    currentTemplate: any;
    currentMapping: any;
    edit = false;
    templatesExist = false;
    fileToDelete: any = {};
    parserFactory: ParserFactory;
    selectedProps: Set<string>;
    resColumns: string[] = [
        'Required', 'Template Input',
        'name', 'Dictionary Name',
        'dictionary-source', 'dependencies',
        'default', 'Velocity', 'Data Type',
        'Entry Schema'
    ];
    initColumn: string[] = ['select', ...this.resColumns];



    // displayedColumns: string[] = ['id', 'name', 'progress', 'color'];
    dataSource: MatTableDataSource<{}>;
    initDataSource: MatTableDataSource<{}>;
    @ViewChild(MatPaginator, { static: true }) initPaginator: MatPaginator;
    @ViewChild(MatSort, { static: true }) initSort: MatSort;

    constructor(
        private packageCreationStore: PackageCreationStore,
        private templateStore: TemplateStore,
        private packageCreationUtils: PackageCreationUtils,
        private toastr: ToastrService,
        private sharedService: SharedService,
        private packageCreationService: PackageCreationService,
        private tourService: TourService,
        private cdr: ChangeDetectorRef
    ) {
    }

    ngAfterViewInit() {
        try {
            this.initDataSource.paginator = this.initPaginator;
            this.initDataSource.sort = this.initSort;
        } catch (e) { }
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
                console.log(templateInfo.mapping);
                this.mappingRes = templateInfo.mapping;
                this.currentMapping = Object.assign({}, templateInfo);
                this.resourceDictionaryRes = [];
                // Assign the data to the data source for the table to render
                this.rerender();
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

    }

    initApplyFilter(event: Event) {
        const filterValue = (event.target as HTMLInputElement).value;
        this.initDataSource.filter = filterValue.trim().toLowerCase();
        if (this.initDataSource.paginator) {
            this.initDataSource.paginator.firstPage();
        }
    }
    setProp(e, propName, index) {
        if (propName === 'input-param') {
            this.mappingRes[index][propName] = e.checked;

        } else {
            // tslint:disable-next-line: no-string-literal
            this.mappingRes[index]['property'][propName] = e.checked;
        }
        //  console.log(this.mappingRes[index]);
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
            this.mappingRes.forEach((res, index) => {
                if (res.name === prop) {
                    console.log('delete...');
                    this.mappingRes.splice(index, 1);
                    this.selectedProps.delete(prop);
                    this.rerender();
                }
            });
        });
    }
    selectAllProps() {
        // if all items are already selected, unselect them
        if (this.mappingRes.length === this.selectedProps.size) {
            this.selectedProps = new Set<string>();
        } else {
            this.mappingRes.forEach(prop => {
                // console.log(prop);
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
                //  this.mappingRes = [];
                currentResDictionary = this.convertDictionaryToMap(res);
                console.log(currentResDictionary);
                if (currentResDictionary && currentResDictionary.length <= 0) {
                    message = 'No values for those attributes';
                }

                // Replcae new values with the old ones
                currentResDictionary.forEach(curr => {
                    for (let i = 0; i < this.mappingRes.length; i++) {
                        if (this.mappingRes[i].name === curr.name) {
                            this.mappingRes[i] = curr;
                        }
                    }
                });
                this.rerender();
                this.toastr.success(message, 'Success');
                this.selectedProps = new Set();
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
        console.log('Templates/' + this.fileName + '-template.' + this.templateInfo.ext);
        this.packageCreationStore.state.templates.files.delete('Templates/' + this.fileName + '-template.' + this.templateInfo.ext);
        // Delete from Mapping
        this.packageCreationStore.state.mapping.files.delete(this.fileToDelete);
        this.openListView();


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

    private finalMapping(mappingArray: Mapping[]): Mapping[] {
        const mapArray: Mapping[] = [];
        for (const mapping of mappingArray) {
            this.MappingAdapter = new MappingAdapter(null, this.dependancies, this.dependanciesSource);
            mapArray.push(this.MappingAdapter.finalize(mapping));
            console.log(mapping);
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
        // tslint:disable-next-line: no-string-literal
        this.mappingRes[index].property['metadata'] = {
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
                this.mappingRes = this.convertDictionaryToMap(this.resourceDictionaryRes);
                console.log(this.mappingRes);
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

    objectToString(object) {
        if (object) {
            return JSON.stringify(object);
        }
        return '';
    }
    saveToStore() {
        const filename = this.fileName;
        if (filename) {
            // check file duplication
            console.log('----------- mode ' + this.edit);
            const fileContent = this.templateFileContent;
            if (
                (!(this.packageCreationStore.fileExist('Templates/' + filename + '-mapping.json')
                    || this.packageCreationStore.fileExist('Templates/' + filename + '-template' + this.getFileExtension())))
                || this.edit
            ) {
                // Save Mapping to Store
                if (this.mappingRes && this.mappingRes.length > 0) {
                    const mapArray = this.finalMapping(this.mappingRes);
                    console.log(mapArray);
                    //  this.packageCreationUtils.transformToJson(this.jsonConvert.serialize(mapArray)))
                    if (this.edit) {
                        this.packageCreationStore.addMapping('Templates/' + filename + '-mapping.json',
                            JSON.stringify(mapArray));
                    } else {
                        this.packageCreationStore.addMapping('Templates/' + filename + '-mapping.json',
                            this.packageCreationUtils.transformToJson(this.jsonConvert.serialize(mapArray)));
                    }
                    this.mappingRes = [];
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
            keyDepend = dict.sources[source].properties['key-dependencies'] || null;
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
        this.initDataSource = new MatTableDataSource(this.mappingRes);
        /*
        Hint: the intial page size for the table will be the result size; I did that because the table doesn't load element in DOM,
        as result some function are not working well like save and you have to move to other pages to fix that.
        */
        this.initPaginator.pageSize = this.mappingRes.length;
        this.initDataSource.paginator = this.initPaginator;
        this.initDataSource.sort = this.initSort;
    }

    ngOnDestroy(): void {
        // Do not forget to unsubscribe the event
        this.dtTrigger.unsubscribe();
        this.resTableDtTrigger.unsubscribe();
        // this.templateStore.unsubscribe();
    }
}

