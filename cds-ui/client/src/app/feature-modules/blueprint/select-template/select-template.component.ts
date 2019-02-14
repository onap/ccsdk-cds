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
import { Component, OnInit } from '@angular/core';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';
import { IBlueprint } from 'src/app/common/core/store/models/blueprint.model';
import { IBlueprintState } from 'src/app/common/core/store/models/blueprintState.model';
import { IMetaData } from 'src/app/common/core/store/models/metadata.model';
import { IImportModel } from 'src/app/common/core/store/models/imports.model';
import { ITopologyTemplate } from 'src/app/common/core/store/models/itopologytemplate.model';

@Component({
  selector: 'app-select-template',
  templateUrl: './select-template.component.html',
  styleUrls: ['./select-template.component.scss']
})
export class SelectTemplateComponent implements OnInit {
  blueprint: IBlueprint;
  topologyTemplate: ITopologyTemplate;
  metaData: IMetaData;
  blueprintState: IBlueprintState;
  importModel: IImportModel;

  constructor(private store: Store<IBlueprintState>) {
    this.importModel.file = '';
  }

  ngOnInit() {
  }
  fileChange(topologyTemp: ITopologyTemplate) {
    this.topologyTemplate = topologyTemp;
    console.log(topologyTemp);
  }
  metaDataDetail(data: IMetaData) {
    
    this.metaData = data;
    console.log("parent" + this.metaData.author_email);
  }
  upload() {

  }
  saveBlueprintModel(){
    this.blueprint.toplogyTemplates=this.topologyTemplate;
    this.blueprint.metadata= this.metaData;
   // this.store.dispatch(new CreateBlueprint(this.blueprint));
  }
}
