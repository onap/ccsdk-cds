import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AceEditorModule } from 'ng2-ace-editor';
import { SharedModulesModule } from '../../shared-modules/shared-modules.module';
import { ExecutionRoutingModule } from './execution.routing.module';
import { ExecutionDashboardComponent } from './execution-dashboard/execution-dashboard.component';
import { ExecutionSetupComponent } from './execution-setup/execution-setup.component';
import { ExecutionHistoryComponent } from './execution-history/execution-history.component';
import { LiveViewComponent } from './live-view/live-view.component';
import { ApiService } from '../../../common/core/services/api.typed.service';

@NgModule({
    declarations: [
        ExecutionDashboardComponent,
        ExecutionSetupComponent,
        ExecutionHistoryComponent,
        LiveViewComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        AceEditorModule,
        SharedModulesModule,
        ExecutionRoutingModule,
    ],
    providers: [ApiService],
})
export class ExecutionModule {
}
