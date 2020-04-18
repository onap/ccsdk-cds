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
import { DictionaryStore } from '../../dictionary.store';

@Component({
  selector: 'app-sort-dictionary',
  templateUrl: './sort-dictionary.component.html',
  styleUrls: ['./sort-dictionary.component.css']
})
export class SortDictionaryComponent implements OnInit {
  sortTypes: string[];
  selected: string;

  constructor(private dictionaryStore: DictionaryStore) {
      this.sortTypes = Object.keys(SortByToServerValue);
      this.selected = 'Recent';
  }

  ngOnInit() {
  }

  sortDictionary(event: any) {
      const key = event.target.name;
      console.log(key);
      this.selected = key;
      this.dictionaryStore.sortPagedDictionary(SortByToServerValue[key]);
  }
}

enum SortByToServerValue {
    Recent = 'DATE',
    Name = 'NAME',
}