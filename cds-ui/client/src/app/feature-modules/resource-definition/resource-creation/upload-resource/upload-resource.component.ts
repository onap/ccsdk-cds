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

import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { IResources } from 'src/app/common/core/store/models/resources.model';
import { IResourcesState } from 'src/app/common/core/store/models/resourcesState.model';
import { LoadResourcesSuccess } from 'src/app/common/core/store/actions/resources.action';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';

@Component({
  selector: 'app-upload-resource',
  templateUrl: './upload-resource.component.html',
  styleUrls: ['./upload-resource.component.scss']
})
export class UploadResourceComponent implements OnInit {

  @Output() fileData = new EventEmitter();
  file: File;
  localResourcesData: IResources;
  fileText: object[];
  blueprintState: IResourcesState;
  bpState: Observable<IResourcesState>;

  constructor(private store: Store<IAppState>) { }

  ngOnInit() {
  }

  fileChanged(e: any) {
    this.file = e.target.files[0];
    this.fileData.emit(this.file);  
  }
  
}
