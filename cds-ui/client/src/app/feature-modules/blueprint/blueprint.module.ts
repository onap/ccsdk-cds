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

import { BlueprintComponent } from './blueprint.component';
import { BlueprintRoutingModule } from './blueprint-routing.module';

import { SelectTemplateModule } from './select-template/select-template.module';
import { ModifyTemplateModule } from './modify-template/modify-template.module';
import { DeployTemplateModule } from './deploy-template/deploy-template.module';
import { TestTemplateModule } from './test-template/test-template.module';
import { SharedModule } from '../../../app/common/shared/shared.module';

import { MatToolbarModule,MatIconModule, MatButtonModule, MatSidenavModule,  MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule} from '@angular/material';
import { ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    BlueprintComponent 
  ],
  imports: [
    CommonModule,
    BlueprintRoutingModule,
    SharedModule,
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
    SelectTemplateModule,
    ModifyTemplateModule,
    DeployTemplateModule,
    TestTemplateModule,
    ReactiveFormsModule      
  ]
})
export class BlueprintModule { }
