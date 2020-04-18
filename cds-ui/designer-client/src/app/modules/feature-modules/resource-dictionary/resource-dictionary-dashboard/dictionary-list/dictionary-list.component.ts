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
import { DictionaryModel } from '../../model/dictionary.model';
import { DictionaryStore } from '../../dictionary.store';

@Component({
  selector: 'app-dictionary-list',
  templateUrl: './dictionary-list.component.html',
  styleUrls: ['./dictionary-list.component.css']
})
export class DictionaryListComponent implements OnInit {
  viewedDictionary: DictionaryModel[] = [];

  constructor(private dictionaryStore: DictionaryStore) {
      console.log('DictionaryListComponent');
      this.dictionaryStore.state$.subscribe(state => {
          console.log(state);
          if (state.filteredPackages) {
              this.viewedDictionary = state.filteredPackages.content;
          }
      });
  }

  ngOnInit() {
      this.dictionaryStore.getAll();
  }

  testDispatch(dictionary: DictionaryModel) {
      console.log(dictionary.name);
  }

}
