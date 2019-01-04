/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/

import { Injectable } from '@angular/core';
import { Effect, ofType, Actions } from '@ngrx/effects';
import { Store, select } from '@ngrx/store';
import { of } from 'rxjs';
import { switchMap, map, withLatestFrom, catchError } from 'rxjs/operators';

import { IAppState } from '../state/app.state';
import * as BlueprintActions from '../actions/blueprint.action';
// import { IBlueprintHttp } from '../models/blueprint-http.model';
// import { BlueprintService } from '../../services/blueprint.service';
// import { BlueprintService } from '../../../feature-modules/blueprint/blueprint.service';

@Injectable()
export class BlueprintEffects {

  
  constructor(
   // private blueprintService: BlueprintService,
    private _actions$: Actions,
    private _store: Store<IAppState>
  ) {}
    
  // @Effect()
  // getBlueprint$ = this._actions$.pipe(
  //   ofType<BlueprintActions.LoadBlueprint>(BlueprintActions.LOAD_BLUEPRINT),
  //   switchMap(() => 
  //     this.blueprintService.loadBlueprint().pipe(
  //     map((blueprintResponse: any) => new  BlueprintActions.LoadBlueprintSuccess(blueprintResponse)),
  //     catchError((error : any) => of(new BlueprintActions.LoadBlueprintSuccess(error)))
  //   ),
   // ,
    // switchMap((blueprintResponse: any) => of(new BlueprintActions.LoadBlueprintSuccess(blueprintResponse)))
//   ),
// )
}