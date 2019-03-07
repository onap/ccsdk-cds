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
import { IResources } from 'src/app/common/core/store/models/resources.model';
import { IResourcesState } from 'src/app/common/core/store/models/resourcesState.model';
import { IResourcesMetaData } from '../../../../common/core/store/models/resourcesMetadata.model';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { A11yModule } from '@angular/cdk/a11y';
import { LoadResourcesSuccess } from 'src/app/common/core/store/actions/resources.action';
import { IPropertyData } from 'src/app/common/core/store/models/propertyData.model';
import { IEntrySchema } from 'src/app/common/core/store/models/entrySchema.model';

@Component({
  selector: 'app-resource-metadata',
  templateUrl: './resource-metadata.component.html',
  styleUrls: ['./resource-metadata.component.scss']
})
export class ResourceMetadataComponent implements OnInit {
    entry_schema:IEntrySchema;
    properties: IPropertyData;
    ResourceMetadata: FormGroup;
    resource_name: string;
    tags: string;
    rdState: Observable<IResourcesState>;
    resources: IResources;
    propertyValues = [];
    property = [];   
  
 constructor(private formBuilder: FormBuilder, private store: Store<IAppState>) { 
    this.rdState = this.store.select('resources');
    this.ResourceMetadata = this.formBuilder.group({
        Resource_Name: ['', Validators.required],
       _tags: ['', Validators.required],
       _description : ['', Validators.required],
       _type: ['', Validators.required],
       required: ['', Validators.required],
       entry_schema: ['']
    });   
 }

 ngOnInit() {
    this.rdState.subscribe(
      resourcesdata => {
        var resourcesState: IResourcesState = { resources: resourcesdata.resources, isLoadSuccess: resourcesdata.isLoadSuccess, isSaveSuccess: resourcesdata.isSaveSuccess, isUpdateSuccess: resourcesdata.isUpdateSuccess };
        this.resource_name = resourcesState.resources.name;
        this.tags = resourcesState.resources.tags;
        this.resources = resourcesState.resources;
        this.properties= resourcesState.resources.property;
        this.propertyValues=  this.checkNested(this.properties);
        this.ResourceMetadata = this.formBuilder.group({
       Resource_Name: [this.resource_name, Validators.required],
        _tags: [this.tags, Validators.required],
        _description : [ this.propertyValues[0], Validators.required],
        _type: [ this.propertyValues[1], Validators.required],
        required: [ this.propertyValues[2], Validators.required],
        entry_schema: [this.propertyValues[3]]
      });   
    })
 }
    
 checkNested(obj) {
  for (let key in obj) {
    if (obj.hasOwnProperty(key)) {
      if (typeof obj[key] == "object"){
         console.log(`Key: ${key}`)
         this.checkNested(obj[key]);
      } else {
         this.property.push(obj[key]);             
      }   
     }
   }
   return this.property
 }
}