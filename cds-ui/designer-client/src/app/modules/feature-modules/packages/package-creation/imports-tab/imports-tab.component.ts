import {Component, OnInit} from '@angular/core';
import {NgxFileDropEntry, FileSystemFileEntry, FileSystemDirectoryEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../package-creation.store';


@Component({
    selector: 'app-imports-tab',
    templateUrl: './imports-tab.component.html',
    styleUrls: ['./imports-tab.component.css']
})
export class ImportsTabComponent {
    constructor(private packageCreationStore: PackageCreationStore) {
    }

    public files: NgxFileDropEntry[] = [];

    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {

            // Is it a file?
            if (droppedFile.fileEntry.isFile) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                fileEntry.file((file: File) => {
                    console.log(droppedFile.relativePath, file);
                    this.packageCreationStore.addDefinition(droppedFile.relativePath, '');

                });
            } else {
                // It was a directory (empty directories are added, otherwise only files)
                const fileEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
                console.log(droppedFile.relativePath, fileEntry);
                this.packageCreationStore.addDefinition(droppedFile.relativePath, '');

            }
        }
    }

    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }
}
