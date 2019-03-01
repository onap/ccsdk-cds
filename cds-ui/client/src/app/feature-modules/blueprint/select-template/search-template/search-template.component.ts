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

import { Component, OnInit, EventEmitter, Output, ViewChild } from '@angular/core';
import { Store } from '@ngrx/store';
import * as JSZip from 'jszip';
import { Observable } from 'rxjs';

import { IBlueprint } from '../../../../common/core/store/models/blueprint.model';
import { IBlueprintState } from '../../../../common/core/store/models/blueprintState.model';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { LoadBlueprintSuccess } from '../../../../common/core/store/actions/blueprint.action';

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

  @ViewChild('fileInput') fileInput;
  result: string = '';

  public paths = [];
  public tree;
  private zipFile: JSZip = new JSZip();
  private fileObject: any;

  constructor(private store: Store<IAppState>) { }

  ngOnInit() {
  }

  fileChanged(e: any) {
    this.file = e.target.files[0];

    // this.zipFile.loadAsync(this.file)
    //   .then((zip) => {
    //     if(zip) {            
    //       this.buildFileViewData(zip);
    //     }
    //   });
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

  async buildFileViewData(zip) {
    for (var file in zip.files) {
      this.fileObject = {
        name: zip.files[file].name,
        data: ''
      };
      const value = <any>await  zip.files[file].async('string');
      this.fileObject.data = value;
      this.paths.push(this.fileObject); 
    }
    this.arrangeTreeData(this.paths);
  }

  arrangeTreeData(paths) {
    const tree = [];

    paths.forEach((path) => {

      const pathParts = path.name.split('/');
      pathParts.shift();
      let currentLevel = tree;

      pathParts.forEach((part) => {
        const existingPath = currentLevel.filter(level => level.name === part);

        if (existingPath.length > 0) {
          currentLevel = existingPath[0].children;
        } else {
          const newPart = {
            name: part,
            children: [],
            data: path.data
          };

          currentLevel.push(newPart);
          currentLevel = newPart.children;
        }
      });
    });
    console.log('tree: ', tree);
    return tree;
  }
}
