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
import { IBlueprint } from '../../../../common/core/store/models/blueprint.model';
import { IBlueprintState } from '../../../../common/core/store/models/blueprintState.model';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { LoadBlueprintSuccess } from '../../../../common/core/store/actions/blueprint.action';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-search-template',
  templateUrl: './search-template.component.html',
  styleUrls: ['./search-template.component.scss']
})
export class SearchTemplateComponent implements OnInit {
  file: File;
  localBluePrintData: IBlueprint;
  fileText: object[];
  blueprintState: IBlueprintState;
  bpState: Observable<IBlueprintState>;

  constructor(private store: Store<IAppState>) { }

  ngOnInit() {
  }

  fileChanged(e: any) {
    this.file = e.target.files[0];
  }
  
  updateBlueprintState() {
    let fileReader = new FileReader();
    fileReader.readAsText(this.file);
    var me = this;
    fileReader.onload = function () {
      var data: IBlueprint = JSON.parse(fileReader.result.toString());
      me.store.dispatch(new LoadBlueprintSuccess(data));
    }
  }
}
