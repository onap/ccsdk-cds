/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright 2019 TechMahindra
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

import {getService} from '@loopback/service-proxy';
import {inject, Provider} from '@loopback/core';
import {ResourcedictionaryDataSource} from '../datasources';

export interface ResourcedictionaryService {
   getAllresourcedictionary(authtoken: string): Promise<any>;
}

export class ResourcedictionaryServiceProvider implements Provider<ResourcedictionaryService> {
  constructor(
    @inject('datasources.resourcedictionary')
    protected dataSource: ResourcedictionaryDataSource = new ResourcedictionaryDataSource(),
  ) {}

  value(): Promise<ResourcedictionaryService> {
    return getService(this.dataSource);
  }
}


