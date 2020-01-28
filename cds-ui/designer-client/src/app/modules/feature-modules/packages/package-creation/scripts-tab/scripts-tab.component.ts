import {Component, OnInit} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../package-creation.store';
import {PackageCreationUtils} from '../package-creation.utils';

@Component({
    selector: 'app-scripts-tab',
    templateUrl: './scripts-tab.component.html',
    styleUrls: ['./scripts-tab.component.css']
})
export class ScriptsTabComponent implements OnInit {

    public scriptsFiles: Map<string, string> = new Map<string, string>();
    public uploadedFiles: FileSystemFileEntry[] = [];
    public files: NgxFileDropEntry[] = [];
    private fileNames: Set<string> = new Set();

    constructor(private packageCreationStore: PackageCreationStore, private packageCreationUtils: PackageCreationUtils) {
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage.scripts && cbaPackage.scripts.files && cbaPackage.scripts.files.size > 0) {
                this.scriptsFiles = cbaPackage.scripts.files;
            }
        });
    }


    ngOnInit() {
    }

    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {
            // Is it a file & Not added before ?
            if (droppedFile.fileEntry.isFile && !this.fileNames.has(droppedFile.fileEntry.name)) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);
                console.log(fileEntry.name);
                this.fileNames.add(fileEntry.name);

            }
        }
    }

    removeFile(fileIndex: number) {
        console.log(this.uploadedFiles[fileIndex]);
        this.packageCreationStore.removeFromState(this.uploadedFiles[fileIndex].name, 'scripts');
        this.uploadedFiles.splice(fileIndex, 1);
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
                    this.packageCreationStore.addScripts(droppedFile.name,
                        this.packageCreationUtils.transformToJson(fileReader.result));
                };
                fileReader.readAsText(file);
            });

        }
    }

    resetTheUploadedFiles() {
        this.uploadedFiles = [];
    }
}
