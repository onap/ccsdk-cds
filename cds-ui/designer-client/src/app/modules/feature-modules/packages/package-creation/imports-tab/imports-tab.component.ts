import { Component, OnInit } from '@angular/core';
import { FileSystemFileEntry, NgxFileDropEntry } from 'ngx-file-drop';
import { PackageCreationStore } from '../package-creation.store';
import { PackageCreationUtils } from '../package-creation.utils';


@Component({
    selector: 'app-imports-tab',
    templateUrl: './imports-tab.component.html',
    styleUrls: ['./imports-tab.component.css']
})
export class ImportsTabComponent implements OnInit {

    public definitionFiles: Map<string, string> = new Map<string, string>();
    public uploadedFiles: FileSystemFileEntry[] = [];
    private fileNames: Set<string> = new Set();
    fileToDelete: any = {};
    public files: NgxFileDropEntry[] = [];

    constructor(private packageCreationStore: PackageCreationStore, private packageCreationUtils: PackageCreationUtils) {
    }
    ngOnInit(): void {
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage.definitions && cbaPackage.definitions.imports && cbaPackage.definitions.imports.size > 0) {
                this.definitionFiles = cbaPackage.definitions.imports;
            }
        });
    }
    removeInitFile(index) {
        this.uploadedFiles.splice(index, 1);
    }
    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {
            // Is it a file? & Not added before
            if (droppedFile.fileEntry.isFile) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);
                console.log(fileEntry.name);
                this.fileNames.add(fileEntry.name);

            }
        }
    }
    initDelete(file) {
        console.log(file);
        this.fileToDelete = file;
    }
    removeFile() {
        const filename = this.fileToDelete.key;
        this.packageCreationStore.removeFileFromDefinition(filename);

        for (let i = 0; i < this.uploadedFiles.length; i++) {
            console.log(this.uploadedFiles[i]);
            if (this.uploadedFiles[i].name === filename) {
                this.uploadedFiles.splice(i, 1);
                break;
            }
        }
    }

    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }

    setFilesToStore() {
        for (const droppedFile of this.uploadedFiles) {
            droppedFile.file((file: File) => {
                const fileReader = new FileReader();
                fileReader.onload = (e) => {
                    this.packageCreationStore.addDefinition('Definitions/' + droppedFile.name,
                        fileReader.result.toString());
                };
                fileReader.readAsText(file);
            });

        }
    }

    resetTheUploadedFiles() {
        this.uploadedFiles = [];
    }

    textChanges(code: any, key: string) {
        this.packageCreationStore.addDefinition(key, code);
    }

    changeDivShow(mapIndex: number) {
        const divElement = document.getElementById('id-' + mapIndex) as HTMLElement;
        if (divElement.getAttribute('class').includes('show')) {
            divElement.setAttribute('class', 'collapse');
        } else {
            divElement.setAttribute('class', 'collapse show');
        }
        console.log(divElement.getAttribute('class'));
    }
}
