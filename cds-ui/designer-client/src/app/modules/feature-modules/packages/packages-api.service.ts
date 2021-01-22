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
import {ApiService} from '../../../common/core/services/api.typed.service';
import {BlueprintURLs} from '../../../common/constants/app-constants';
import {BlueprintModel, BlueprintPage} from './model/Blueprint.model';


@Injectable({
    providedIn: 'root'
})
export class PackagesApiService {
    packages: BlueprintModel[] = [];
    private numberOfPackages: number;

    constructor(private api: ApiService<BlueprintPage>) {
    }

    getPagedPackages(pageNumber: number, pageSize: number, sortBy: string): Observable<BlueprintPage[]> {
        const sortType = sortBy.includes('DATE') ? 'DESC' : 'ASC';
        return this.api.get(BlueprintURLs.getPagedBlueprints, {
            offset: pageNumber,
            limit: pageSize,
            sort: sortBy,
            sortType
        });
    }

    async checkBlueprintIfItExists(name: string, version: string): Promise<BlueprintPage[]> {
        return await this.api.get(BlueprintURLs.getBlueprintByName + '/' + name + '/version/' + version).toPromise();
    }

    getCountOfAllPackages(observable: Observable<number>) {
        observable.subscribe(data => {
            this.numberOfPackages = data;
            console.log(data);
        });
    }

    getPagedPackagesByKeyWord(keyWord: string, pageNumber: number, pageSize: number, sortBy: string) {
        const sortType = sortBy.includes('DATE') ? 'DESC' : 'ASC';
        return this.api.get(BlueprintURLs.getMetaDatePageable + '/' + keyWord, {
            offset: pageNumber,
            limit: pageSize,
            sort: sortBy,
            sortType
        });
    }
}
