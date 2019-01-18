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
import { Routes, RouterModule } from '@angular/router';
import { ResourceDefinitionComponent } from './resource-definition.component';


const routes: Routes = [
    {
        path: '',
        component: ResourceDefinitionComponent,
        children: [
            {
                path: '',
               loadChildren: './resource-creation/resource-creation.module#ResourceCreationModule'
            },
            {
                path: 'resource-creation',
                loadChildren: './resource-creation/resource-creation.module#ResourceCreationModule'
            },
            {
                path: 'resource-edit',
                loadChildren: './resource-edit/resource-edit.module#ResourceEditModule'
            },
            {
                path: 'save-resource',
                loadChildren: './save-resource/save-resource.module#SaveResourceModule'
            }
        ]
    }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ResourceDefinitionRoutingModule { }
