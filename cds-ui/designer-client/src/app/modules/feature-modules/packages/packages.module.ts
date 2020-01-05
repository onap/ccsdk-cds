import {NgModule} from '@angular/core';
import {CommonModule, JsonPipe} from '@angular/common';
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
import {PackageCreationComponent} from './package-creation/package-creation.component';
import {FormsModule} from '@angular/forms';
import { ImportsTabComponent } from './package-creation/imports-tab/imports-tab.component';
import { NgxFileDropModule } from 'ngx-file-drop';

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
        PackageCreationComponent,
        ImportsTabComponent,
    ],
    imports: [
        CommonModule,
        PackagesRoutingModule,
        NgbPaginationModule,
        SharedModulesModule,
        SidebarModule.forRoot(),
        FormsModule,
        NgxFileDropModule,
    ],
    providers: [ApiService, JsonPipe],
    bootstrap: []
})
export class PackagesModule {
}
