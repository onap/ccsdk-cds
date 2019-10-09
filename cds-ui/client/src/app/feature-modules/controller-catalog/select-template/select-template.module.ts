/*
* ============LICENSE_START=======================================================
/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2019 TechMahindra
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
import { TemplateOptionsComponent } from './template-options/template-options.component';
import { SelectTemplateComponent } from './select-template.component';
import { SelectTemplateRoutingModule } from './select-template-routing.module';
import { AppMaterialModule } from 'src/app/common/modules/app-material.module';
import { MatAutocompleteModule,MatToolbarModule,MatIconModule, MatButtonModule, MatSidenavModule,  MatCheckboxModule, MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule} from '@angular/material';
import { SharedModule } from '../../../../app/common/shared/shared.module';
import { FormsModule,ReactiveFormsModule } from '@angular/forms';

@NgModule({
  declarations: [
    TemplateOptionsComponent,
    SelectTemplateComponent
  ],
  exports: [
    TemplateOptionsComponent,
    SelectTemplateComponent
  ],
  imports: [
    CommonModule,
    SelectTemplateRoutingModule,
    ReactiveFormsModule,
    AppMaterialModule,
    FormsModule,
    ReactiveFormsModule,
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
    MatStepperModule,
    MatAutocompleteModule
  ],
  providers: [
  ]
})
export class SelectTemplateModule { }
