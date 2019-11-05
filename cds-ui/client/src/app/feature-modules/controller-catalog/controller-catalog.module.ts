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
import { MatToolbarModule, MatButtonModule, MatSidenavModule,  MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule, MatAutocompleteModule, MatDialogModule} from '@angular/material';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule,ReactiveFormsModule } from '@angular/forms';
import { NgJsonEditorModule } from 'ang-jsoneditor';

import { SharedModule } from '../../../app/common/shared/shared.module';
import { ControllerCatalogRoutingModule } from './controller-catalog-routing.module';
import { ControllerCatalogComponent } from './controller-catalog.component';
import { SelectTemplateComponent } from './select-template/select-template.component';
import { TemplateOptionsComponent } from './select-template/template-options/template-options.component';
import { SearchCatalogComponent } from './search-catalog/search-catalog.component';
import { CreateCatalogComponent } from './create-catalog/create-catalog.component';
import { CreateCatalogModule } from './create-catalog/create-catalog.module';
import { SearchCatalogModule } from './search-catalog/search-catalog.module';

@NgModule({
  declarations: [ ControllerCatalogComponent, SelectTemplateComponent, TemplateOptionsComponent, SearchCatalogComponent, CreateCatalogComponent ],
  imports: [
    CommonModule,
    ControllerCatalogRoutingModule,
    CreateCatalogModule,
    SearchCatalogModule,
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
    MatAutocompleteModule,
    MatDialogModule,
    FormsModule,
    ReactiveFormsModule,
    NgJsonEditorModule
  ]
})
export class ControllerCatalogModule { }
