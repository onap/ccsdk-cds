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
import { SearchCatalogRoutingModule } from './search-catalog-routing.module';
import { MatToolbarModule, MatButtonModule, MatSidenavModule,  MatDialogModule, MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule, MatAutocompleteModule } from '@angular/material';
import { MatIconModule } from '@angular/material/icon';
import { SharedModule } from 'src/app/common/shared/shared.module';
import { FormsModule,ReactiveFormsModule } from '@angular/forms';
import { SearchCatalogService } from './search-catalog.service';
import { CatalogDataDialogComponent } from './catalog-data-dialog/catalog-data-dialog.component';

@NgModule({
  declarations: [ CatalogDataDialogComponent ],
  imports: [
    CommonModule,
    SearchCatalogRoutingModule,
    FormsModule,
    ReactiveFormsModule,
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
    MatDialogModule
  ],
  providers: [ SearchCatalogService ],
  entryComponents: [ CatalogDataDialogComponent ]
})
export class SearchCatalogModule { }
