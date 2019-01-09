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

import { Action } from '@ngrx/store';
import { IBlueprint } from '../models/blueprint.model';
import { IBlueprintState } from '../models/blueprintState.model';
import { initialBlueprintState } from '../state/blueprint.state';
import * as BlueprintActions from '../actions/blueprint.action';

// const initialState: BlueprintState = {
//     blueprint: {
//         metadata: {
//             template_author: '',
//             author_email: '',
//             user_groups: '',
//             template_name: '',
//             template_version: '',
//             template_tags: ''
//         },
//         fileImports: [{file:''}],
//         toplogyTemplates: 'temp'        
//     },
//     isLoadSuccess: false,
//     isUpdateSuccess: false,
//     isSaveSuccess: false
// }


export function blueprintReducer(state: IBlueprintState = initialBlueprintState, action: BlueprintActions.Actions) : IBlueprintState {
    switch(action.type) {
        case BlueprintActions.LOAD_BLUEPRINT_SUCCESS:
            return {...state,
                    blueprint: action.payload
                    }
        default:
            return state;
    }
}
