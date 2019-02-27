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

import { Component, OnInit } from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import sample_licenses from '../../../../../assets/sample-licenses.json';

@Component({
  selector: 'app-resource-metadata',
  templateUrl: './resource-metadata.component.html',
  styleUrls: ['./resource-metadata.component.scss']
})
export class ResourceMetadataComponent implements OnInit {

    ResourceMetadata: FormGroup;
   data=sample_licenses;
    
    constructor(private _formBuilder: FormBuilder) {
      this.ResourceMetadata = this._formBuilder.group({
      Resource_Name: ['', Validators.required],
      _tags: ['', Validators.required],
      _description : ['', Validators.required],
      _type: ['', Validators.required],
      _required: ['', Validators.required],
      entry_schema: ['', Validators.required]
    });
    }

    ngOnInit() {
        this.ResourceMetadata = this._formBuilder.group({
      Resource_Name: [this.data["name"], Validators.required],
      _tags: [this.data["tags"], Validators.required],
      _required : [this.data["property"]["required"], Validators.required],
      _description : [this.data["property"]["description"], Validators.required],
      _type: [this.data["property"]["type"], Validators.required],
      entry_schema: [this.data["property"]["entry_schema"]["type"], Validators.required]
    });    
     
  }
}
