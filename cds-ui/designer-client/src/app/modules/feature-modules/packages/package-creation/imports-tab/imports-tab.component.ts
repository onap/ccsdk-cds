import {Component, OnInit} from '@angular/core';
import {NgxFileDropEntry, FileSystemFileEntry, FileSystemDirectoryEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../package-creation.store';
import {PackageCreationUtils} from '../package-creation.utils';


@Component({
    selector: 'app-imports-tab',
    templateUrl: './imports-tab.component.html',
    styleUrls: ['./imports-tab.component.css']
})
export class ImportsTabComponent {

    public definitionFiles: Map<string, string> = new Map<string, string>();
    public uploadedFiles: FileSystemFileEntry[] = [];

    public files: NgxFileDropEntry[] = [];

    constructor(private packageCreationStore: PackageCreationStore, private packageCreationUtils: PackageCreationUtils) {
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage.definitions && cbaPackage.definitions.files && cbaPackage.definitions.files.size > 0) {
                this.definitionFiles = cbaPackage.definitions.files;
            }
        });
    }

    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {

            // Is it a file?
            if (droppedFile.fileEntry.isFile) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);


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
                    this.packageCreationStore.addDefinition(droppedFile.name,
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
