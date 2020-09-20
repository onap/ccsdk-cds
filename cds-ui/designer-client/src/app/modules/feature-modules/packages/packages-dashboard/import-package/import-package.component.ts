import {Component, OnInit} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationExtractionService} from '../../package-creation/package-creation-extraction.service';
import {Router} from '@angular/router';

@Component({
    selector: 'app-import-package',
    templateUrl: './import-package.component.html',
    styleUrls: ['./import-package.component.css']
})
export class ImportPackageComponent implements OnInit {

    public uploadedFiles: FileSystemFileEntry[] = [];
    private fileNames: Set<string> = new Set();
    fileToDelete: any = {};
    public files: NgxFileDropEntry[] = [];

    constructor(private packageCreationExtractionService: PackageCreationExtractionService,
                private router: Router) {
    }

    ngOnInit() {
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
        for (let i = 0; i < this.uploadedFiles.length; i++) {
            console.log(this.uploadedFiles[i]);
            if (this.uploadedFiles[i].name === filename) {
                this.uploadedFiles.splice(i, 1);
                break;
            }
        }
    }

    resetTheUploadedFiles() {
        this.uploadedFiles = [];
    }


    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }

    saveFileToStore() {
        for (const droppedFile of this.uploadedFiles) {
            const file = this.getFile(droppedFile);
            this.packageCreationExtractionService.extractBlobToStore(file);
        }
    }

    openFilesInCreationPackage() {
        this.router.navigate(['/packages/createPackage/']);
    }

    async getFile(fileEntry) {
        try {
            return await new Promise((resolve, reject) => fileEntry.file(resolve, reject));
        } catch (err) {
            console.log(err);
        }
    }
}
