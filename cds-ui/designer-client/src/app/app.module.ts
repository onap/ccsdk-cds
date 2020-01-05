
/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
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

import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {AngularFontAwesomeModule} from 'angular-font-awesome';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatTabsModule} from '@angular/material/tabs';
import {ApiService} from './common/core/services/api.service';
import {HttpClientModule} from '@angular/common/http';
import {PackagesModule} from './modules/feature-modules/packages/packages.module';
import { SidebarModule } from 'ng-sidebar';
import {SharedModulesModule} from './modules/shared-modules/shared-modules.module';
import { NgxFileDropModule } from 'ngx-file-drop';

@NgModule({
    declarations: [
        AppComponent,
    ],
    imports: [
        BrowserModule,
        NgbModule,
        AngularFontAwesomeModule,
        AppRoutingModule,
        NoopAnimationsModule,
        MatTabsModule,
        HttpClientModule,
        PackagesModule,
        SharedModulesModule,
        NgxFileDropModule,
    ],

    providers: [ApiService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
