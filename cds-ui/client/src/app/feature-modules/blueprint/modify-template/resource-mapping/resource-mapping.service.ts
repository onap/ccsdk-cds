/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
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
import { HttpClient } from '@angular/common/http';
import { Observable, observable } from 'rxjs';
import { ApiService } from '../../../../common/core/services/api.service';

@Injectable()
export class ResourceMappingService {
  // blueprintUrl = '../../constants/blueprint.json';

  constructor(private api: ApiService) {
  }

  getResourceDictionaryByName(name) {
    //   return this.api.get('');

      return new Observable((observer) => {
    
        // observable execution
        observer.next({"name": "sample-input-source",
        "dataType": "string",
        "entrySchema": null,
        "definition": {
        "tags": "sample-input-source",
        "name": "sample-input-source",
        "property": {
        "description": "name of the ",
        "required": null,
        "type": "string",
        "status": null,
        "constraints": null,
        "value": null,
        "default": null,
        "entry_schema": null
        },
        "updated-by": "brindasanth@onap.com",
        "sources": {
        "input": {
        "description": null,
        "type": "source-input",
        "metadata": null,
        "directives": null,
        "properties": {
        "key": "input-source"
        },
        "attributes": null,
        "capabilities": null,
        "requirements": null,
        "interfaces": null,
        "artifacts": null,
        "copy": null,
        "node_filter": null
        }
        }
        },
        "description": "name of the ",
        "tags": "sample-input-source",
        "creationDate": "2019-04-03T10:36:31.603Z",
        "updatedBy": "brindasanth@onap.com"})
        observer.complete()
    });
  }

}