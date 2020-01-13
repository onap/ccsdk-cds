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
import {PackageCreationService} from './package-creation.service';
import {PackageCreationUtils} from './package-creation.utils';
import List = _.List;
import {Router} from '@angular/router';

@Component({
    selector: 'app-package-creation',
    templateUrl: './package-creation.component.html',
    styleUrls: ['./package-creation.component.css']
})
export class PackageCreationComponent implements OnInit {
    counter = 0;
    modes: object[] = [
        {name: 'Designer Mode', style: 'mode-icon icon-designer-mode'},
        {name: 'Scripting Mode', style: 'mode-icon icon-scripting-mode'}];
    customKeysAndValues: Map<string, string> = new Map<string, string>();

    dictionaryLibraryInstances: string[] = ['x', 'y'];
    private container: HTMLElement;
    private elements: HTMLCollection;
    private newElement: HTMLElement;
    private metaDataTab: MetaDataTab = new MetaDataTab();

    private result: string;

    private folder: FolderNodeElement = new FolderNodeElement();
    private zipFile: JSZip = new JSZip();
    private filesData: any = [];
    private errorMessage: string;
    private keys: NodeListOf<HTMLElement>;
    private values: NodeListOf<HTMLElement>;


    constructor(private packageCreationService: PackageCreationService, private packageCreationUtils: PackageCreationUtils,
                private router: Router) {
    }

    ngOnInit() {
        // this.customKeysAndValues.set('Dictionary Library Instances', ' ');
        this.keys = document.getElementsByName('key');
        this.values = document.getElementsByName('value');
    }


    createAnotherCustomKeyDiv() {
        this.newElement = document.getElementById('target');
        const id = this.newElement.getAttribute('id');
        this.newElement.setAttribute('id', 'target' + this.counter++);
        const copiedElement = this.newElement.cloneNode(true);
        this.container = document.getElementById('container');
        this.container.appendChild(copiedElement);
        this.elements = this.container.children;
        this.newElement.setAttribute('id', id);
        this.clearCopiedElement();

        // console.log(this.packageCreationService.checkBluePrintNameAndVersion(this.metaDataTab.name, this.metaDataTab.version));
        /*this.metaDataTab = new MetaDataTab();
        this.metaDataTab.name = 'klfdj';
        this.metaDataTab.entryFileName = 'Definitions/vLB_CDS.json';
        this.metaDataTab.description = 'rere';
        this.metaDataTab.tags = 'ffsssssss';
        this.metaDataTab.version = '1.01.10';
        this.metaDataTab.templateName = 'test';


        this.saveToFileSystem(MetaDataFile.getObjectInstance(this.metaDataTab));*/
    }

    private clearCopiedElement() {
        const newCopiedElement: HTMLInputElement = document.getElementById('target' + (this.counter - 1)) as HTMLInputElement;
        const inputElements = newCopiedElement.getElementsByTagName('input');
        for (let i = 0; i < inputElements.length; i++) {
            const element: HTMLInputElement = inputElements.item(i) as HTMLInputElement;
            element.value = '';
        }
    }


    validatePackageNameAndVersion() {
        if (this.metaDataTab.name && this.metaDataTab.version) {
            this.packageCreationService.checkBluePrintNameAndVersion(this.metaDataTab.name, this.metaDataTab.version).then(element => {
                if (element) {
                    this.errorMessage = 'the package with name and version is exists';
                } else {
                    this.errorMessage = ' ';
                }
            });
        }

    }

    getDictionaryLibraryInstances() {


    }

    saveMetaData() {
        for (let i = 0; i < this.values.length; i++) {
            const inputKeyElement: HTMLInputElement = this.keys.item(i) as HTMLInputElement;
            const inputKey: string = inputKeyElement.value;
            const inputValueElement: HTMLInputElement = this.values.item(i) as HTMLInputElement;
            const inputValue: string = inputValueElement.value;
            this.customKeysAndValues.set(inputKey, inputValue);
        }

        this.metaDataTab.mapOfCustomKey = this.customKeysAndValues;
        this.setModeType(this.metaDataTab);
        this.setEntryPoint(this.metaDataTab);

        this.addToscaMetaDataFile(this.metaDataTab);

        const vlbDefinition: VlbDefinition = new VlbDefinition();
        this.fillVLBDefinition(vlbDefinition, this.metaDataTab);

        this.filesData.push(this.folder.TREE_DATA);
        this.saveToBackend();
        this.packageCreationService.refreshPackages();
        this.router.navigate(['/packages']);

    }

    addToscaMetaDataFile(metaDataTab: MetaDataTab) {
        const filename = 'TOSCA.meta';
        FilesContent.putData(filename, MetaDataFile.getObjectInstance(this.metaDataTab));
    }

    private setModeType(metaDataTab: MetaDataTab) {
        if (metaDataTab.mode.startsWith('Scripting')) {
            metaDataTab.mode = 'KOTLIN_SCRIPT';
        } else if (metaDataTab.mode.startsWith('Designer')) {
            metaDataTab.mode = 'DEFAULT';
        } else {
            metaDataTab.mode = 'GENERIC_SCRIPT';
        }
    }

    saveToBackend() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.saveBluePrint(blob);

            });
    }


    private saveBluePrint(blob) {
        const formData = new FormData();
        formData.append('file', blob);
        this.packageCreationService.saveBlueprint(formData)
            .subscribe(
                data => {
                    console.log('Success:' + JSON.stringify(data));
                }, error => {
                    console.log('Error -' + error.message);
                });
    }

    create() {
        this.folder.TREE_DATA.forEach((path) => {

            const name = path.name;
            if (path.children) {
                this.zipFile.folder(name);
                path.children.forEach(children => {
                    const name2 = children.name;
                    if (FilesContent.getMapOfFilesNamesAndContent().has(name2)) {
                        this.zipFile.file(name + '/' + name2, FilesContent.getMapOfFilesNamesAndContent().get(name2));
                    } else {
                    }

                });

            }
        });
    }


    deleteCustomKey(event) {
        this.container = document.getElementById('container');
        const element = event.parentElement.parentElement.parentElement;
        this.container.removeChild(element);
    }


    private setEntryPoint(metaDataTab: MetaDataTab) {
        if (metaDataTab.mode.startsWith('DEFAULT')) {
            metaDataTab.entryFileName = 'Definitions/vLB_CDS.json';
        } else {
            metaDataTab.entryFileName = '';
        }


    }

    private fillVLBDefinition(vlbDefinition: VlbDefinition, metaDataTab: MetaDataTab) {

        const metadata: Metadata = new Metadata();
        metadata.template_author = 'Shaaban';
        metadata.template_name = metaDataTab.templateName;
        metadata.template_tags = metaDataTab.tags;

        metadata.dictionary_group = 'default';
        metadata.template_version = metaDataTab.version;
        metadata['author-email'] = 'shaaban.altanany.ext@orange.com';
        metadata['user-groups'] = 'ADMIN';
        // metadata.mapOfCustomKeys = this.metaDataTab.mapOfCustomKey;
        vlbDefinition.tosca_definitions_version = metaDataTab.version;
        vlbDefinition.metadata = metadata;

        // console.log(vlbDefinition.metadata.mapOfCustomKeys);
        const value = this.packageCreationUtils.transformToJson(vlbDefinition);
        console.log(value);
        FilesContent.putData('vLB_CDS.json', value);
    }
}
