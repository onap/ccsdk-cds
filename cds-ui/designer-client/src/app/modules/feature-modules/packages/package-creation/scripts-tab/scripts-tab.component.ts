import {Component, OnInit} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../package-creation.store';
import 'ace-builds/src-noconflict/ace';
import 'ace-builds/webpack-resolver';

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

    constructor(
        private packageCreationStore: PackageCreationStore,
    ) {

    }


    ngOnInit() {
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage.scripts && cbaPackage.scripts.files && cbaPackage.scripts.files.size > 0) {
                this.scriptsFiles = cbaPackage.scripts.files;
            }
        });

        /* this.packageStore.state$.subscribe(res => {
             //  this.scriptsFiles =
             console.log('from scripts');
             console.log(res.scripts);
             this.scriptsFiles = res.scripts.files;
         });*/
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
        const filename = 'Scripts/' + this.uploadedFiles[fileIndex].name;
        this.packageCreationStore.removeFileFromState(filename);
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
                    this.packageCreationStore.addScripts('Scripts/' + droppedFile.name,
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
        this.packageCreationStore.addScripts(key, code);
    }
}
