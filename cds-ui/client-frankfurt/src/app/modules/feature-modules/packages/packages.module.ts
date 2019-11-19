import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PackagesRoutingModule} from './packages.routing.module';
import {NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import { DesignerComponent } from './designer/designer.component';
import { SidebarModule } from 'ng-sidebar';


@NgModule({
    declarations: [DesignerComponent],
    imports: [
        CommonModule,
        PackagesRoutingModule,
        NgbPaginationModule,
        SidebarModule.forRoot(),
    ],
    providers: [],
    bootstrap: []
})
export class PackagesModule {
}
