import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {PackagesDashboardComponent} from './packages-dashboard/packages-dashboard.component';
import {DesignerComponent} from './designer/designer.component';
import {PackageCreationComponent} from './package-creation/package-creation.component';
import {ConfigurationDashboardComponent} from './configuration-dashboard/configuration-dashboard.component';


const routes: Routes = [
    {
        path: '',
        component: PackagesDashboardComponent
    },
    {path: 'designer', component: DesignerComponent},
    {path: 'package/:id', component: ConfigurationDashboardComponent},
    {path: 'createPackage', component: PackageCreationComponent},
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PackagesRoutingModule {
}
