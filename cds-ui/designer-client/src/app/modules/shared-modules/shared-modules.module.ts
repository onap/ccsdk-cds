import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HeaderComponent } from './header/header.component';
import { TooltipModule } from 'ngx-bootstrap/tooltip';



@NgModule({
  declarations: [HeaderComponent],
  imports: [
    CommonModule,
    TooltipModule.forRoot(),
  ], exports : [HeaderComponent, TooltipModule,]
})
export class SharedModulesModule { }
