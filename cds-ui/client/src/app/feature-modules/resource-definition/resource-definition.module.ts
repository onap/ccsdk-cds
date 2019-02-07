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

import { ResourceDefinitionRoutingModule } from './resource-definition-routing.module';
import { ResourceDefinitionComponent } from './resource-definition.component';
import { SharedModule } from '../../../app/common/shared/shared.module';
import { SaveResourceModule } from './save-resource/save-resource.module';
import { ResourceCreationModule } from './resource-creation/resource-creation.module';
import { ResourceEditModule } from './resource-edit/resource-edit.module';

import { MatToolbarModule, MatButtonModule, MatSidenavModule,  MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule} from '@angular/material';
import { MatIconModule } from '@angular/material/icon';

@NgModule({
  
    declarations: [ResourceDefinitionComponent],
  
    imports: [
    CommonModule,
    ResourceDefinitionRoutingModule,
    SharedModule,
    SaveResourceModule,
    ResourceCreationModule,
    ResourceEditModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
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
    MatStepperModule,
  ]
})
export class ResourceDefinitionModule { }
