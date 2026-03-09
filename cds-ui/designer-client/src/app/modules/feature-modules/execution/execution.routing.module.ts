import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExecutionDashboardComponent } from './execution-dashboard/execution-dashboard.component';

const routes: Routes = [
    {
        path: '',
        component: ExecutionDashboardComponent,
    },
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule],
})
export class ExecutionRoutingModule {
}
