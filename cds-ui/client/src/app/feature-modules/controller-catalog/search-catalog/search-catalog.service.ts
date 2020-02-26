/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2019 TechMahindra
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
import { HttpClient } from '@angular/common/http';
import { Observable, observable } from 'rxjs';
import { ControllerCatalogURLs } from 'src/app/common/constants/app-constants';
import { ApiService } from 'src/app/common/core/services/api.service';

@Injectable()
export class SearchCatalogService {

  constructor(private _http: HttpClient, private api: ApiService) {
  }

  searchByTags(tag) {
    return this.api.get(ControllerCatalogURLs.searchControllerCatalogByTags + '/' + tag);
  }
  deleteCatalog(modelName) {
    return this.api.delete(ControllerCatalogURLs.deleteCatalog + '/' + modelName);
}
}
