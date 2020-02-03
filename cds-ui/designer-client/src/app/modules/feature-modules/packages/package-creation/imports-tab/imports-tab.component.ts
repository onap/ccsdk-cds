import { Component, OnInit } from '@angular/core';
import { FileSystemFileEntry, NgxFileDropEntry } from 'ngx-file-drop';
import { PackageCreationStore } from '../package-creation.store';
import { PackageCreationUtils } from '../package-creation.utils';
import 'ace-builds/src-noconflict/ace';
import 'ace-builds/webpack-resolver';


@Component({
    selector: 'app-imports-tab',
    templateUrl: './imports-tab.component.html',
    styleUrls: ['./imports-tab.component.css']
})
export class ImportsTabComponent implements OnInit {

    public definitionFiles: Map<string, string> = new Map<string, string>();
    public uploadedFiles: FileSystemFileEntry[] = [];
    private fileNames: Set<string> = new Set();

    public files: NgxFileDropEntry[] = [];

    constructor(private packageCreationStore: PackageCreationStore, private packageCreationUtils: PackageCreationUtils) {
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage.definitions && cbaPackage.definitions.imports && cbaPackage.definitions.imports.size > 0) {
                this.definitionFiles = cbaPackage.definitions.imports;
            }
        });
    }

    ngOnInit(): void {
        // TODO
    }

    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {
            // Is it a file? & Not added before
            if (droppedFile.fileEntry.isFile && !this.fileNames.has(droppedFile.fileEntry.name)) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);
                console.log(fileEntry.name);
                this.fileNames.add(fileEntry.name);

            } /*else {
                const directorEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
                this.filesUnderDirectory = directorEntry.getFile('');
               // const fileEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
               /* this.uploadedFile.push(droppedFile);
                const formData = new FormData()
                formData.append('logo', fileEntry, droppedFile.relativePath);
                console.log(formData);*/
            /* // It was a directory (empty directories are added, otherwise only files)
             const fileEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
             console.log(droppedFile.relativePath, fileEntry);


              const formData = new FormData();
              formData.append('logo', droppedFile, droppedFile.relativePath);
              console.log(formData);

             //this.packageCreationStore.addDefinition(droppedFile.relativePath, this.getContent(droppedFile.relativePath));
*/
            /* }*/
        }
    }

    removeFile(fileIndex: number) {
        const filename = 'Definitions/' + this.uploadedFiles[fileIndex].name;
        this.packageCreationStore.removeFileFromDefinition(filename);
        this.uploadedFiles.splice(fileIndex, 1);
    }

    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }

    /* readFileContent(file: File): string | ArrayBuffer {
         const fileReader = new FileReader();
        // let content: string | ArrayBuffer = '';
         fileReader.onload = (e) => {
             content = fileReader.result;
         };
         fileReader.readAsText(file);
         return content;
     }
 */
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
}
