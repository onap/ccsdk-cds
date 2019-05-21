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
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Observable } from 'rxjs';
import { Store } from '@ngrx/store';

import { IAppState } from '../../../../common/core/store/state/app.state';
import { IBlueprintState } from 'src/app/common/core/store/models/blueprintState.model';
import { IBlueprint } from 'src/app/common/core/store/models/blueprint.model';
import { IMetaData } from '../../../../common/core/store/models/metadata.model';
import { SetBlueprintState } from 'src/app/common/core/store/actions/blueprint.action';

@Component({
  selector: 'app-metadata',
  templateUrl: './metadata.component.html',
  styleUrls: ['./metadata.component.scss']
})
export class MetadataComponent implements OnInit {
  CBAMetadataForm: FormGroup;
  metadata: IMetaData;
  bpState: Observable<IBlueprintState>;
  blueprint: IBlueprint;
  filesTree: any = [];
  filesData: any = [];
  selectedFile: string;
  zipFolder: any;
  blueprintName: string;
  uploadedFileName: string;
  entryDefinition: string;
  
  constructor(private formBuilder: FormBuilder, private store: Store<IAppState>) {
    this.bpState = this.store.select('blueprint');
    this.CBAMetadataForm = this.formBuilder.group({
      template_author: ['', Validators.required],
      author_email: ['', Validators.required],
      user_groups: ['', Validators.required],
      template_name: ['', Validators.required],
      template_version: ['', Validators.required],
      template_tags: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.bpState.subscribe(
      blueprintdata => {
        var blueprintState: IBlueprintState = { blueprint: blueprintdata.blueprint, isLoadSuccess: blueprintdata.isLoadSuccess, isSaveSuccess: blueprintdata.isSaveSuccess, isUpdateSuccess: blueprintdata.isUpdateSuccess };
        this.blueprint = blueprintState.blueprint;
        this.filesTree = blueprintdata.files;
        this.filesData = blueprintdata.filesData;
        this.blueprintName = blueprintdata.name;
        this.uploadedFileName = blueprintdata.uploadedFileName;
        this.entryDefinition = blueprintdata.entryDefinition;



        var blueprintState: IBlueprintState = { blueprint: blueprintdata.blueprint, isLoadSuccess: blueprintdata.isLoadSuccess, isSaveSuccess: blueprintdata.isSaveSuccess, isUpdateSuccess: blueprintdata.isUpdateSuccess };
        this.metadata = blueprintState.blueprint.metadata;
        this.blueprint = blueprintState.blueprint;
        let metadatavalues = [];
        for (let key in this.metadata) {
          if (this.metadata.hasOwnProperty(key)) {
            metadatavalues.push(this.metadata[key]);
          }
        }
        let temp_author = metadatavalues[0];
        console.log(temp_author);
        this.CBAMetadataForm = this.formBuilder.group({
          template_author: [metadatavalues[0], Validators.required],
          author_email: [metadatavalues[1], Validators.required],
          user_groups: [metadatavalues[2], Validators.required],
          template_name: [metadatavalues[3], Validators.required],
          template_version: [metadatavalues[4], Validators.required],
          template_tags: [metadatavalues[5], Validators.required]
        });
      })
  }

  UploadMetadata() {
    this.metadata = Object.assign({}, this.CBAMetadataForm.value);
    this.blueprint.metadata = this.metadata;

    this.filesData.forEach((fileNode) => {
      if (fileNode.name.includes(this.blueprintName) && fileNode.name == this.entryDefinition) {
        fileNode.data = JSON.stringify(this.blueprint, null, "\t");
      }
    });
    let blueprintState = {
      blueprint: this.blueprint,
      name: this.blueprintName,
      files: this.filesTree,
      filesData: this.filesData,
      uploadedFileName: this.uploadedFileName,
      entryDefinition: this.entryDefinition
    }
    this.store.dispatch(new SetBlueprintState(blueprintState));
  }

}