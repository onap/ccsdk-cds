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
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { DictionaryCreationService } from './dictionary-creation.service';
import { ApiService } from 'src/app/common/core/services/api.service';
import { MetaData } from '../model/metaData.model';

describe('DictionaryCreationService', () => {
  let service: DictionaryCreationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.get(DictionaryCreationService);
    httpMock = TestBed.get(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('loads a dictionary entry by name as editable metadata', () => {
    let metaData: MetaData;
    service.getDictionaryByName('vnf name').subscribe(result => metaData = result);

    httpMock.expectOne({ method: 'GET', url: '/resourcedictionary/vnf%20name' }).flush({
      name: 'vnf name',
      tags: 'vnf, name',
      updatedBy: 'designer',
      definition: {
        name: 'vnf name',
        tags: 'vnf, name',
        group: 'default',
        property: { type: 'string', description: 'VNF name', entry_schema: null },
        sources: { input: { type: 'source-input' } }
      }
    });

    expect(metaData.name).toBe('vnf name');
    expect(metaData.tags).toBe('vnf, name');
    expect(metaData['updated-by']).toBe('designer');
    expect(metaData.property.type).toBe('string');
    expect(metaData.property.entry_schema.type).toBe('');
    expect(metaData.sources).toEqual({ input: { type: 'source-input' } });
    expect((metaData as any).group).toBe('default');
  });

  it('deletes a dictionary entry by name', () => {
    let deleted = false;
    service.delete('vnf name').subscribe(() => deleted = true);

    httpMock.expectOne({ method: 'DELETE', url: '/resourcedictionary/vnf%20name' }).flush(null);

    expect(deleted).toBe(true);
  });
});
