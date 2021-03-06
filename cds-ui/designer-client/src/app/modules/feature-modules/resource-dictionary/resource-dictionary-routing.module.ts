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
import { Routes, RouterModule } from '@angular/router';
import { ResourceDictionaryDashboardComponent } from './resource-dictionary-dashboard/resource-dictionary-dashboard.component';
import { ResourceDictionaryCreationComponent } from './resource-dictionary-creation/resource-dictionary-creation.component';

const routes: Routes = [
  {
    path: '',
    component: ResourceDictionaryDashboardComponent
  },
  {
    path: 'createDictionary',
    component: ResourceDictionaryCreationComponent
  }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ResourceDictionaryRoutingModule { }
