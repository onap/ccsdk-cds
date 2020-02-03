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
import {FilesContent, FolderNodeElement, MetaDataTabModel} from './mapping-models/metadata/MetaDataTab.model';

import * as JSZip from 'jszip';
import {PackageCreationStore} from './package-creation.store';
import {Definition} from './mapping-models/CBAPacakge.model';
import {PackageCreationModes} from './creationModes/PackageCreationModes';
import {PackageCreationBuilder} from './creationModes/PackageCreationBuilder';


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
    private metaDataTab: MetaDataTabModel = new MetaDataTabModel();
    private folder: FolderNodeElement = new FolderNodeElement();
    private zipFile: JSZip = new JSZip();
    private filesData: any = [];
    private definition: Definition = new Definition();

    // adding initial referencing to designer mode


    constructor(private packageCreationStore: PackageCreationStore) {
    }

    ngOnInit() {

    }

    saveBluePrint() {
        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                console.log(cbaPackage);
                FilesContent.clear();
                let packageCreationModes: PackageCreationModes;
                cbaPackage = PackageCreationModes.mapModeType(cbaPackage);
                cbaPackage.metaData = PackageCreationModes.setEntryPoint(cbaPackage.metaData);
                packageCreationModes = PackageCreationBuilder.getCreationMode(cbaPackage);
                packageCreationModes.execute(cbaPackage);
                /*
                 this.addToscaMetaDataFile(this.metaDataTab);

                 this.definition = cbaPackage.definitions;
                 this.definition.metaDataTab = cbaPackage.metaData;
                 this.createDefinitionsFolder(this.definition);*/
                // const vlbDefinition: VlbDefinition = new VlbDefinition();
                // this.fillVLBDefinition(vlbDefinition, this.metaDataTab);

                this.filesData.push(this.folder.TREE_DATA);
                this.saveBluePrintToDataBase();
            });


    }


    saveBluePrintToDataBase() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.packageCreationStore.saveBluePrint(blob);
            });
    }


    create() {
        FilesContent.getMapOfFilesNamesAndContent().forEach((key, value) => {
            this.zipFile.folder(key.split('/')[0]);
            this.zipFile.file(key, value);
        });
        /*this.folder.TREE_DATA.forEach((path) => {
            const name = path.name;
            if (path.children) {
                this.zipFile.folder(name);
                path.children.forEach(children => {
                    const name2 = children.name;
                    if (FilesContent.getMapOfFilesNamesAndContent().has(name2)) {
                        this.zipFile.file(name + '/' + name2, FilesContent.getMapOfFilesNamesAndContent().get(name2));
                    } else {
                        // this.zipFile.file(name2, FilesContent.getMapOfFilesNamesAndContent().get(name2));
                    }

                });

            }
        });*/
    }

    private createDefinitionsFolder(definition: Definition) {
        this.definition.imports.forEach((key, value) => {
            FilesContent.putData(key, value);
        });

        /*const filenameEntry = 'vLB_CDS.json';
        const vlbDefinition: VlbDefinition = new VlbDefinition();
        const metadata: MetaDataTabModel = new MetaDataTabModel();

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

        const value = this.packageCreationUtils.transformToJson(vlbDefinition);
        FilesContent.putData(filenameEntry, value);*/
    }

}
