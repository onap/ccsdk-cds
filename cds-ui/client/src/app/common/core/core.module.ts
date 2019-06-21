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
import { StoreModule, Store } from '@ngrx/store';
import { EffectsModule } from '@ngrx/effects';
import { StoreRouterConnectingModule } from '@ngrx/router-store';
import { HttpClientModule } from '@angular/common/http';

import { appReducers } from './store/reducers/app.reducer';
import { BlueprintEffects } from './store/effects/blueprint.effects';
import { ResourcesEffects } from './store/effects/resources.effects';
import { ApiService } from './services/api.service';
import { NotificationHandlerService } from './services/notification-handler.service';
import { LoaderService } from './services/loader.service';
// import { BlueprintService } from './services/blueprint.service';

@NgModule({
  declarations: [
  ],
  imports: [
    CommonModule,
    StoreModule.forRoot(appReducers),
    EffectsModule.forRoot([BlueprintEffects, ResourcesEffects]),
    StoreRouterConnectingModule.forRoot({ stateKey: 'router' }),
    HttpClientModule
  ],
  providers: [ApiService,
    NotificationHandlerService,
    LoaderService
  ]
})
export class CoreModule { }
