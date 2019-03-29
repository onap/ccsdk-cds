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
import { Store } from '@ngrx/store';
import { IResources } from 'src/app/common/core/store/models/resources.model';
import { IAppState } from 'src/app/common/core/store/state/app.state';
import { LoadResourcesSuccess } from 'src/app/common/core/store/actions/resources.action';

@Component({
  selector: 'app-existing-model',
  templateUrl: './existing-model.component.html',
  styleUrls: ['./existing-model.component.scss']
})
export class ExistingModelComponent implements OnInit {

  resourceName:string;
  
 constructor(private store: Store<IAppState>) { }

  ngOnInit() {
 }
 
 selectedResource(value){
  console.log(value);
  this.resourceName=value;
 }

 getDataUsingResouceName(){
 
 }

 updateResourcesState() {
    var me = this; 
    var data:IResources;
    me.store.dispatch(new LoadResourcesSuccess(data));   
 }


}
