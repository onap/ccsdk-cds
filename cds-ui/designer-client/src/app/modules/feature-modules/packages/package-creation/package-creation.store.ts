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

import {Injectable} from '@angular/core';

import {Store} from '../../../../common/core/stores/Store';

import {CBAPackage, DslDefinition} from './mapping-models/CBAPacakge.model';
import {PackageCreationService} from './package-creation.service';
import {FolderNodeElement, MetaDataTabModel} from './mapping-models/metadata/MetaDataTab.model';
import * as JSZip from 'jszip';


@Injectable({
    providedIn: 'root'
})
export class PackageCreationStore extends Store<CBAPackage> {

    private folder: FolderNodeElement = new FolderNodeElement();
    private zipFile: JSZip = new JSZip();

    constructor(private packageCreationService: PackageCreationService) {
        super(new CBAPackage());
    }

    changeMetaData(metaDataObject: MetaDataTabModel) {

        this.setState({
            ...this.state,
            metaData: metaDataObject
        });
    }

    changeDslDefinition(dslDefinition: DslDefinition) {

        this.setState({
            ...this.state,
            definitions: this.state.definitions.setDslDefinition(dslDefinition)
        });
    }


    addDefinition(name: string, content: string) {

        this.setState({
            ...this.state,
            definitions: this.state.definitions.setImports(name, content)
        });
    }

    addScripts(name: string, content: string) {

        this.setState({
            ...this.state,
            scripts: this.state.scripts.setScripts(name, content)
        });

    }

    removeFileFromState(name: string) {
        this.state.scripts.files.delete(name);
    }

    removeFileFromDefinition(filename) {
        this.state.definitions.imports.delete(filename);
    }

    saveBluePrint(blob) {
        this.packageCreationService.savePackage(blob);
    }

    addTemplate(filePath: string, fileContent: string) {
        this.setState({
            ...this.state,
            templates: this.state.templates.setTemplates(filePath, fileContent)
        });
    }

    getTemplateAndMapping(variables: string[]) {
        this.packageCreationService.getTemplateAndMapping(variables).subscribe(element => {
            console.log('the element is ' + element);
        });
    }
}
