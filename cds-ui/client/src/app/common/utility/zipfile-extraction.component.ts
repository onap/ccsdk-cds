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
import { Component, OnInit } from '@angular/core';
import * as JSZip from 'jszip';
import { SortPipe } from '../shared/pipes/sort.pipe';
import { LoaderService } from '../core/services/loader.service';

@Component({
  selector: 'app-zipfile-extraction',
  templateUrl: './zipfile-extraction.component.html',
  styleUrls: ['./zipfile-extraction.component.scss']
})
export class ZipfileExtractionComponent implements OnInit {
  private paths = [];
  private tree;
  private zipFile: JSZip = new JSZip();
  private fileObject: any;
  private activationBlueprint: any;
  private tocsaMetadaData: any;
  private blueprintName: string;
  private entryDefinition: string;
  validfile: boolean = false;
  uploadedFileName: string;
  filesData: any = [];
  
  constructor(private loader: LoaderService) { }

  ngOnInit() {
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
