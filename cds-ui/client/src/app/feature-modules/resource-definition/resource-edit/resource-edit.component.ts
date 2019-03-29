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

import { Component, OnInit, ViewChild } from '@angular/core';
import { IResources } from 'src/app/common/core/store/models/resources.model';
import { IResourcesState } from 'src/app/common/core/store/models/resourcesState.model';
import { LoadResourcesSuccess,UpdateResources,SetResourcesState } from 'src/app/common/core/store/actions/resources.action';
import { Store } from '@ngrx/store';
import { IAppState } from '../../../common/core/store/state/app.state';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';
import { Observable } from 'rxjs';
import { A11yModule } from '@angular/cdk/a11y';

@Component({
  selector: 'app-resource-edit',
  templateUrl: './resource-edit.component.html',
  styleUrls: ['./resource-edit.component.scss']
})
export class ResourceEditComponent implements OnInit {

    resources:IResources;
    data:IResources;
    rdState: Observable<IResourcesState>;
    designerMode: boolean = true;
    editorMode: boolean = false;
    viewText: string = "Open in Editor Mode";
    @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;
    options = new JsonEditorOptions();
  
  constructor(private store: Store<IAppState>) {
  	this.rdState = this.store.select('resources');
    this.options.mode = 'text';
    this.options.modes = [ 'text', 'tree', 'view'];
    this.options.statusBar = false;    
  }

  ngOnInit() {
    this.rdState.subscribe(
      resourcesdata => {
        var resourcesState: IResourcesState = { resources: resourcesdata.resources, isLoadSuccess: resourcesdata.isLoadSuccess, isSaveSuccess: resourcesdata.isSaveSuccess, isUpdateSuccess: resourcesdata.isUpdateSuccess };
          this.resources=resourcesState.resources;
    })     
  }

 metaDataDetail(data: IResources) {
    this.data=data;       
  }
    
 sourcesDetails(data: IResources) {
    this.data=data; 
 }
    
 onChange($event) {
      this.data=JSON.parse($event.srcElement.value);
  };
  
 updateResourcesState(){
      console.log(this.data);
      let resourcesState = {
      resources: this.data,
      isLoadSuccess: true,
      isUpdateSuccess:true,
      isSaveSuccess:true
    }  
   this.store.dispatch(new SetResourcesState(resourcesState));   
  }
    
 changeView() {
    if(this.viewText == 'Open in Editor Mode') {
      this.editorMode =  true;
      this.designerMode = false;
      this.viewText = 'Open in Form Mode'
    } else {
      this.editorMode =  false;
      this.designerMode = true;
      this.viewText = 'Open in Editor Mode'
    }
  }  
}
