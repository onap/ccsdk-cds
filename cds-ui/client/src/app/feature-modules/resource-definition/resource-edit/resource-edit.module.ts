/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright 2019 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { ResourceEditComponent } from './resource-edit.component';
import { ResourceEditRoutingModule } from './resource-edit-routing.module';
import { MatExpansionModule,MatToolbarModule,MatIconModule, MatButtonModule, MatSidenavModule,  MatCheckboxModule, MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule} from '@angular/material';
import { FormsModule,ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../../../../app/common/shared/shared.module';
import { SourcesTemplateComponent } from './sources-template/sources-template.component';
import { ResourceMetadataComponent } from './resource-metadata/resource-metadata.component';
import { DragDropModule } from '@angular/cdk/drag-drop';

@NgModule({
  declarations: [ ResourceEditComponent,SourcesTemplateComponent,ResourceMetadataComponent ],
  imports: [
   CommonModule,
   ResourceEditRoutingModule,
   SharedModule,
   FormsModule,ReactiveFormsModule,
   DragDropModule,
   MatExpansionModule,MatToolbarModule,MatIconModule, MatButtonModule, MatSidenavModule,  MatCheckboxModule, MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule

  ],
  exports: [ ResourceEditComponent,SharedModule ]
})
export class ResourceEditModule { }
