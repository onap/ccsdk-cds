/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
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

import {getService, juggler} from '@loopback/service-proxy';
import {inject, Provider} from '@loopback/core';
import {RestDataSource} from '../datasources/rest.datasource';


export interface RestResponseData {
   userId: number;
   id: number;
   title: string;
   completed: boolean;
}

export interface RestService {
   getrestdata(id?: number): Promise<RestResponseData>;
}
export class RestProvider implements Provider<RestService> {
   constructor(
      @inject('datasources.rest')
      protected dataSource: juggler.DataSource = new RestDataSource(),
   ) {}
   
   value(): Promise<RestService> {
      return getService(this.dataSource);
   }
}