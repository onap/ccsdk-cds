/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2020 TechMahindra
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
import { DictionaryCreationService } from '../dictionary-creation.service';
import { DictionaryCreationStore } from '../dictionary-creation.store';

@Component({
  selector: 'app-dictionary-editor',
  templateUrl: './dictionary-editor.component.html',
  styleUrls: ['./dictionary-editor.component.css']
})
export class DictionaryEditorComponent implements OnInit {
  text = '';
  constructor(
    private dictionaryStore: DictionaryCreationStore,
    private dictionaryService: DictionaryCreationService
  ) {
  }

  ngOnInit() {
  }

  textChanged(event) {
    console.log(JSON.parse(event));
    this.dictionaryStore.changeMetaData(JSON.parse(event));
  }
}

