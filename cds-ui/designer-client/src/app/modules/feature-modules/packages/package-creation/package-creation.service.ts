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

import {Observable} from 'rxjs';
import {ApiService} from '../../../../common/core/services/api.service';
import {BlueprintURLs, ResourceDictionaryURLs} from '../../../../common/constants/app-constants';
import {PackagesApiService} from '../packages-api.service';
import {PackagesStore} from '../packages.store';
import {ResourceDictionary} from './mapping-models/ResourceDictionary.model';

@Injectable({
    providedIn: 'root'
})
export class PackageCreationService {


    constructor(private api: ApiService, private packagesListService: PackagesApiService, private packagesStore: PackagesStore) {
    }

    private saveBlueprint(body: any | null, options?: any): Observable<any> {
        return this.api.post(BlueprintURLs.save, body, {responseType: 'text'});
    }

    private enrichBlueprint(body: any | null, options?: any): Observable<any> {
        return this.api.post(BlueprintURLs.enrich, body, {responseType: 'blob'});
    }

    private deployBluePrint(body: any | null, options?: any): Observable<any> {
        return this.api.post(BlueprintURLs.deploy, body, {responseType: 'text'});
    }

    async checkBluePrintNameAndVersion(name: string, version: string): Promise<boolean> {
        return await this.packagesListService.checkBluePrintIfItExists(name, version)
            .then(bluePrintModelsResult => bluePrintModelsResult != null && bluePrintModelsResult.length > 0);
    }

    refreshPackages() {
        this.packagesStore.getAll();
    }

    savePackage(blob) {
        const formData = this.getFormData(blob);
        return this.saveBlueprint(formData);
    }

    enrichPackage(blob) {
        const formData = this.getFormData(blob);
        return this.enrichBlueprint(formData);
    }

    deploy(blob) {
        const formData = this.getFormData(blob);
        return this.deployBluePrint(formData);
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


}
