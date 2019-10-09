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
import { initialCatalogState } from '../state/catalog.state';
import * as CatalogActions from '../actions/catalog.action';
import { ICatalogState } from '../models/catalogState.model';

export function catalogReducer(state: ICatalogState = initialCatalogState, action: CatalogActions.Actions) : ICatalogState {
    switch(action.type) {
        case CatalogActions.LOAD_CATALOG_SUCCESS:
            return {...state,
                    catalog: action.payload
                    }
        case CatalogActions.SET_CATALOG_STATE:
            return {...state,
                    catalog: action.payload.catalog
                    }
        default:
            return state;
    }
}
