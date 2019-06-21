/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
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
import { IBlueprint } from 'src/app/common/core/store/models/blueprint.model';
import "ace-builds/webpack-resolver";
import 'brace';
import 'brace/ext/language_tools';
import 'ace-builds/src-min-noconflict/snippets/html';
import * as JSZip from 'jszip';
import { saveAs } from 'file-saver';

import { IAppState } from '../../../../common/core/store/state/app.state';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';
import { IBlueprintState } from 'src/app/common/core/store/models/blueprintState.model';
import { LoadBlueprintSuccess, SetBlueprintState } from '../../../../common/core/store/actions/blueprint.action'
import { ApiService } from 'src/app/common/core/services/api.service';
import { IMetaData } from 'src/app/common/core/store/models/metadata.model';
import { EditorService } from './editor.service';
import { SortPipe } from '../../../../common/shared/pipes/sort.pipe';
import { NotificationHandlerService } from 'src/app/common/core/services/notification-handler.service';
import { LoaderService } from 'src/app/common/core/services/loader.service';


interface Node {
  name: string;
  children?: Node[];
  data?: any
}

const TREE_DATA: Node[] = [
  {
    name: 'Definitions',
    children: [
      { name: 'activation-blueprint.json' },
      { name: 'artifacts_types.json' },
      { name: 'data_types.json' },
    ]
  }
];

/** Flat node with expandable and level information */
interface ExampleFlatNode {
  expandable: boolean;
  name: string;
  level: number;
}


