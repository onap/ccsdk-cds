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

import {Observable, Subject} from 'rxjs';
import {ApiService} from '../../../../common/core/services/api.service';
import {BlueprintURLs, ResourceDictionaryURLs} from '../../../../common/constants/app-constants';
import {PackagesApiService} from '../packages-api.service';
import {PackagesStore} from '../packages.store';
import {ResourceDictionary} from './mapping-models/ResourceDictionary.model';
import {FilesContent, FolderNodeElement} from './mapping-models/metadata/MetaDataTab.model';
import {PackageCreationModes} from './creationModes/PackageCreationModes';
import {PackageCreationBuilder} from './creationModes/PackageCreationBuilder';
import {PackageCreationStore} from './package-creation.store';
import {CBAPackage} from './mapping-models/CBAPacakge.model';
import {PackageCreationUtils} from './package-creation.utils';
import * as JSZip from 'jszip';
import {DesignerStore} from '../designer/designer.store';

@Injectable({
    providedIn: 'root'
})
export class PackageCreationService {
    private cbaPackage: CBAPackage;
    folder: FolderNodeElement = new FolderNodeElement();
    filesData: any = [];
    zipFile: JSZip = new JSZip();

    constructor(private api: ApiService, private packagesListService: PackagesApiService,
                private packagesStore: PackagesStore, private designerStore: DesignerStore,
                private packageCreationStore: PackageCreationStore, private packageCreationUtils: PackageCreationUtils
    ) {
        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                this.cbaPackage = cbaPackage;
            });
    }

    private saveBlueprint(body: any | null, options?: any): Observable<string> {
        return this.api.post(BlueprintURLs.save, body, {responseType: 'text'});
    }

    private enrichBlueprint(body: any | null, options?: any): Observable<any> {
        return this.api.post(BlueprintURLs.enrich, body, {responseType: 'blob'});
    }

    private enrichandpublish(body: any | null, options?: any): Observable<any> {
        return this.api.post(BlueprintURLs.enrichandpublish, body, {responseType: 'text'});
    }

    private deployBlueprint(body: any | null, options?: any): Observable<any> {
        return this.api.post(BlueprintURLs.deploy, body, {responseType: 'text'});
    }

    async checkBlueprintNameAndVersion(name: string, version: string): Promise<boolean> {
        return await this.packagesListService.checkBlueprintIfItExists(name, version)
            .then(bluePrintModelsResult => bluePrintModelsResult != null && bluePrintModelsResult.length > 0);
    }

    refreshPackages() {
        this.packagesStore.getAll();
    }

    public savePackage(blob): Observable<string> {
        const formData = this.getFormData(blob);
        return this.saveBlueprint(formData);
    }

    enrichPackage(blob) {
        const formData = this.getFormData(blob);
        return this.enrichBlueprint(formData);
    }

    enrichAndDeployPackage(blob) {
        const formData = this.getFormData(blob);
        return this.enrichandpublish(formData);
    }

    deploy(blob) {
        const formData = this.getFormData(blob);
        return this.deployBlueprint(formData);
    }

    private getFormData(blob) {
        const formData = new FormData();
        formData.append('file', blob);
        return formData;
    }

    getTemplateAndMapping(variables: string[]): Observable<ResourceDictionary[]> {
        return this.api.post(ResourceDictionaryURLs.searchResourceDictionaryByNames, variables);
    }

    downloadPackage(id) {
        return this.api.getCustomized(BlueprintURLs.download + id, {responseType: 'blob'});
    }

    public saveBlueprintToDataBase(): Observable<string> {
        this.formTreeData();
        this.create();
        const subject = new Subject<any>();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.savePackage(blob).subscribe(bluePrintModel => {
                    subject.next(bluePrintModel);
                });
            });
        return subject.asObservable();
    }

    public deployCurrentPackage() {
        this.formTreeData();
        this.create();
        const subject = new Subject<any>();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.deploy(blob).subscribe(bluePrintModel => {
                    subject.next(bluePrintModel);
                });
            });
        return subject.asObservable();
    }

    public enrichCurrentPackage() {
        this.formTreeData();
        this.create();
        const subject = new Subject<any>();
        return this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                return this.enrichPackage(blob).pipe();
            });
        //  return subject.asObservable();

    }

    private create() {
        this.zipFile = new JSZip();
        FilesContent.getMapOfFilesNamesAndContent().forEach((value, key) => {
            this.zipFile.folder(key.split('/')[0]);
            this.zipFile.file(key, value);
        });

    }

    private formTreeData() {

        FilesContent.clear();
        let packageCreationModes: PackageCreationModes;
        this.cbaPackage = PackageCreationModes.mapModeType(this.cbaPackage);
        this.cbaPackage.metaData = PackageCreationModes.setEntryPoint(this.cbaPackage.metaData);
        packageCreationModes = PackageCreationBuilder.getCreationMode(this.cbaPackage);
        this.designerStore.state$.subscribe(state => {
            this.cbaPackage.templateTopology.content = this.packageCreationUtils.transformToJson(state.template);
        });
        packageCreationModes.execute(this.cbaPackage, this.packageCreationUtils);
        this.filesData.push(this.folder.TREE_DATA);
    }

}
