import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';

import { ApiService } from './services/api.service';
// import { BlueprintService } from './services/blueprint.service';

@NgModule({
  declarations: [
  ],
  imports: [
    CommonModule,
    HttpClientModule
  ],
  providers : [ ApiService ]
})
export class CoreModule { }
