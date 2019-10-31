import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PackagesListComponent} from './packages-list/packages-list.component';
import {ApiService} from '../common/core/services/api.service';
import {PackagesComponent} from './packages.component';
import {PackagesRoutingModule} from './packages.routing.module';
import {PaginationConfigComponent} from './pagination-config/pagination-config.component';
import {NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';

@NgModule({
    declarations: [PackagesListComponent, PackagesComponent, PaginationConfigComponent],
    imports: [
        CommonModule,
        PackagesRoutingModule,
        NgbPaginationModule
    ],
    providers: [ApiService],
    bootstrap: [PackagesComponent]
})
export class PackagesModule {
}
