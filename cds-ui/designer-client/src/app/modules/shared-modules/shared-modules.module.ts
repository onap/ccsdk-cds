import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { HeaderActionsComponent } from './header-actions/header-actions.component';
import { RouterModule } from '@angular/router';
import { TooltipModule } from 'ngx-bootstrap/tooltip';

@NgModule({
  declarations: [HeaderComponent, HeaderActionsComponent],
  imports: [
    CommonModule,
    RouterModule,
    TooltipModule.forRoot(),
  ],
  exports : [HeaderComponent, HeaderActionsComponent, TooltipModule, ]
})
export class SharedModulesModule { }
