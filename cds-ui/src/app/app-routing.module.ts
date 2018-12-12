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
import { Routes, RouterModule } from '@angular/router';
import { AboutComponent } from './common/components/index';
import { PageNotFoundComponent } from './common/components/page-not-found/page-not-found.component';
import { SelectTemplateComponent } from './feature-module/select-template/select-template.component';


const routes: Routes = [{
  path: 'about',
  component: AboutComponent
}
,
 {
  path: 'selectTemplate',
  loadChildren: './feature-module/select-template/select-template.module#SelectTemplateModule'
 },
//  {
//   path: 'modifyTemplate',
//   redirectTo: '/modifyTemplate',
//   pathMatch: 'full'
//  },
//  {
//   path: 'testTemplate',
//   redirectTo: '/testTemplate',
//   pathMatch: 'full'
//  },
//  {
//   path: 'deployTemplate',
//   redirectTo: '/deployTemplate',
//   pathMatch: 'full'
//  },
 {
  path: '',
  redirectTo: '/selectTemplate',
  pathMatch: 'full'
 },
 {
  path: 'modifyTemplate',
  loadChildren: './feature-module/modify-template/modify-template.module#ModifyTemplateModule'
 },
{
  path: 'testTemplate',
  loadChildren: './feature-module/test-template/test-template.module#TestTemplateModule'
},
{
  path: 'deployTemplate',
  loadChildren: './feature-module/deploy-template/deploy-template.module#DeployTemplateModule'
}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
