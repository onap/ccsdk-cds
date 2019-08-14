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

import { Component, OnInit, ViewChild, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Store } from '@ngrx/store';
import { SearchTemplateService } from '../search-template.service';
import { MatAutocompleteTrigger } from '@angular/material';
import { NotificationHandlerService } from 'src/app/common/core/services/notification-handler.service';
import { SearchPipe } from 'src/app/common/shared/pipes/search.pipe';
import * as JSZip from 'jszip';
import { SortPipe } from '../../../../../common/shared/pipes/sort.pipe';
import { LoaderService } from '../../../../../common/core/services/loader.service';
import { IBlueprint } from '../../../../../common/core/store/models/blueprint.model';
import { IBlueprintState } from '../../../../../common/core/store/models/blueprintState.model';
import { IAppState } from '../../../../../common/core/store/state/app.state';
import { SetBlueprintState } from '../../../../../common/core/store/actions/blueprint.action';

@Component({
  selector: 'app-search-from-database',
  templateUrl: './search-from-database.component.html',
  styleUrls: ['./search-from-database.component.scss']
})
export class SearchFromDatabaseComponent implements OnInit {

  myControl: FormGroup;
  @Output() resourcesData = new EventEmitter();
  options: any[] = [];
  // @ViewChild('resourceSelect', { read: MatAutocompleteTrigger }) resourceSelect: MatAutocompleteTrigger;

  validfile: boolean = false;
  filesTree: any = [];
  filesData: any = [];
  private zipFile: JSZip = new JSZip();
  private paths = [];
  private tree;
  private fileObject: any;
  private activationBlueprint: any;
  private tocsaMetadaData: any;
  private blueprintName: string;
  private entryDefinition: string;
  uploadedFileName: string;

  searchText: string = '';
  constructor(private _formBuilder: FormBuilder,
    private searchService: SearchTemplateService, private alertService: NotificationHandlerService, 
    private loader: LoaderService, private store: Store<IAppState>) { }

  ngOnInit() {
    this.myControl = this._formBuilder.group({
      search_input: ['', Validators.required]
    });
  }
  selected(value) {
    this.resourcesData.emit(value);
  }

  fetchResourceByName() {
    this.options = [];
    this.searchService.searchByTags(this.searchText)
      .subscribe(data => {
        data.forEach(element => {
          this.options.push(element)
        });
      }, error => {
        this.alertService.error('Blueprint not matching the search tag' + error);
      })
  }

  editCBA(artifactName: string,artifactVersion:string, option: string) {
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        const formData = new FormData();
        formData.append("file", blob);
        // this.editorService.enrich("/enrich-blueprint/", formData)
        this.searchService.getBlueprintZip(artifactName + "/" + artifactVersion)
          .subscribe(
            (response) => {
              // console.log(response);
              this.zipFile.files = {};
              this.zipFile.loadAsync(response)
                .then((zip) => {
                  if (zip) {
                    this.buildFileViewData(zip);
                    // console.log("processed");
                    let data: IBlueprint = this.activationBlueprint ? JSON.parse(this.activationBlueprint.toString()) : this.activationBlueprint;
                    let blueprintState = {
                      blueprint: data,
                      name: this.blueprintName,
                      files: this.tree,
                      filesData: this.paths,
                      uploadedFileName: this.blueprintName,
                      entryDefinition: this.entryDefinition
                    }
                    this.store.dispatch(new SetBlueprintState(blueprintState));
                    // console.log(blueprintState);
                  }
                });
              // this.alertService.success('Blueprint enriched successfully');
            },
            (error) => {
              this.alertService.error('Blue print error' + error.message);
            });
      });
  }

  create() {
    this.filesData.forEach((path) => {
      let index = path.name.indexOf("/");
      let name = path.name.slice(index + 1, path.name.length);
      this.zipFile.file(name, path.data);
    });
  }

  async buildFileViewData(zip) {
    this.validfile = false;
    this.paths = [];
    // console.log(zip.files);
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
            // console.log('newpart', newPart);
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
    // console.log(toscaData);
  }
}
