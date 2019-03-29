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

import { Component, OnInit, ViewChild, EventEmitter, Output } from '@angular/core';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { IResources } from 'src/app/common/core/store/models/resources.model';
import { IResourcesState } from 'src/app/common/core/store/models/resourcesState.model';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { A11yModule } from '@angular/cdk/a11y';
import { LoadResourcesSuccess } from 'src/app/common/core/store/actions/resources.action';
import { ISourcesData } from 'src/app/common/core/store/models/sourcesData.model';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';

@Component({
  selector: 'app-sources-template',
  templateUrl: './sources-template.component.html',
  styleUrls: ['./sources-template.component.scss']
})
export class SourcesTemplateComponent implements OnInit {

    @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;
    options = new JsonEditorOptions(); 
    rdState: Observable<IResourcesState>;
    resources: IResources;
    option = ['mdsal','default'];
    sources:ISourcesData; 
    sourcesOptions = [];
    sourcesData = [];
    @Output() resourcesData = new EventEmitter();  
 
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
        this.sources = resourcesState.resources.sources;
        for (let key in this.sources) {
            this.sourcesOptions.push(key);  
        }
    })
 }

 onChange(item,$event) {
    var editedData =JSON.parse($event.srcElement.value);
    var originalSources = this.resources.sources;
     for (let key in originalSources){
        if(key == item){
            originalSources[key] = editedData;
        }
     }
     this.resources.sources = Object.assign({},originalSources);
 };
    
 selected(value){
 	this.sourcesData=this.sources[value];
    return this.sourcesData;    
 }    

 delete(item,i){
 	if(confirm("Are sure you want to delete this source ?")) {
    	var originalSources = this.resources.sources;
    	for (let key in originalSources){
     		if(key == item){    
      			delete originalSources[key];
      		}
     	}
    	this.resources.sources = Object.assign({},originalSources);
  		this.sourcesOptions.splice(i,1);
  	}     
 } 
  
 UploadSourcesData() {
  	this.resourcesData.emit(this.resources);        
  }
    
 drop(event: CdkDragDrop<string[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data,
                        event.container.data,
                        event.previousIndex,
                        event.currentIndex);
    }
  }    
}
