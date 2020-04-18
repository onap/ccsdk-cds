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
  selector: 'app-dictionary-pagination',
  templateUrl: './dictionary-pagination.component.html',
  styleUrls: ['./dictionary-pagination.component.css']
})
export class DictionaryPaginationComponent implements OnInit {

  pageNumber: number;
  totalCount: number;
  pageSize: number;
  previousPage: number;

  constructor(private dictionaryStore: DictionaryStore) {
      this.pageSize = dictionaryStore.pageSize;

      this.dictionaryStore.state$
          .subscribe(state => {
              this.pageNumber = state.currentPage;
              this.totalCount = state.totalPackages;
          });
  }

  ngOnInit() {
  }

  public getPageFromService(page) {
      console.log('getPageFromService', page);
      if (isNaN(page)) {
          page = 1;
          console.log('page change to first...', page);
      }
      if (this.previousPage !== page) {
          this.dictionaryStore.getPage(page - 1, this.dictionaryStore.pageSize);
          this.previousPage = page;
      }
  }
}
