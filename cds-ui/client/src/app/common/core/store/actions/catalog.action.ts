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

import { Injectable } from '@angular/core';
import { Action, Store } from '@ngrx/store';
import { ICatalog } from '../models/catalog.model';
import { ICatalogState } from '../models/catalogState.model';

export const LOAD_CATALOG = 'LOAD_CATALOG';
export const LOAD_CATALOG_SUCCESS = 'LOAD_CATALOG_SUCCESS';
export const LOAD_CATALOG_FAILURE = 'LOAD_CATALOG_FAILURE';
export const UPDATE_CATALOG ='UPDATE_CATALOG';
export const UPDATE_CATALOG_SUCCESS = 'UPDATE_CATALOG_SUCCESS';
export const UPDATE_CATALOG_FAILURE = 'UPDATE_CATALOG_FAILURE';
export const SAVE_CATALOG = 'SAVE_CATALOG';
export const SAVE_CATALOG_SUCCESS = 'SAVE_CATALOG_SUCCESS';
export const SAVE_CATALOG_FAILURE = 'SAVE_CATALOG_FAILURE';

export const SET_CATALOG = 'SET CATALOG';
export const REMOVE_CATALOG = 'Remove CATALOG';

export const SET_CATALOG_STATE = 'SET CATALOG state';


export class LoadCatalog implements Action {
    readonly type = LOAD_CATALOG;
    constructor(public startLoadSuccess?: boolean) {}
}

export class LoadCatalogSuccess implements Action {
    readonly type = LOAD_CATALOG_SUCCESS;
    constructor(public payload: ICatalog) {}
}

export class LoadCatalogFailure implements Action {
    readonly type = LOAD_CATALOG_FAILURE;
    constructor(public error: any) {}
}

export class SetCatalogState implements Action {
    readonly type = SET_CATALOG_STATE;
    constructor(public payload: ICatalogState) {}
}

// export class SetCatalog implements Action {
//     readonly type = SET_CATALOG;
//     constructor(public payload: Catalog) {}
// }

// export class RemoveCatalog implements Action {
//     readonly type = REMOVE_CATALOG;
//     constructor(public payload: Catalog) {}
// }

export class UpdateCatalog implements Action {
    readonly type = UPDATE_CATALOG;
    constructor(public payload: ICatalog) {}
}

export type Actions = LoadCatalog | LoadCatalogSuccess | LoadCatalogFailure | SetCatalogState;