/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2020 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

import { Injectable } from '@angular/core';
import { DictionaryPage } from './model/dictionary.model';
import { ApiService } from 'src/app/common/core/services/api.typed.service';
import { ResourceDictionaryURLs } from 'src/app/common/constants/app-constants';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DictionaryApiService {

  constructor( private api: ApiService<DictionaryPage>) { }

  getPagedDictionary(pageNumber: number, pageSize: number, sortBy: string): Observable<DictionaryPage[]> {
    return this.api.get(ResourceDictionaryURLs.getPagedDictionary, {
      offset: pageNumber,
      limit: pageSize,
      sort: sortBy
    });
  }
  getPagedDictionaryByKeyWord(keyWord: string, pageNumber: number, pageSize: number, sortBy: string): Observable<DictionaryPage[]> {
    return this.api.get(ResourceDictionaryURLs.getMetaDatePageable + '/' + keyWord, {
        offset: pageNumber,
        limit: pageSize,
        sort: sortBy
    });
  }
}
