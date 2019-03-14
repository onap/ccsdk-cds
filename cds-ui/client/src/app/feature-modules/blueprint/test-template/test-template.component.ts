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

import { Component, OnInit, ViewChild } from '@angular/core';
import { FlatTreeControl } from '@angular/cdk/tree';
import { MatTreeFlatDataSource, MatTreeFlattener } from '@angular/material/tree';
import { Observable, Subscription } from 'rxjs';
import { Store } from '@ngrx/store';

import { IAppState } from '../../../common/core/store/state/app.state';
import { IBlueprintState } from 'src/app/common/core/store/models/blueprintState.model';
import { IBlueprint } from 'src/app/common/core/store/models/blueprint.model';
import { IMetaData } from '../../../common/core/store/models/metadata.model';
import { LoadBlueprintSuccess } from 'src/app/common/core/store/actions/blueprint.action';

import "ace-builds/webpack-resolver";
import 'brace';
import 'brace/ext/language_tools';
import 'ace-builds/src-min-noconflict/snippets/html';

@Component({
  selector: 'app-test-template',
  templateUrl: './test-template.component.html',
  styleUrls: ['./test-template.component.scss']
})
export class TestTemplateComponent implements OnInit {
  private blueprintpState: Subscription;
  private request;
  private workflows = [];
  @ViewChild('editor') editor;
  options: any = { fontSize: "100%", printMargin: false, tabSize: 2 };

  constructor(private store: Store<IAppState>) {
    this.blueprintpState = this.store.select('blueprint')
      .subscribe((data: any) => {
        console.log(data);
        if (data && data.blueprint && data.blueprint.topology_template && data.blueprint.topology_template.workflows) {
          this.buildWorkflowData(data.blueprint.topology_template.workflows);
          // this.request = JSON.stringify(data.blueprint.topology_template.workflows[0], undefined, 4);
        }
      });

  }

  ngOnInit() {
  }

  fileClicked(file) {
    console.log('selected file:' + file);
  }

  buildWorkflowData(data) {
    this.workflows = [];
    for (var property1 in data) {
      data[property1].name = property1;
      this.workflows.push(data[property1])
    }
    this.request = this.workflows[0];
  }

  createRequest(workflow) {
    this.request = JSON.stringify(workflow, undefined, 4);

  }

}
