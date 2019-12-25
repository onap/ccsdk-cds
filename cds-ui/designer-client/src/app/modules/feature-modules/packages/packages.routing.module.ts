import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {PackagesDashboardComponent} from './packages-dashboard/packages-dashboard.component';
import {DesignerComponent} from './designer/designer.component';
import {PackageCreationComponent} from './package-creation/package-creation.component';


const routes: Routes = [
    {
        path: '',
        component: PackagesDashboardComponent
    },
    {path: 'designer', component: DesignerComponent},
    {path: 'createPackage', component: PackageCreationComponent},
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PackagesRoutingModule {
}
