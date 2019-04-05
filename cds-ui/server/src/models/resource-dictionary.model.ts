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



import {Entity, model, property} from '@loopback/repository';

@model({settings: {"strict":false}})
export class ResourceDictionary extends Entity {
  @property({
    type: 'number',
    id: true,
    required: true,
  })
  id : string;

  @property({
    type: 'string',
  })
  tags?: string;

  @property({
    type: 'string',
  })
  updatedby?: string;

  @property({
    type: 'object',
  })
  property?: object;

  @property({
    type: 'object',
  })
  sources?: object;

  // Define well-known properties here

  // Indexer property to allow additional data
  [prop: string]: any;

  constructor(data?: Partial<ResourceDictionary>) {
    super(data);
  }
}
