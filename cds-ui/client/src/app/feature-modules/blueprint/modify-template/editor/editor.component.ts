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
  private zipFile: JSZip = new JSZip();

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

  constructor(private store: Store<IAppState>) {
    this.dataSource.data = TREE_DATA;
    this.bpState = this.store.select('blueprint');
    // this.dataSource.data = TREE_DATA;
  }

  hasChild = (_: number, node: ExampleFlatNode) => node.expandable;

  ngOnInit() {
    this.editorContent();
    this.dataSource.data = this.filesTree;
  }

  fileClicked(file) {
    console.log('selected file:' + file);
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
        let blueprint = [];
        for (let key in this.blueprintdata) {
          if (this.blueprintdata.hasOwnProperty(key)) {
            blueprint.push(this.blueprintdata[key]);
          }
        }
        this.text = JSON.stringify(this.blueprintdata, null, '\t');
        // this.editor.getEditor().getSession().setMode("ace/mode/json");
        this.editor.getEditor().getSession().setTabSize(2);
        this.editor.getEditor().getSession().setUseWrapMode(true);
        // this.editor.getEditor().setValue(JSON.stringify(this.blueprintdata, null, '\t'));
        // console.log(this.text);
      })
  }

  updateBlueprint() {
    console.log(this.blueprint);
    this.filesData.forEach(fileNode=>{
      if(fileNode.name.includes(this.blueprintName.trim()) && fileNode.name.includes(this.selectedFile.trim())) {
        fileNode.data = this.text;
      } else if(fileNode.name.includes(this.selectedFile.trim())) {
        fileNode.data = this.text;
      }
    });

    if(this.selectedFile == this.blueprintName.trim()) {
      this.blueprint = JSON.parse(this.text);
    } else {
      this.blueprint = this.blueprintdata;
    }

    let blueprintState = {
      blueprint: this.blueprint,
      name: this.blueprintName,
      files: this.filesTree,
      filesData: this.filesData
    }
    this.store.dispatch(new SetBlueprintState(blueprintState));
    // console.log(this.text);
  }

  selectFileToView(file) {
    this.selectedFile = file.name;
    this.filesData.forEach((fileNode) => {
      if (fileNode.name.includes(file.name)) {
        this.text = fileNode.data;
      }
    })
  }

  SaveToBackend() {
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        
      });
  }

  deploy() {
    // to do
  }

  create() {    
    this.filesData.forEach((path) => {
      this.zipFile.file(path.name, path.data);
    });
  }

  download() {
    this.create();
    var zipFilename = "baseconfiguration.zip";
    this.zipFile.generateAsync({ type: "blob" })
      .then(blob => {
        saveAs(blob, zipFilename);
      });
  }
}
