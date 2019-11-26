import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApiService} from '../../../common/core/services/api.typed.service';
import {PackagesRoutingModule} from './packages.routing.module';
import {NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import { SharedModulesModule } from '../../shared-modules/shared-modules.module';
import {SearchByTagsComponent} from './packages-dashboard/search-by-tags/search-by-tags.component';
import { PackagesDashboardComponent } from './packages-dashboard/packages-dashboard.component';
import { PackageListComponent } from './packages-dashboard/package-list/package-list.component';
import { DesignerComponent } from './designer/designer.component';
import { SidebarModule } from 'ng-sidebar';
import { PackagePaginationComponent } from './packages-dashboard/package-pagination/package-pagination.component';


@NgModule({
    declarations: [PackagesDashboardComponent,
        SearchByTagsComponent,
        PackageListComponent,
        DesignerComponent,
        PackagePaginationComponent
    ],
    imports: [
        CommonModule,
        PackagesRoutingModule,
        NgbPaginationModule,
        SharedModulesModule,
        SidebarModule.forRoot(),
    ],
    providers: [ApiService],
    bootstrap: []
})
export class PackagesModule {
}
