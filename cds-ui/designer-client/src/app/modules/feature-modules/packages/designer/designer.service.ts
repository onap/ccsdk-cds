/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
===================================================================
Modification Copyright (c) 2020 IBM
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

import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../common/core/services/api.typed.service';
import { ResourceDictionaryURLs, BlueprintURLs } from '../../../../common/constants/app-constants';
import { ModelType } from './model/ModelType.model';
import { BluePrintDetailModel } from '../model/BluePrint.detail.model';


@Injectable({
    providedIn: 'root'
})
export class DesignerService {

    constructor(
        private api: ApiService<ModelType>,
        private api2: ApiService<BluePrintDetailModel>
    ) {
    }

    getFunctions(modelDefinitionType: string): Observable<ModelType[]> {
        return this.api.get(ResourceDictionaryURLs.getResourceDictionary + '/' + modelDefinitionType);
    }

    private getBluePrintModel(id: string): Observable<BluePrintDetailModel> {
        return this.api2.getOne(BlueprintURLs.getOneBlueprint + '/' + id);
    }

    getPagedPackages(id: string) {
        return this.getBluePrintModel(id);
    }

    publishBlueprint(body: any | null, options?: any): Observable<any> {

        return this.api.post(BlueprintURLs.publish, body, { responseType: 'text' });
    }

}
