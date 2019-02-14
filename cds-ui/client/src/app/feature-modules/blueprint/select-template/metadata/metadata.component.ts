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

import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { IMetaData } from '../../../../common/core/store/models/metadata.model';
import { A11yModule } from '@angular/cdk/a11y';

@Component({
  selector: 'app-metadata',
  templateUrl: './metadata.component.html',
  styleUrls: ['./metadata.component.scss']
})
export class MetadataComponent implements OnInit {
  CBAMetadataForm: FormGroup;
  metadata: IMetaData;
  @Output() metadataform = new EventEmitter<IMetaData>();

  constructor(private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.CBAMetadataForm = this.formBuilder.group({
      template_author: ['', Validators.required],
      author_email: ['', Validators.required],
      user_groups: ['', Validators.required],
      template_name: ['', Validators.required],
      template_version: ['', Validators.required],
      template_tags: ['', Validators.required]
    });
  }
  UploadMetadata() {
    this.metadata = Object.assign({}, this.CBAMetadataForm.value);
    console.log(this.metadata.template_author);
    this.metadataform.emit(this.metadata);
  }

}