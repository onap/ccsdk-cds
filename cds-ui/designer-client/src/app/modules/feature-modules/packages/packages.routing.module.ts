import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {PackagesDashboardComponent} from './packages-dashboard/packages-dashboard.component';
import {DesignerComponent} from './designer/designer.component';
import {PackageCreationComponent} from './package-creation/package-creation.component';
import {ConfigurationDashboardComponent} from './configuration-dashboard/configuration-dashboard.component';
import {DesignerSourceViewComponent} from './designer/source-view/source-view.component';
import {ComponentCanDeactivateGuard} from '../../../common/core/canDactivate/ComponentCanDeactivateGuard';


const routes: Routes = [
    {
        path: '',
        component: PackagesDashboardComponent
    },
    {path: 'designer/:id', component: DesignerComponent},
    {path: 'designer/source/:id', component: DesignerSourceViewComponent},
    {path: 'package/:id', component: ConfigurationDashboardComponent, canDeactivate: [ComponentCanDeactivateGuard]},
    {path: 'createPackage', component: PackageCreationComponent, canDeactivate: [ComponentCanDeactivateGuard]}
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PackagesRoutingModule {
}
