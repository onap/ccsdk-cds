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
import { CdkDragDrop, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { SourcesStore } from './sources.store';
import { DictionaryCreationService } from '../dictionary-creation.service';

@Component({
  selector: 'app-sources-template',
  templateUrl: './sources-template.component.html',
  styleUrls: ['./sources-template.component.css']
})
export class SourcesTemplateComponent implements OnInit {
  private searchQuery = '';
  lang = 'json';
  sources = {};
  option = [];
  sourcesOptions = [];
  textValue: any;
  selectItem: boolean;
  ddSource = [];
  checked: boolean;
  searchText = '';
  text = '';
  selectedArray = [];
  constructor(
    private sourcesStore: SourcesStore,
    private dictionaryCreationService: DictionaryCreationService
  ) {

  }

  ngOnInit() {
    //  this.sourcesStore.getAllSources();
    this.dictionaryCreationService.getSources().subscribe(sources => {
      // console.log(sources);
      this.sources = sources[0];
      // this.sources = {
      //   "input": "source-input", "rest": "source-rest", "default":"source-default", "capability": "source-capability",
      // "sdnc": "source-rest", "vault-data": "source-rest", "processor-db": "source-db", "aai-data": "source-rest",
      // "script": "source-capability"
      // };
      for (const key in this.sources) {
        if (key) {
          console.log(key + ' - ' + this.sources[key]);
          const sourceObj = { name: key, value: this.sources[key] };
          this.option.push(sourceObj);
        }
      }

    });
  }

  saveSorcesDataToStore() {
    this.sourcesStore.saveSources(this.ddSource);
  }

  drop(event: CdkDragDrop<string[]>) {
    console.log('-------------');
    console.log(event);
    this.ddSource = [];
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex);
    }

    for (const key2 in this.sources) {
      if (key2) {
        const originalSources = this.sourcesOptions;
        for (const key of originalSources) {
          /* tslint:disable:no-string-literal */
          if (key.name === key2['name']) {
            const obj = `{${key.name}: ${key.value}}`;
            this.ddSource.push(obj);
          }
        }
      }
    }
  }

  searchDictionary(event: any) {
    this.searchQuery = event.target.value;
    this.searchQuery = this.searchQuery.trim();
    console.log(this.searchQuery);
    // this.dictionaryStore.search(this.searchQuery);
  }

  onChange(item: string, isChecked: boolean) {
    if (isChecked) {
      this.selectedArray.push(item);
    }
  }

  textChanged(event, item) {
    const editedData = JSON.parse(event);
    const originalSources = this.sources;
    for (const key in originalSources) {
      if (key === item.name) {
        this.sources[key] = editedData;
      }
    }
    this.option = [];
    this.sourcesStore.changeSources(this.sources);
  }

}
