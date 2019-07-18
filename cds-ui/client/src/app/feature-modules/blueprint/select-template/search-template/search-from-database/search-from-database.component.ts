/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 IBM Intellectual Property. All rights reserved.
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

import { Component, OnInit, ViewChild, EventEmitter, Output  } from '@angular/core';
import { FormBuilder, FormGroup, Validators} from '@angular/forms';
import { SearchTemplateService } from '../search-template.service';
import { MatAutocompleteTrigger } from '@angular/material';
import { SearchPipe } from 'src/app/common/shared/pipes/search.pipe';
@Component({
  selector: 'app-search-from-database',
  templateUrl: './search-from-database.component.html',
  styleUrls: ['./search-from-database.component.scss']
})
export class SearchFromDatabaseComponent implements OnInit {

  myControl: FormGroup;
  @Output() resourcesData = new EventEmitter();  
  options: any[]   = [];
  @ViewChild('resourceSelect', { read: MatAutocompleteTrigger }) resourceSelect: MatAutocompleteTrigger;

  searchText: string = '';
  constructor(private _formBuilder: FormBuilder,
              private existingModelService: SearchTemplateService)  { }
  
 ngOnInit() {
    this.myControl = this._formBuilder.group({
      search_input: ['', Validators.required]
    });
  }
 selected(value){
   this.resourcesData.emit(value);
   }

   fetchResourceByName() {
      this.existingModelService.searchByTags("/searchByTags/",this.searchText)
      .subscribe(data=>{
          console.log(data);
          data.forEach(element => {
            this.options.push(element)
          });          
        this.resourceSelect.openPanel();
      }, error=>{
        window.alert('error' + error);
      })
   }

}
