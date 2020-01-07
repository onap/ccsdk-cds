import {Component, OnInit} from '@angular/core';
import {NgxFileDropEntry, FileSystemFileEntry, FileSystemDirectoryEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../package-creation.store';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';


@Component({
    selector: 'app-imports-tab',
    templateUrl: './imports-tab.component.html',
    styleUrls: ['./imports-tab.component.css']
})
export class ImportsTabComponent {

    fileContent: string | ArrayBuffer = '';

    constructor(private packageCreationStore: PackageCreationStore, private http: HttpClient) {
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

                    const formData = new FormData();
                    formData.append('logo', file, droppedFile.relativePath);
                    console.log(formData);

                    this.packageCreationStore.addDefinition(droppedFile.relativePath, this.getContent(droppedFile.relativePath));

                });
            } else {
                // It was a directory (empty directories are added, otherwise only files)
                const fileEntry = droppedFile.fileEntry as FileSystemDirectoryEntry;
                console.log(droppedFile.relativePath, fileEntry);


               /* const formData = new FormData();
                formData.append('logo', droppedFile, droppedFile.relativePath);
                console.log(formData);*/

                this.packageCreationStore.addDefinition(droppedFile.relativePath, this.getContent(droppedFile.relativePath));

            }
        }
    }

    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }


    getContent(filePath: string) {
        let content = '';
        this.getJSON(filePath).subscribe(data => {
            content = data;
        });
        return content;
    }

    public getJSON(filePath: string): Observable<any> {
        return this.http.get(filePath);
    }
}
