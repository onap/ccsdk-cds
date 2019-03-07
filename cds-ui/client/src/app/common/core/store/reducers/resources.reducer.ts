/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2019 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
import { Action } from '@ngrx/store';
import { IResources } from '../models/resources.model';
import { IResourcesState } from '../models/resourcesState.model';
import { initialResourcesState } from '../state/resources.state';
import * as ResourcesActions from '../actions/resources.action';

export function resourcesReducer(state: IResourcesState = initialResourcesState, action: ResourcesActions.Actions) : IResourcesState {
    switch(action.type) {
        case ResourcesActions.LOAD_RESOURCES_SUCCESS:
            return {...state,
                    resources: action.payload
                    }
        default:
            return state;
    }
}
