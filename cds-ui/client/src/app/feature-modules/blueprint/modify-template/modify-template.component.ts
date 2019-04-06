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

import { EditorComponent } from './editor/editor.component';

@Component({
  selector: 'app-modify-template',
  templateUrl: './modify-template.component.html',
  styleUrls: ['./modify-template.component.scss']
})
export class ModifyTemplateComponent implements OnInit {

  isEnriched: boolean = false;
  designerMode: boolean = false;
  editorMode: boolean = true;
  viewText: string = "Designer View";

  @ViewChild(EditorComponent) editorComp: EditorComponent;

  constructor() { }

  ngOnInit() {
  }

  viewNodeDetails(nodeTemplate) {
    console.log(nodeTemplate);
  }

  changeView() {
    if(this.viewText == 'Editor View') {
      this.editorMode =  true;
      this.designerMode = false;
      this.viewText = 'Designer View'
    } else {
      this.editorMode =  false;
      this.designerMode = true;
      this.viewText = 'Editor View'
    }
  }

  getEnriched() {
    this.editorComp.getEnriched();
  }

  saveToControllerBlueprint() {
    this.editorComp.saveToBackend();
  }

  publishToControllerBlueprint() {
    this.editorComp.publish();
  }

  saveToBlueprintProcessor() {
    this.editorComp.deploy();
  }

  processBlueprint(){
    
  }
  downloadCBA() {
    this.editorComp.download();
  }

}
