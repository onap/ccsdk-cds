/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/

import {Component, OnInit} from '@angular/core';
import {FilesContent, FolderNodeElement, MetaDataFile, MetaDataTab} from './mapping-models/metadata/MetaDataTab.model';
// import {saveAs} from 'file-saver/dist/FileSaver';
import * as JSZip from 'jszip';
import {Observable} from 'rxjs';
import {ApiService} from '../../../../common/core/services/api.service';
import {BlueprintURLs} from '../../../../common/constants/app-constants';
import {Import, Metadata, VlbDefinition} from './mapping-models/definitions/VlbDefinition';
import {JsonConvert} from 'json2typescript';
import {JsonPipe} from '@angular/common';

@Component({
    selector: 'app-package-creation',
    templateUrl: './package-creation.component.html',
    styleUrls: ['./package-creation.component.css']
})
export class PackageCreationComponent implements OnInit {

    modes: string[] = ['Designer Mode', 'Scripting Mode'];
    dictionaryLibraryInstances: string[] = ['x', 'y'];
    private target: HTMLElement;
    private newElement: HTMLElement;
    private metaDataTab: MetaDataTab = new MetaDataTab();

    private result: string;

    private folder: FolderNodeElement = new FolderNodeElement();
    private zipFile: JSZip = new JSZip();
    private filesData: any = [];


    constructor(private api: ApiService, private pipe: JsonPipe) {
    }

    ngOnInit() {
    }


    createAnotherCustomKeyDiv() {
        console.log(this.metaDataTab);
        this.newElement = document.createElement('div');
        this.newElement.setAttribute('class', 'alert-dark');
        this.target = document.getElementById('target');
        this.target.appendChild(this.newElement);
        this.metaDataTab = new MetaDataTab();
        this.metaDataTab.name = 'klfdj';
        this.metaDataTab.entryFileName = 'Definitions/vLB_CDS.json';
        this.metaDataTab.description = 'rere';
        this.metaDataTab.tags = 'ffsssssss';
        this.metaDataTab.version = '1.01.10';
        this.metaDataTab.templateName = 'test';


        this.saveToFileSystem(MetaDataFile.getObjectInstance(this.metaDataTab));
    }

    validatePacakgeName() {

    }

    getDictionaryLibraryInstances() {

    }

    saveMetaData() {


    }

    saveFile() {
        const headers = new Headers();
        headers.append('Accept', 'text/plain');
        /*this.http.get('/api/files', {headers: headers})
            .toPromise()
            .then(response => this.saveToFileSystem(response));*/
    }

    private saveToFileSystem(response) {
        // const contentDispositionHeader: string = response.headers.get('Content-Disposition');
        // const parts: string[] = contentDispositionHeader.split(';');
        const filename = 'TOSCA.meta';
        FilesContent.putData(filename, response);

        const filenameEntry = 'vLB_CDS.json';
        const vlbDefinition: VlbDefinition = new VlbDefinition();
        const metadata: Metadata = new Metadata();

        metadata.templateAuthor = ' lldkslds';
        metadata.templateName = ' lldkslds';
        metadata.templateTags = ' lldkslds';
        metadata.templateVersion = ' lldkslds';
        metadata['author-email'] = ' lldkslds';
        metadata['user-groups'] = ' lldkslds';
        vlbDefinition.metadata = metadata;

        vlbDefinition.imports = [{
            file: 'Definitions/data_types.json'
        }];

        const jsonConvert: JsonConvert = new JsonConvert();
        const value = this.pipe.transform(vlbDefinition);
        FilesContent.putData(filenameEntry, value);


        this.filesData.push(this.folder.TREE_DATA);
        // saveAs(blob, filename);
        this.saveToBackend();
    }


    saveToBackend() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                const formData = new FormData();
                formData.append('file', blob);
                // this.editorService.saveBlueprint("/create-blueprint/", formData)
                this.saveBlueprint(formData)
                    .subscribe(
                        data => {
                            console.log('Success:' + JSON.stringify(data));
                        }, error => {
                            console.log('Save -' + error.message);
                        });

            });
    }


    create() {
        this.folder.TREE_DATA.forEach((path) => {

            const name = path.name;
            if (path.children) {
                this.zipFile.folder(name);
                path.children.forEach(children => {
                    const name2 = children.name;
                    console.log(FilesContent.getMapOfFilesNamesAndContent());
                    console.log(name2);
                    if (FilesContent.getMapOfFilesNamesAndContent().has(name2)) {
                        this.zipFile.file(name + '/' + name2, FilesContent.getMapOfFilesNamesAndContent().get(name2));
                    } else {
                        // this.zipFile.file(name + '/' + name2, 'kdjskj');
                    }

                });

            }
        });
    }

    saveBlueprint(body: any | null, options?: any): Observable<any> {

        return this.api.post('https://localhost:3000/' + BlueprintURLs.save, body, {responseType: 'text'});
    }
}
