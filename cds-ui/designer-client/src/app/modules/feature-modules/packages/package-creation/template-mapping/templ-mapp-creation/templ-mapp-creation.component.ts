import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../../package-creation.store';
import {TemplateInfo, TemplateStore} from '../../template.store';

@Component({
    selector: 'app-templ-mapp-creation',
    templateUrl: './templ-mapp-creation.component.html',
    styleUrls: ['./templ-mapp-creation.component.css']
})
export class TemplMappCreationComponent implements OnInit {
    @Output() showListViewParent = new EventEmitter<any>();

    public uploadedFiles: FileSystemFileEntry[] = [];
    private fileNames: Set<string> = new Set();

    public files: NgxFileDropEntry[] = [];
    fileName: any;
    templateInfo = new TemplateInfo();
    private variables: string[] = [];


    constructor(private packageCreationStore: PackageCreationStore, private templateStore: TemplateStore) {
    }

    ngOnInit() {
        this.templateStore.state$.subscribe(templateInfo => {
            this.templateInfo = templateInfo;
            this.fileName = templateInfo.fileName.split('/')[1];
            this.variables = this.getTemplateVariable(templateInfo.fileContent);
        });
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

    setFilesToStore() {
        for (const droppedFile of this.uploadedFiles) {
            droppedFile.file((file: File) => {
                const fileReader = new FileReader();
                fileReader.onload = (e) => {
                    this.packageCreationStore.addTemplate('Templates/' + this.fileName,
                        fileReader.result.toString());
                };
                fileReader.readAsText(file);
            });

        }
        this.uploadedFiles = [];
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

    initTemplateMappingTableFromCurrentTemplate() {
        if (this.variables && this.variables.length > 0) {
            this.packageCreationStore.getTemplateAndMapping(this.variables);
        }
    }

    textChanges(code: any, fileName: string) {
        this.packageCreationStore.addTemplate(fileName, code);
    }
}
