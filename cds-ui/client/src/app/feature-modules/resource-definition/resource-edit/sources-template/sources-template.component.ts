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

import { Component, OnInit } from '@angular/core';
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { IResources } from 'src/app/common/core/store/models/resources.model';
import { IResourcesState } from 'src/app/common/core/store/models/resourcesState.model';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { A11yModule } from '@angular/cdk/a11y';
import { LoadResourcesSuccess } from 'src/app/common/core/store/actions/resources.action';
import { ISourcesData } from 'src/app/common/core/store/models/sourcesData.model';

@Component({
  selector: 'app-sources-template',
  templateUrl: './sources-template.component.html',
  styleUrls: ['./sources-template.component.scss']
})
export class SourcesTemplateComponent implements OnInit {
    rdState: Observable<IResourcesState>;
    resources: IResources;
    todo = [];
    sources:ISourcesData; 
    sourcesOptions = [];

 constructor(private store: Store<IAppState>) {
    this.rdState = this.store.select('resources');
 }

 ngOnInit() {
    this.rdState.subscribe(
      resourcesdata => {
        var resourcesState: IResourcesState = { resources: resourcesdata.resources, isLoadSuccess: resourcesdata.isLoadSuccess, isSaveSuccess: resourcesdata.isSaveSuccess, isUpdateSuccess: resourcesdata.isUpdateSuccess };
        this.sources = resourcesState.resources.sources;
        for (let key in this.sources) {
          if (this.sources.hasOwnProperty(key)) {
            this.sourcesOptions.push(this.sources[key]);             
          }
        }
        console.log(this.sourcesOptions);
    })
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
