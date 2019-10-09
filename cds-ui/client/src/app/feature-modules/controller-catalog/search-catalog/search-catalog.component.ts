/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2019 TechMahindra
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

import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SearchCatalogService } from './search-catalog.service';
import { MatAutocompleteTrigger } from '@angular/material';

@Component({
  selector: 'app-search-catalog',
  templateUrl: './search-catalog.component.html',
  styleUrls: ['./search-catalog.component.scss']
})
export class SearchCatalogComponent implements OnInit {
  myControl: FormGroup;
  searchText: string = '';
  options: any[]   = [];
  @ViewChild('catalogSelect', { read: MatAutocompleteTrigger }) catalogSelect: MatAutocompleteTrigger;
  constructor(private _formBuilder: FormBuilder, private searchCatalogService: SearchCatalogService)  { }
  
 ngOnInit() {
    this.myControl = this._formBuilder.group({
      search_input: ['', Validators.required]
    });
  }
  fetchCatalogByName() {
    this.searchCatalogService.searchByTags(this.searchText)
    .subscribe(data=>{
        console.log(data);
        data.forEach(element => {
          this.options.push(element)
        });          
      this.catalogSelect.openPanel();
    }, error=>{
      window.alert('Catalog not matching the search tag' + error);
    })
   }

   editInfo(artifactName: string, artifactVersion: string, option: string) {
  }
}
