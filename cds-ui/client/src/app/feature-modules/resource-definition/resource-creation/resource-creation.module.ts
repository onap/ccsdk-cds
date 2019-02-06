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

import { ResourceCreationRoutingModule } from './resource-creation-routing.module';
import { ResourceCreationComponent } from './resource-creation.component';
import { ResourceTemplateOptionsComponent } from './resource-template-options/resource-template-options.component';
import { MatToolbarModule,MatIconModule, MatButtonModule, MatSidenavModule,  MatCheckboxModule, MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule} from '@angular/material';
import { UploadResourceComponent } from './upload-resource/upload-resource.component';

@NgModule({
  declarations: [ResourceCreationComponent],
  imports: [
    CommonModule,
    ResourceCreationRoutingModule,
    MatToolbarModule,
    MatIconModule, 
    MatButtonModule, 
    MatSidenavModule,  
    MatCheckboxModule, 
    MatListModule, 
    MatGridListModule, 
    MatCardModule, 
    MatMenuModule, 
    MatTableModule, 
    MatPaginatorModule, 
    MatSortModule, 
    MatInputModule, 
    MatSelectModule, 
    MatRadioModule, 
    MatFormFieldModule, 
    MatStepperModule
  ]
})
export class ResourceCreationModule { }
