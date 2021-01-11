import { NgModule } from '@angular/core';
import { CommonModule, JsonPipe } from '@angular/common';
import { ApiService } from '../../../common/core/services/api.typed.service';
import { PackagesRoutingModule } from './packages.routing.module';
import { NgbPaginationModule } from '@ng-bootstrap/ng-bootstrap';
import { SharedModulesModule } from '../../shared-modules/shared-modules.module';
import { PackagesDashboardComponent } from './packages-dashboard/packages-dashboard.component';
import { PackageListComponent } from './packages-dashboard/package-list/package-list.component';
import { DesignerComponent } from './designer/designer.component';
import { SidebarModule } from 'ng-sidebar';
import { PackagePaginationComponent } from './packages-dashboard/package-pagination/package-pagination.component';
import { SortPackagesComponent } from './packages-dashboard/sort-packages/sort-packages.component';
import { PackagesHeaderComponent } from './packages-dashboard/packages-header/packages-header.component';
import { PackagesSearchComponent } from './packages-dashboard/search-by-packages/search-by-packages.component';
import { TagsFilteringComponent } from './packages-dashboard/filter-by-tags/filter-by-tags.component';
import { ConfigurationDashboardComponent } from './configuration-dashboard/configuration-dashboard.component';
import { ActionsComponent } from './designer/actions/actions.component';
import { PackageCreationComponent } from './package-creation/package-creation.component';
import { FormsModule } from '@angular/forms';
import { ImportsTabComponent } from './package-creation/imports-tab/imports-tab.component';
import { NgxFileDropModule } from 'ngx-file-drop';
import { TemplateMappingComponent } from './package-creation/template-mapping/template-mapping.component';
import { SourceEditorComponent } from './source-editor/source-editor.component';
import { ScriptsTabComponent } from './package-creation/scripts-tab/scripts-tab.component';
import { AceEditorModule } from 'ng2-ace-editor';
import { MetadataTabComponent } from './package-creation/metadata-tab/metadata-tab.component';
import { DslDefinitionsTabComponent } from './package-creation/dsl-definitions-tab/dsl-definitions-tab.component';
import { TemplMappCreationComponent } from './package-creation/template-mapping/templ-mapp-creation/templ-mapp-creation.component';
import { TemplMappListingComponent } from './package-creation/template-mapping/templ-mapp-listing/templ-mapp-listing.component';
import { DataTablesModule } from 'angular-datatables';
import { DesignerSourceViewComponent } from './designer/source-view/source-view.component';
import { NgxUiLoaderModule } from 'ngx-ui-loader';
import { TourMatMenuModule } from 'ngx-tour-md-menu';
import { ComponentCanDeactivateGuard } from '../../../common/core/canDactivate/ComponentCanDeactivateGuard';
import { ImportPackageComponent } from './packages-dashboard/import-package/import-package.component';
import { FunctionsAttributeComponent } from './designer/functions-attribute/functions-attribute.component';
import { ActionAttributesComponent } from './designer/action-attributes/action-attributes.component';
import { MatInputModule, MatPaginatorModule, MatProgressSpinnerModule, MatSortModule, MatTableModule } from '@angular/material';
import { TopologyTemplateComponent } from './package-creation/topology-template/topology-template.component';
import { CollapseModule } from 'ngx-bootstrap/collapse';

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
        ActionsComponent,
        PackageCreationComponent,
        ImportsTabComponent,
        TemplateMappingComponent,
        TemplMappCreationComponent,
        TemplMappListingComponent,
        SourceEditorComponent,
        ScriptsTabComponent,
        MetadataTabComponent,
        DslDefinitionsTabComponent,
        DesignerSourceViewComponent,
        ImportPackageComponent,
        FunctionsAttributeComponent,
        ActionAttributesComponent,
        TopologyTemplateComponent,

    ],
    imports: [
        CommonModule,
        PackagesRoutingModule,
        NgbPaginationModule,
        SharedModulesModule,
        SidebarModule.forRoot(),
        FormsModule,
        NgxFileDropModule,
        AceEditorModule,
        DataTablesModule,
        // Import NgxUiLoaderModule
        NgxUiLoaderModule.forRoot({
            bgsColor: 'red',
            bgsOpacity: 0.5,
            bgsPosition: 'bottom-right',
            bgsSize: 60,
            bgsType: 'ball-spin-clockwise',
            blur: 5,
            delay: 0,
            fgsColor: '#63bdba',
            fgsPosition: 'center-center',
            fgsSize: 60,
            fgsType: 'rectangle-bounce',
            gap: 24,
            logoPosition: 'center-center',
            logoSize: 120,
            logoUrl: 'assets/img/logo-icon.svg',
            masterLoaderId: 'master',
            overlayBorderRadius: '0',
            overlayColor: 'rgba(40, 40, 40, 0.8)',
            pbColor: '#63bdba',
            pbDirection: 'ltr',
            pbThickness: 3,
            hasProgressBar: true,
            text: '',
            textColor: '#FFFFFF',
            textPosition: 'center-center',
            maxTime: -1,
            minTime: 300
        }),
        TourMatMenuModule.forRoot(),
        MatInputModule,
        MatTableModule,
        MatPaginatorModule,
        MatSortModule,
        MatProgressSpinnerModule,
        CollapseModule
    ],
    providers: [ApiService, JsonPipe, ComponentCanDeactivateGuard],
    bootstrap: []
})
export class PackagesModule {
}
