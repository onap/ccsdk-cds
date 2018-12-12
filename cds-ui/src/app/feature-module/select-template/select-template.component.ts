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
import { Appstate } from '../../common/store/app.state';
import { Observable} from 'rxjs';
import { Store } from '@ngrx/store';
import { Blueprint } from '../../common/store/models/blueprint.model';

@Component({
  selector: 'app-select-template',
  templateUrl: './select-template.component.html',
  styleUrls: ['./select-template.component.scss']
})
export class SelectTemplateComponent implements OnInit {
  blueprint: any
  constructor(private store: Store<Appstate>) {
    this.blueprint = store.select('blueprint').subscribe(data =>{
       console.log(data);      
    });
   }

  ngOnInit() {
  }

}
