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

import { Component, OnInit, EventEmitter, Output, ViewChild, Input } from '@angular/core';
import { Store } from '@ngrx/store';
import * as JSZip from 'jszip';
import { Observable } from 'rxjs';

import { IBlueprint } from '../../../../common/core/store/models/blueprint.model';
import { IBlueprintState } from '../../../../common/core/store/models/blueprintState.model';
import { IAppState } from '../../../../common/core/store/state/app.state';
import { LoadBlueprintSuccess, SET_BLUEPRINT_STATE, SetBlueprintState } from '../../../../common/core/store/actions/blueprint.action';
import { SortPipe } from '../../../../common/shared/pipes/sort.pipe';
import { LoaderService } from '../../../../common/core/services/loader.service';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-search-template',
  templateUrl: './search-template.component.html',
  styleUrls: ['./search-template.component.scss']
})
export class SearchTemplateComponent implements OnInit {
  file: File;
  localBlueprintData: IBlueprint;
  fileText: object[];
  blueprintState: IBlueprintState;
  bpState: Observable<IBlueprintState>;
  validfile: boolean = false;
  uploadedFileName: string;
  @ViewChild('fileInput') fileInput;
  result: string = '';
  @Input() optionSelected: string;
  myControl: FormGroup;
  @Output() resourcesData = new EventEmitter();
  options: any[] = [];
  searchText: string = '';

  private paths = [];
  private tree;
  private zipFile: JSZip = new JSZip();
  private fileObject: any;
  private activationBlueprint: any;
  private tocsaMetadaData: any;
  private blueprintName: string;
  private entryDefinition: string;

  constructor(private store: Store<IAppState>, private loader: LoaderService, private formBuilder: FormBuilder) { }

  ngOnInit() {
    this.myControl = this.formBuilder.group({
      search_input: ['', Validators.required]
    });
  }

  selected(value) {
    this.resourcesData.emit(value);
  }

  fileChanged(e: any) {
    this.paths = [];
    this.file = e.target.files[0];
    this.uploadedFileName = (this.file.name.split('.'))[0];
    this.zipFile.files = {};
    this.zipFile.loadAsync(this.file)
      .then((zip) => {
        if (zip) {
          this.loader.showLoader();
          this.buildFileViewData(zip);
        }
      });
  }

  updateBlueprintState() {
    let data: IBlueprint = this.activationBlueprint ? JSON.parse(this.activationBlueprint.toString()) : this.activationBlueprint;
    let blueprintState = {
      blueprint: data,
      name: this.blueprintName,
      files: this.tree,
      filesData: this.paths,
      uploadedFileName: this.uploadedFileName,
      entryDefinition: this.entryDefinition
    }
    this.store.dispatch(new SetBlueprintState(blueprintState))

  }

  async buildFileViewData(zip) {
    this.validfile = false;
    this.paths = [];
    console.log(zip.files);
    for (var file in zip.files) {
      console.log("name: " + zip.files[file].name);
      this.fileObject = {
        // nameForUIDisplay: this.uploadedFileName + '/' + zip.files[file].name,
        // name: zip.files[file].name,
        name: this.uploadedFileName + '/' + zip.files[file].name,
        data: ''
      };
      const value = <any>await zip.files[file].async('string');
      this.fileObject.data = value;
      this.paths.push(this.fileObject);
    }

    if (this.paths) {
      this.paths.forEach(path => {
        if (path.name.includes("TOSCA.meta")) {
          this.validfile = true
        }
      });
    } else {
      alert('Please update proper file');
    }

    if (this.validfile) {
      this.fetchTOSACAMetadata();
      this.paths = new SortPipe().transform(this.paths, 'asc', 'name');
      this.tree = this.arrangeTreeData(this.paths);
    } else {
      alert('Please update proper file with TOSCA metadata');
    }
  }

  arrangeTreeData(paths) {
    const tree = [];

    paths.forEach((path) => {

      const pathParts = path.name.split('/');
      // pathParts.shift();
      let currentLevel = tree;

      pathParts.forEach((part) => {
        const existingPath = currentLevel.filter(level => level.name === part);

        if (existingPath.length > 0) {
          currentLevel = existingPath[0].children;
        } else {
          const newPart = {
            name: part,
            children: [],
            data: path.data,
            path: path.name
          };
          if (part.trim() == this.blueprintName.trim()) {
            this.activationBlueprint = path.data;
            newPart.data = JSON.parse(this.activationBlueprint.toString());
            console.log('newpart', newPart);
            this.entryDefinition = path.name.trim();
          }
          if (newPart.name !== '') {
            currentLevel.push(newPart);
            currentLevel = newPart.children;
          }
        }
      });
    });
    this.loader.hideLoader();
    return tree;
  }

  fetchTOSACAMetadata() {
    let toscaData = {};
    this.paths.forEach(file => {
      if (file.name.includes('TOSCA.meta')) {
        let keys = file.data.split("\n");
        keys.forEach((key) => {
          let propertyData = key.split(':');
          toscaData[propertyData[0]] = propertyData[1];
        });
      }
    });
    this.blueprintName = (((toscaData['Entry-Definitions']).split('/'))[1]).toString();;
    console.log(toscaData);
  }
}
