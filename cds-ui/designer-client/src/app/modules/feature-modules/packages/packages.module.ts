import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ApiService} from '../../../common/core/services/api.typed.service';
import {PackagesRoutingModule} from './packages.routing.module';
import {NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import {SharedModulesModule} from '../../shared-modules/shared-modules.module';
import {PackagesDashboardComponent} from './packages-dashboard/packages-dashboard.component';
import {PackageListComponent} from './packages-dashboard/package-list/package-list.component';
import {DesignerComponent} from './designer/designer.component';
import {SidebarModule} from 'ng-sidebar';
import {PackagePaginationComponent} from './packages-dashboard/package-pagination/package-pagination.component';
import {SortPackagesComponent} from './packages-dashboard/sort-packages/sort-packages.component';
import {PackagesHeaderComponent} from './packages-dashboard/packages-header/packages-header.component';
import {PackagesSearchComponent} from './packages-dashboard/search-by-packages/search-by-packages.component';
import {TagsFilteringComponent} from './packages-dashboard/filter-by-tags/filter-by-tags.component';
import {ConfigurationDashboardComponent} from './configuration-dashboard/configuration-dashboard.component';
import {FunctionsComponent} from './designer/functions/functions.component';
import {ActionsComponent} from './designer/actions/actions.component';


@NgModule({
    declarations: [PackagesDashboardComponent,
        TagsFilteringComponent,
        PackageListComponent,
        DesignerComponent,
        PackagePaginationComponent,
        PackagesSearchComponent,
        SortPackagesComponent,
        ConfigurationDashboardComponent,
        PackagesHeaderComponent,
        FunctionsComponent,
        ActionsComponent,
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
