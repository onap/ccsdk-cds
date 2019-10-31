import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import {PackagesListComponent} from './packages-list/packages-list.component';


const routes: Routes = [
    {
        path: 'list',
        component: PackagesListComponent
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PackagesRoutingModule {
}
