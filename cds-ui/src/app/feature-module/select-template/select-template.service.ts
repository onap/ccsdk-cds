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
import { HttpClient } from '@angular/common/http';
import { Observable} from 'rxjs';
import { Store } from '@ngrx/store';
import { Blueprint } from '../../common/store/models/blueprint.model';
import { Appstate } from '../../common/store/app.state';
import { LoadBlueprint, LoadBlueprintSuccess, LoadBlueprintFailure, SetBlueprint } from '../../common/store/actions/blueprint.action';

@Injectable({
  providedIn: 'root'
})
export class SelectTemplateService {

  constructor(private http: HttpClient, private store: Store<Appstate>) { }

  /**
   * example method to load blueprint data and update in store
   */
  LoadTemplateData() {
    this.store.dispatch(new LoadBlueprint(true));
    this.http.get('../../../assets/Configurations/sampleTemplate.json')
    .subscribe((data : Blueprint)=>{
      this.store.dispatch(new SetBlueprint(data));
      this.store.dispatch(new LoadBlueprintSuccess(true))
    },
    error=>{
      this.store.dispatch(new LoadBlueprintFailure(false));
    });
  }
}
