import {Component, OnInit} from '@angular/core';
import {FileSystemFileEntry, NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationStore} from '../package-creation.store';
import 'ace-builds/src-noconflict/ace';
import 'ace-builds/webpack-resolver';
import {ToastrService} from 'ngx-toastr';

declare var $: any;

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
    fileToDelete: any = {};

    constructor(
        private packageCreationStore: PackageCreationStore,
        private toastService: ToastrService
    ) {
    }


    ngOnInit() {

        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage.scripts && cbaPackage.scripts.files && cbaPackage.scripts.files.size > 0) {
                this.scriptsFiles = cbaPackage.scripts.files;
            }
        });
    }

    public dropped(files: NgxFileDropEntry[]) {
        this.files = files;
        for (const droppedFile of files) {
            // Is it a file & Not added before ?
            if (droppedFile.fileEntry.isFile) {
                const fileEntry = droppedFile.fileEntry as FileSystemFileEntry;
                this.uploadedFiles.push(fileEntry);
                console.log(fileEntry.name);
                this.fileNames.add(fileEntry.name);

            }
        }
    }

    removeInitFile(index) {
        this.uploadedFiles.splice(index, 1);
    }

    initDelete(file) {
        this.fileToDelete = file;
    }

    removeFile(filePath: string, FileIndex: number) {
        const filename = filePath.split('/')[2] || '';
        //  const filename = 'Scripts/' + this.getFileType(this.uploadedFiles[fileIndex].name) + '/' + this.uploadedFiles[fileIndex].name;
        this.packageCreationStore.removeFileFromState(filePath);
        // remove from upload files array
        // tslint:disable-next-line: prefer-for-of
        for (let i = 0; i < this.uploadedFiles.length; i++) {
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
                    this.packageCreationStore.addScripts('Scripts/' + this.getFileType(droppedFile.name) + '/' + droppedFile.name,
                        fileReader.result.toString());
                };
                fileReader.readAsText(file);
            });

        }
    }

    getFileType(filename: string): string {
        let fileType = '';
        const fileExtension = filename.substring(filename.lastIndexOf('.') + 1);
        if (fileExtension === 'py') {
            fileType = 'python';
        } else if (fileExtension === 'kt') {
            fileType = 'kotlin';
        }
        return fileType;
    }

    resetTheUploadedFiles() {
        this.uploadedFiles = [];
    }

    textChanges(code: any, key: string) {
        this.packageCreationStore.addScripts(key, code);
        this.toastService.success(key + ' is updated successfully');
    }

    changeDivShow(mapIndex: number) {
        const divElement = document.getElementById('id-script-' + mapIndex) as HTMLElement;
        if (divElement.getAttribute('class').includes('show')) {
            divElement.setAttribute('class', 'collapse');
        } else {
            divElement.setAttribute('class', 'collapse show');
        }
    }
}
