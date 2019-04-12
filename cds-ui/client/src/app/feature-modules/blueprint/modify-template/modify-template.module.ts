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
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CdkTableModule } from '@angular/cdk/table';
import { FormsModule } from '@angular/forms';

import { ModifyTemplateComponent } from './modify-template.component';
import { ModifyTemplateRoutingModule } from './modify-template-routing.module';
import { AppMaterialModule } from '../../../common/modules/app-material.module';
import { DesignerComponent } from './designer/designer.component';
import { EditorComponent } from './editor/editor.component';
import { AceEditorModule } from 'ng2-ace-editor';
import { ResourceMappingComponent } from './resource-mapping/resource-mapping.component';
import { ResourceMappingService } from './resource-mapping/resource-mapping.service';
import { EditorService } from './editor/editor.service';

@NgModule({
  declarations: [
    ModifyTemplateComponent,
    DesignerComponent,
    EditorComponent,
    ResourceMappingComponent
  ],
  exports: [
    ModifyTemplateComponent,
    DesignerComponent,
    AceEditorModule
  ],
  imports: [
    CommonModule,
    CdkTableModule,
    AppMaterialModule,
    ModifyTemplateRoutingModule,
    AceEditorModule,
    FormsModule
  ],
  providers: [ ResourceMappingService , EditorService ]
})
export class ModifyTemplateModule { }
