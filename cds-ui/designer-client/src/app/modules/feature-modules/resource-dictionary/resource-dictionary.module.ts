/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2020 TechMahindra
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
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { SidebarModule } from 'ng-sidebar';
import { FormsModule } from '@angular/forms';
import { NgxFileDropModule } from 'ngx-file-drop';
import { AceEditorModule } from 'ng2-ace-editor';
import { DataTablesModule } from 'angular-datatables';

import { ResourceDictionaryRoutingModule } from './resource-dictionary-routing.module';
import { ResourceDictionaryDashboardComponent } from './resource-dictionary-dashboard/resource-dictionary-dashboard.component';
import { DictionaryHeaderComponent } from './resource-dictionary-dashboard/dictionary-header/dictionary-header.component';
import { SearchDictionaryComponent } from './resource-dictionary-dashboard/search-dictionary/search-dictionary.component';
import { FilterbyTagsComponent } from './resource-dictionary-dashboard/filterby-tags/filterby-tags.component';
import { SortDictionaryComponent } from './resource-dictionary-dashboard/sort-dictionary/sort-dictionary.component';
import { DictionaryPaginationComponent } from './resource-dictionary-dashboard/dictionary-pagination/dictionary-pagination.component';
import { SharedModulesModule } from '../../shared-modules/shared-modules.module';
import { DictionaryListComponent } from './resource-dictionary-dashboard/dictionary-list/dictionary-list.component';

@NgModule({
  declarations: [
    ResourceDictionaryDashboardComponent,
    DictionaryHeaderComponent,
    SearchDictionaryComponent,
    FilterbyTagsComponent,
    SortDictionaryComponent,
    DictionaryPaginationComponent,
    DictionaryListComponent,
  ],
  imports: [
    CommonModule,
    ResourceDictionaryRoutingModule,
    NgbPaginationModule,
    SharedModulesModule,
    SidebarModule.forRoot(),
    FormsModule,
    NgxFileDropModule,
    AceEditorModule,
    DataTablesModule,
  ]
})
export class ResourceDictionaryModule { }
