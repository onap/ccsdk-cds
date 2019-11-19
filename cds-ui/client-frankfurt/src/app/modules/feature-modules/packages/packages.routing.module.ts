import {NgModule} from '@angular/core';
import {Routes, RouterModule} from '@angular/router';
import { DesignerComponent } from './designer/designer.component';


const routes: Routes = [
    { path: 'designer', component: DesignerComponent },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class PackagesRoutingModule {
}
