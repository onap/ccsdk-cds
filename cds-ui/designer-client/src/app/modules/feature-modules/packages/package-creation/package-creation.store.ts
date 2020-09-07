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
import {MetaDataTabModel} from './mapping-models/metadata/MetaDataTab.model';
import {Observable} from 'rxjs';
import {ResourceDictionary} from './mapping-models/ResourceDictionary.model';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import {TemplateTopology} from './mapping-models/definitions/VlbDefinition';


@Injectable({
    providedIn: 'root'
})
export class PackageCreationStore extends Store<CBAPackage> {


    constructor(private packageCreationService: PackageCreationService) {
        super(new CBAPackage());
    }

    changeMetaData(metaDataObject: MetaDataTabModel) {

        this.setState({
            ...this.state,
            metaData: metaDataObject
        });
    }

    setCustomKeys(mapOfCustomKey: Map<string, string>) {
        this.setState({
            ...this.state,
            metaData: this.state.metaData.setCustomKey(mapOfCustomKey)
        });
    }

    istemplateExist(): boolean {
        return this.state.templates.files.size > 0 && this.state.mapping.files.size > 0;
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

    fileExist(key: string) {
        return this.state.templates.files.has(key);
    }

    removeFileFromDefinition(filename) {
        this.state.definitions.imports.delete(filename);
    }

    saveBluePrint(blob): Observable<BluePrintDetailModel> {
        return this.packageCreationService.savePackage(blob);
    }

    enrichBluePrint(blob): Observable<any> {
        return this.packageCreationService.enrichPackage(blob);
    }

    deployBluePrint(blob): Observable<BluePrintDetailModel> {
        return this.packageCreationService.deploy(blob);
    }

    addTemplate(filePath: string, fileContent: string) {
        this.setState({
            ...this.state,
            templates: this.state.templates.setTemplates(filePath, fileContent)
        });
    }

    addMapping(filePath: string, fileContent: string) {
        this.setState({
            ...this.state,
            mapping: this.state.mapping.setContent(filePath, fileContent)
        });
    }

    getTemplateAndMapping(variables: string[]): Observable<ResourceDictionary[]> {
        return this.packageCreationService.getTemplateAndMapping(variables);
    }

    clear() {
        this.setState(new CBAPackage());
    }

    setEntryDefinition(data: string) {
        console.log('setting manual enrichment ');
    }

    addTopologyTemplate(templateTopology: TemplateTopology) {
        this.setState({
            ...this.state,
            templateTopology
        });
    }
}
