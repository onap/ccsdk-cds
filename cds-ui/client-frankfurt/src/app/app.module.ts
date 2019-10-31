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
import {PackagesModule} from './packages/packages.module';


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
        PackagesModule
    ],
    providers: [ApiService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
