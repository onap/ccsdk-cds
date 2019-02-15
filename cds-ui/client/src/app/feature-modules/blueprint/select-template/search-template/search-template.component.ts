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

import { Component, OnInit, EventEmitter, Output } from '@angular/core';
import { Store } from '@ngrx/store';
import { IBlueprint} from '../../../../common/core/store/models/blueprint.model';

@Component({
  selector: 'app-search-template',
  templateUrl: './search-template.component.html',
  styleUrls: ['./search-template.component.scss']
})
export class SearchTemplateComponent implements OnInit {
  file: any;
  localBluePrintData: IBlueprint;
  fileText: object[];
  
  constructor() { }

  ngOnInit() {
  }

  fileChanged(e: any) {
    this.file = e.target.files[0];
    let fileReader = new FileReader();
    fileReader.readAsText(e.srcElement.files[0]);
    var me = this;
    fileReader.onload = function () {
      let fileData = JSON.stringify(fileReader.result);
      me.localBluePrintData = JSON.parse(fileData);
      console.log(me.localBluePrintData);
    }
  }
  extractBlueprint(){
  }
}
