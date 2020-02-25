import { Component, EventEmitter, OnInit, Output, OnDestroy, ViewChild } from '@angular/core';
import { FileSystemFileEntry, NgxFileDropEntry } from 'ngx-file-drop';
import { PackageCreationStore } from '../../package-creation.store';
import { TemplateInfo, TemplateStore } from '../../template.store';
import { Subject } from 'rxjs';
import { ResourceDictionary } from '../../mapping-models/ResourceDictionary.model';
import { DataTableDirective } from 'angular-datatables';
import { MappingAdapter } from '../../mapping-models/mappingAdapter.model';
import { Mapping } from '../../mapping-models/CBAPacakge.model';

@Component({
    selector: 'app-templ-mapp-creation',
    templateUrl: './templ-mapp-creation.component.html',
    styleUrls: ['./templ-mapp-creation.component.css']
})
export class TemplMappCreationComponent implements OnInit, OnDestroy {
    @Output() showListViewParent = new EventEmitter<any>();

    public uploadedFiles: FileSystemFileEntry[] = [];
    private fileNames: Set<string> = new Set();

    public files: NgxFileDropEntry[] = [];
    fileName: any;
    templateInfo = new TemplateInfo();
    private variables: string[] = [];
    private mappingFileValues = [];
    dtOptions: DataTables.Settings = {};
    // We use this trigger because fetching the list of persons can be quite long,
    // thus we ensure the data is fetched before rendering
    dtTrigger = new Subject();
    resourceDictionaryRes: ResourceDictionary[] = [];
    allowedExt = [];
    @ViewChild(DataTableDirective, { static: false })
    dtElement: DataTableDirective;
    MappingAdapter: MappingAdapter;
    mapping = new Map();
    templateFileContent: string;
    templateExt = 'Velcoity';


    constructor(
        private packageCreationStore: PackageCreationStore,
        private templateStore: TemplateStore,
    ) {
    }

    ngOnInit() {
        this.templateStore.state$.subscribe(templateInfo => {
            console.log(templateInfo);
            this.templateInfo = templateInfo;
            this.fileName = templateInfo.fileName.split('/')[1];
            this.variables = this.getTemplateVariable(templateInfo.fileContent);
        });

        this.dtOptions = {
            pagingType: 'full_numbers',
            pageLength: 10,
            destroy: true,
            // retrieve: true,
        };
    }

    getFileExtension() {
        switch (this.templateExt) {
            case 'Velcoity': return '.vtl';
            case 'Koltin': return '.ktl';
            case 'Jinja': return '.j2';
            default: return '.vtl';
        }
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
            if (droppedFile.fileEntry.isFile && !this.fileNames.has(droppedFile.fileEntry.name)) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);
                this.fileNames.add(fileEntry.name);

            }
        }
    }

    removeFile(fileIndex: number) {
        /*const filename = 'Definitions/' + this.uploadedFiles[fileIndex].name;
        this.packageCreationStore.removeFileFromDefinition(filename);
        this.uploadedFiles.splice(fileIndex, 1);*/
    }

    uploadFile() {
        if (this.allowedExt.includes('.csv')) {
            this.fetchCSVkeys();
        } else {
            this.setTemplateFilesToStore();
        }
    }

    fetchCSVkeys() {
        for (const droppedFile of this.uploadedFiles) {
            droppedFile.file((file: File) => {
                const fileReader = new FileReader();
                fileReader.onload = (e) => {
                    this.variables = fileReader.result.toString().split(',');
                    console.log(this.variables);
                    this.initTemplateMappingTableFromCurrentTemplate(null);
                };
                fileReader.readAsText(file);
            });
        }
        this.uploadedFiles = [];
    }

    private convertDictionaryToMap(resourceDictionaries: ResourceDictionary[]): Mapping[] {
        const mapArray = [];
        for (const resourceDictionary of resourceDictionaries) {
            this.MappingAdapter = new MappingAdapter(resourceDictionary);
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

    resetTheUploadedFiles() {
        this.uploadedFiles = [];
    }

    openListView() {
        this.showListViewParent.emit('tell parent to open create views');
    }

    initTemplateMappingTableFromCurrentTemplate(e) {
        if (e) { e.preventDefault(); }
        if (this.variables && this.variables.length > 0) {
            console.log('base');
            this.packageCreationStore.getTemplateAndMapping(this.variables).subscribe(res => {
                this.resourceDictionaryRes = res;
                console.log(this.resourceDictionaryRes);
                this.rerender();
            });
        }
    }

    saveToStore() {
        // Save Mapping to Store
        const mapArray = this.convertDictionaryToMap(this.resourceDictionaryRes);
        this.packageCreationStore.addMapping('Templates/' + this.fileName + '-mapping.json', JSON.stringify(mapArray));
        console.log(this.packageCreationStore.state);
        // Save Template to store
        this.packageCreationStore.addTemplate('Templates/' + this.fileName + '-template' + this.getFileExtension(),
            this.templateFileContent);
    }

    rerender(): void {
        if (this.dtElement.dtInstance) {
            console.log('rerender');
            this.dtElement.dtInstance.then((dtInstance: DataTables.Api) => {
                dtInstance.destroy();
                this.dtElement.dtOptions = this.dtOptions;
                this.dtElement.dtTrigger.next();
                dtInstance.draw();
            });
        } else {
            this.dtTrigger.next();
        }
    }

    ngOnDestroy(): void {
        // Do not forget to unsubscribe the event
        this.dtTrigger.unsubscribe();
    }
}
