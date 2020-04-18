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
import { DictionaryModel, DictionaryPage } from '../../model/dictionary.model';
import { DictionaryStore } from '../../dictionary.store';

@Component({
  selector: 'app-filterby-tags',
  templateUrl: './filterby-tags.component.html',
  styleUrls: ['./filterby-tags.component.css']
})
export class FilterbyTagsComponent implements OnInit {
  page: DictionaryPage;
  tags: string[] = [];
  viewedTags: string[] = [];
  searchTag = '';
  viewedDictionary: DictionaryModel[] = [];
  private checkBoxTages = '';
  currentPage = 0;

  constructor(private dictionaryStore: DictionaryStore) {
      this.dictionaryStore.state$.subscribe(state => {
          console.log(state);
          if (state.page) {
              this.viewedDictionary = state.page['content'];
              this.tags = [];
              if (state.currentPage !== this.currentPage) {
                  this.checkBoxTages = '';
                  this.currentPage = state.currentPage;
              }
              this.viewedDictionary.forEach(element => {
                  element.tags.split(',').forEach(tag => {
                      this.tags.push(tag.trim());
                  });
                  this.tags.push('All');
                  this.tags = this.tags.filter((value, index, self) => self.indexOf(value) === index);
                  this.assignTags();
              });
          }
      });
  }

  ngOnInit() {

  }

  reloadChanges(event: any) {
      this.searchTag = event.target.value;
      this.filterItem(this.searchTag);
  }

  private assignTags() {
      this.viewedTags = this.tags;
  }

  private filterItem(value) {
      if (!value) {
          this.assignTags();
      }
      this.viewedTags = this.tags.filter(
          item => item.toLowerCase().indexOf(value.toLowerCase()) > -1
      );
  }

  reloadDictionary(event: any) {
      if (!event.target.checked) {
          this.checkBoxTages = this.checkBoxTages.replace(event.target.id + ',', '')
              .replace(event.target.id, '');
      } else {
          this.checkBoxTages += event.target.id.trim() + ',';
      }
      const tagsSelected = this.checkBoxTages.split(',').filter(item => {
          if (item) {
              return true;
          }
      }).map((item) => {
          return item.trim();
      });
      this.dictionaryStore.filterByTags(tagsSelected);
  }

}