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

import { Component, OnInit, Output, EventEmitter} from '@angular/core';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-template-options',
  templateUrl: './template-options.component.html',
  styleUrls: ['./template-options.component.scss']
})
export class TemplateOptionsComponent implements OnInit {
  selectedOption: string;
  @Output() option = new EventEmitter();

  constructor() { }

  ngOnInit() {
  }

  selected(value){
    console.log(value);
    this.option.emit(value);
  }
  // loadTemplateData() {
  //  // to do
  // }
}
