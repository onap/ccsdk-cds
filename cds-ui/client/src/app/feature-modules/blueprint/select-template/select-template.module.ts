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
import { TemplateOptionsComponent } from './template-options/template-options.component';
import { SearchTemplateComponent } from './search-template/search-template.component';
import { MetadataComponent } from './metadata/metadata.component';
import { SelectTemplateComponent } from './select-template.component';
import { SelectTemplateRoutingModule } from './select-template-routing.module';
import { MatToolbarModule, MatButtonModule, MatSidenavModule,  MatListModule, MatGridListModule, MatCardModule, MatMenuModule, MatTableModule, MatPaginatorModule, MatSortModule, MatInputModule, MatSelectModule, MatRadioModule, MatFormFieldModule, MatStepperModule} from '@angular/material';
import { MatIconModule } from '@angular/material/icon';

import { SharedModule } from '../../../../app/common/shared/shared.module';

@NgModule({
  declarations: [
    TemplateOptionsComponent,
    SearchTemplateComponent,
    MetadataComponent,
    SelectTemplateComponent
    
  ],
  exports: [
    TemplateOptionsComponent,
    SearchTemplateComponent,
    MetadataComponent,
    SelectTemplateComponent
  ],
  imports: [
    CommonModule,
    SelectTemplateRoutingModule,
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
    SharedModule
  ]
})
export class SelectTemplateModule { }
