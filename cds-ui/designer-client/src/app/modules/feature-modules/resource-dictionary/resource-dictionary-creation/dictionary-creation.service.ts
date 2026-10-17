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
import { ResourceDictionaryURLs } from 'src/app/common/constants/app-constants';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Sources } from '../model/sources.model';
import { ApiService } from 'src/app/common/core/services/api.service';
import { EntrySchema, MetaData, Property } from '../model/metaData.model';

@Injectable({
  providedIn: 'root'
})
export class DictionaryCreationService {

  constructor(private api: ApiService) { }

  getSources() {
    return this.api.get(ResourceDictionaryURLs.getSources);
  }

  save(metadata) {
    return this.api.post(ResourceDictionaryURLs.saveDictionary, { ...metadata });
  }

  getDictionaryByName(name: string): Observable<MetaData> {
    return this.api.get(ResourceDictionaryURLs.getDictionaryByName + encodeURIComponent(name))
      .pipe(map(entry => this.toMetaData(entry)));
  }

  delete(name: string) {
    return this.api.delete(ResourceDictionaryURLs.getDictionaryByName + encodeURIComponent(name));
  }

  private toMetaData(entry): MetaData {
    const definition = entry.definition || {};
    const metaData: MetaData = Object.assign(new MetaData(), definition);
    metaData.name = definition.name || entry.name;
    metaData.tags = definition.tags || entry.tags || '';
    metaData['updated-by'] = definition['updated-by'] || entry.updatedBy;
    metaData.sources = definition.sources || {};
    metaData.property = Object.assign(new Property(), definition.property);
    metaData.property.entry_schema = metaData.property.entry_schema || new EntrySchema();
    return metaData;
  }
}