@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent implements OnInit {

  @ViewChild('editor') editor;
  blueprintdata: IBlueprint;
  blueprint: IBlueprint;
  bpState: Observable<IBlueprintState>;
  text: string;
  filesTree: any = [];
  filesData: any = [];
  selectedFile: string;
  zipFolder: any;
  blueprintName: string;
  fileExtension: string;
  mode: string;
  private zipFile: JSZip = new JSZip();
  activeNode: any;
  selectedFolder: string;
  activationBlueprint: string;
  isNameTextboxEnablled: boolean = false;
  fileAction: string;
  filetoDelete: string;
  currentFilePath: string = '';
  selectedFileObj = { name: '', type: '' };
  viewTemplateMode: boolean = false;
  paramData: any = {
    'capability-data': [],
    'resourceAccumulatorResolvedData': []
  };
  validfile: boolean = false;
  @ViewChild('fileInput') fileInput;
  result: string = '';
  private paths = [];
  private tree;
  private fileObject: any;
  private tocsaMetadaData: any;
  metadata: IMetaData;
  uploadedFileName: string;
  entryDefinition: string;

  private transformer = (node: Node, level: number) => {
    return {
      expandable: !!node.children && node.children.length > 0,
      name: node.name,
      level: level,
    };
  }

  treeControl = new FlatTreeControl<ExampleFlatNode>(
    node => node.level, node => node.expandable);

  treeFlattener = new MatTreeFlattener(
    this.transformer, node => node.level, node => node.expandable, node => node.children);

  dataSource = new MatTreeFlatDataSource(this.treeControl, this.treeFlattener);
  artifactName: any;
  artifactVersion: any;

  constructor(private store: Store<IAppState>, private apiservice: EditorService,
    private alertService: NotificationHandlerService, private loader: LoaderService
    ) 
    {
    this.dataSource.data = TREE_DATA;
    this.bpState = this.store.select('blueprint');
    // this.dataSource.data = TREE_DATA;
  }

  hasChild = (_: number, node: ExampleFlatNode) => node.expandable;

  ngOnInit() {
    this.editorContent();
    this.dataSource.data = this.filesTree;
  }

  editorContent() {
    this.editor.setTheme("eclipse");
    this.editor.getEditor().setOptions({
      // enableBasicAutocompletion: true,
      fontSize: "100%",
      printMargin: false,
    });
    this.editor.getEditor().commands.addCommand({
      name: "showOtherCompletions",
      bindKey: "Ctrl-.",
      exec: function (editor) {

      }
    })
    this.bpState.subscribe(
      blueprintdata => {
        var blueprintState: IBlueprintState = { blueprint: blueprintdata.blueprint, isLoadSuccess: blueprintdata.isLoadSuccess, isSaveSuccess: blueprintdata.isSaveSuccess, isUpdateSuccess: blueprintdata.isUpdateSuccess };
        this.blueprintdata = blueprintState.blueprint;
        this.filesTree = blueprintdata.files;
        this.filesData = blueprintdata.filesData;
        this.dataSource.data = this.filesTree;
        this.blueprintName = blueprintdata.name;
        this.uploadedFileName = blueprintdata.uploadedFileName;
        this.entryDefinition = blueprintdata.entryDefinition;
        let blueprint = [];
        for (let key in this.blueprintdata) {
          if (this.blueprintdata.hasOwnProperty(key)) {
            blueprint.push(this.blueprintdata[key]);
          }
        }
        this.metadata = blueprintState.blueprint.metadata;
        let metadatavalues = [];
        for (let key in this.metadata) {
          if (this.metadata.hasOwnProperty(key)) {
            metadatavalues.push(this.metadata[key]);
          }
        }
        this.artifactName = metadatavalues[3];
        this.artifactVersion = metadatavalues[4];
        this.editor.getEditor().getSession().setTabSize(2);
        this.editor.getEditor().getSession().setUseWrapMode(true);
        this.editor.getEditor().getSession().setValue("");
        this.setEditorMode();
      })
  }

  updateBlueprint() {
    // console.log(this.blueprint);
    // this.filesData.forEach(fileNode => {
    //   if (this.selectedFile && fileNode.name.includes(this.blueprintName.trim()) && fileNode.name.includes(this.selectedFile.trim())) {
    //     fileNode.data = this.text;
    //   } else if (this.selectedFile && fileNode.name.includes(this.currentFilePath)) {
    //     // this.selectedFile && fileNode.name.includes(this.selectedFile.trim())) {
    //     fileNode.data = this.text;
    //   }
    // });

    // if (this.selectedFile && this.selectedFile == this.blueprintName.trim()) {
    //   this.blueprint = JSON.parse(this.text);
    // } else {
    //   this.blueprint = this.blueprintdata;
    // }

    let blueprintState = {
      blueprint: this.blueprint,
      name: this.blueprintName,
      files: this.filesTree,
      filesData: this.filesData,
      uploadedFileName: this.uploadedFileName,
      entryDefinition: this.entryDefinition
    }
    this.store.dispatch(new SetBlueprintState(blueprintState));
  }

  selectFileToView(file) {
    if (file.name.includes('.vtl')) { this.viewTemplateMode = true; } else { this.viewTemplateMode = false; }
    this.currentFilePath = '';
    this.expandParents(file);
    this.selectedFileObj.name = file.name;
    this.selectedFileObj.type = 'file';
    this.selectedFile = file.name;
    this.filetoDelete = file.name;
    this.currentFilePath = this.currentFilePath + this.selectedFile;
    this.filesData.forEach((fileNode) => {
      if (fileNode.name.includes(file.name) && fileNode.name == this.currentFilePath) {
        this.text = fileNode.data;
      }
    })
    this.fileExtension = this.selectedFile.substr(this.selectedFile.lastIndexOf('.') + 1);
    this.setEditorMode();
  }

  getEnriched() {
    this.create();
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        const formData = new FormData();
        formData.append("file", blob);
        this.apiservice.enrich("/enrich-blueprint/", formData)
          .subscribe(
            (response) => {
              this.zipFile.files = {};
              this.zipFile.loadAsync(response)
                .then((zip) => {
                  if (zip) {
                    this.buildFileViewData(zip);
                    console.log("processed");
                  }
                });
              this.alertService.success('Blueprint enriched successfully');
            },
            (error)=>{
              this.alertService.error('Enrich:' + error.message);
            });
      });
  }



  saveToBackend() {
    this.create();
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        const formData = new FormData();
        formData.append("file", blob);
        this.apiservice.post("/create-blueprint/", formData)
          .subscribe(
            data => {
              this.alertService.success('Success:' + JSON.stringify(data));
            }, error=>{
              this.alertService.error('Save -' +error.message);
            });

      });
  }

  deploy() {
    // to do
    this.create();
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        const formData = new FormData();
        formData.append("file", blob);
        this.apiservice.deployPost("/deploy-blueprint/", formData)
          .subscribe(data => {
            this.alertService.success('Saved Successfully:' + JSON.stringify(data));
          }, error=>{
            this.alertService.error('Deploy - ' + error.message);
          });

      });
  }

  publish() {
    this.create();
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        const formData = new FormData();
        formData.append("file", blob);
        this.apiservice.post("/publish/", formData)
          .subscribe(data => {
            this.alertService.success('Published:' + JSON.stringify(data))
          }, error=>{
            this.alertService.error('Publish - ' + error.message);
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

  download() {
    console.log(this.artifactName);
    status = this.apiservice.downloadCBA("/download-blueprint/" + this.artifactName + "/" + this.artifactVersion);
    window.alert(status);
    // .subscribe(response => {
    //   console.log(response);
    //   var blob = new Blob([response], { type: 'application/zip' });
    //   const fileName = 'CBA';
    //   saveAs(blob, fileName);
    // },
    //   error => {
    //     console.log(error);
    //   }
    // );

    // this.create();
    // var zipFilename = "baseconfiguration.zip";
    // this.zipFile.generateAsync({ type: "blob" })
    //   .then(blob => {
    //     saveAs(blob, zipFilename);
    //   });
  }
  setEditorMode() {
    switch (this.fileExtension) {
      case "xml":
        this.mode = 'xml';
        break;
      case "py":
        this.mode = 'python';
        break;
      case "kts":
        this.mode = 'kotlin';
        break;
      case "txt":
        this.mode = 'text';
        break;
      case "meta":
        this.mode = 'text';
        break;
      case "vtl":
        this.mode = 'velocity';
        break;
      default:
        this.mode = 'json';
    }
  }

  selectFolder(node) {
    this.currentFilePath = '';
    this.expandParents(node);
    this.selectedFolder = node.name;
    this.filetoDelete = node.name;
    this.selectedFileObj.name = node.name;
    this.selectedFileObj.type = 'folder';
    this.currentFilePath = this.currentFilePath + this.selectedFolder + '/';
  }

  createFolderOrFile(name) {
    if (name && name.srcElement.value !== null && name.srcElement.value !== '') {
      let newFilesData: [any] = this.filesData;
      let newFileNode = {
        name: '',
        data: ''
      }
      let newFileNode1 = {
        name: '',
        data: ''
      }
      if (this.fileAction == 'createFolder') {
        newFileNode.name = this.currentFilePath + name.srcElement.value + '/'
        newFileNode.data = '';
        newFileNode1.name = this.currentFilePath + name.srcElement.value + '/README.md'
        newFileNode1.data = name.srcElement.value + ' Folder';
        this.filesData.push(newFileNode);
        this.filesData.push(newFileNode1);
      } else {
        newFileNode.name = this.currentFilePath + name.srcElement.value;
        newFileNode.data = '';
        this.filesData.push(newFileNode);
      }
      this.filesData = new SortPipe().transform(this.filesData, 'asc', 'name');
      this.arrangeTreeData(this.filesData);
    }
  }

  findIndexForNewNode() {
    let indexForNewNode;
    for (let i = 0; i < this.filesData.length; i++) {
      if (this.filesData[i].name.includes(this.selectedFolder)) {
        indexForNewNode = i;
      }
    }
    return indexForNewNode;
  }

  async buildFileViewData(zip) {
    this.validfile = false;
    this.paths = [];
    for (var file in zip.files) {
      this.fileObject = {
        // name: zip.files[file].name,
        // name: this.uploadedFileName + '/' + zip.files[file].name,
        name: this.uploadedFileName + zip.files[file].name,
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
      this.filesData = this.paths;
      this.paths = new SortPipe().transform(this.paths, 'asc', 'name');
      this.tree = this.arrangeTreeData(this.paths);
    } else {
      alert('Please update proper file with TOSCA metadata');
    }
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

  arrangeTreeData(paths) {
    const tree = [];

    paths.forEach((path) => {

      const pathParts = path.name.split('/');
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
          }
          if (newPart.name !== '') {
            currentLevel.push(newPart);
            currentLevel = newPart.children;
          }
        }
      });
    });
    this.dataSource.data = tree;
    this.filesTree = tree;
    this.isNameTextboxEnablled = false;
    this.updateBlueprint();
  }

  enableNameInputEl(action) {
    this.fileAction = action;
    if (action == 'createFolder' || action == 'createFile') {
      this.isNameTextboxEnablled = true;
    }
  }

  deleteFolderOrFile(action) {
    for (let i = 0; i < this.filesData.length; i++) {
      if (this.filesData[i].name.includes(this.filetoDelete.trim()) && this.filesData[i].name.includes(this.currentFilePath)) {
        this.filesData.splice(i, 1);
        i = i - 1;
      }
    }
    this.filesData = new SortPipe().transform(this.filesData, 'asc', 'name');
    this.arrangeTreeData(this.filesData);
  }

  expandParents(node) {
    const parent = this.getParent(node);
    this.treeControl.expand(parent);

    if (parent && parent.level > 0) {
      this.expandParents(parent);
    }

    console.log(this.currentFilePath);
  }

  getParent(node) {
    const { treeControl } = this;
    const currentLevel = treeControl.getLevel(node);

    if (currentLevel < 1) {
      // this.currentFilePath = this.currentFilePath + this.selectedFolder;
      return null;
    }

    const startIndex = treeControl.dataNodes.indexOf(node) - 1;

    for (let i = startIndex; i >= 0; i--) {
      const currentNode = treeControl.dataNodes[i];

      if (treeControl.getLevel(currentNode) < currentLevel) {
        this.currentFilePath = currentNode.name + '/' + this.currentFilePath;
        return currentNode;
      }
    }
  }
  loadConfigParams() {
    console.log(this.currentFilePath);
    console.log(this.selectedFile);
    console.log(this.selectedFileObj);
    console.log(this.selectedFolder);
    console.log(this.text);

    let parsedData = JSON.parse(this.text);
    this.paramData.resourceAccumulatorResolvedData = parsedData['resource-accumulator-resolved-data'];
    let i = 0;

    this.paramData.resourceAccumulatorResolvedData.forEach(element => {
      element.id = i;
      let tempElement = element['param-value'];
      let indexLength = tempElement.length;
      tempElement = tempElement.slice(2, indexLength);
      let index = tempElement.indexOf('}');
      tempElement = this.removeItemByIndex(tempElement, index);
      element['param-value'] = tempElement;
      i++;
    });

  }

  removeItemByIndex(paramValue, index) {
    if (index == 0) {
      return paramValue.slice(1)
    } else if (index > 0) {
      let indexLength = paramValue.length;
      return paramValue.slice(0, index) + paramValue.slice(index + 1, indexLength);
    } else {
      return paramValue;
    }
  }

  saveEditedChanges() {
    this.loader.showLoader();
    this.filesData.forEach(fileNode => {
      if (this.selectedFile && fileNode.name.includes(this.blueprintName.trim()) && fileNode.name.includes(this.selectedFile.trim())) {
        fileNode.data = this.text;
      } else if (this.selectedFile && fileNode.name.includes(this.currentFilePath)) {
        // this.selectedFile && fileNode.name.includes(this.selectedFile.trim())) {
        fileNode.data = this.text;
      }
    });

    if (this.selectedFile && this.selectedFile == this.blueprintName.trim()) {
      this.blueprint = JSON.parse(this.text);
    } else {
      this.blueprint = this.blueprintdata;
    }

    this.updateBlueprint();
    this.loader.hideLoader();
  }
}
