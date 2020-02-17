import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../../package-creation.store';

@Component({
    selector: 'app-templ-mapp-creation',
    templateUrl: './templ-mapp-creation.component.html',
    styleUrls: ['./templ-mapp-creation.component.css']
})
export class TemplMappCreationComponent implements OnInit {

    public uploadedFiles: FileSystemFileEntry[] = [];
    private fileNames: Set<string> = new Set();

    public files: NgxFileDropEntry[] = [];
    fileName: any;

    constructor(private packageCreationStore: PackageCreationStore) {
    }

    ngOnInit() {
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
}
