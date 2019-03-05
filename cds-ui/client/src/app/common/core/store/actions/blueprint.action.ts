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
import { Action, Store } from '@ngrx/store';
import { IBlueprint } from '../models/blueprint.model';
import { IBlueprintState } from '../models/blueprintState.model';

export const LOAD_BLUEPRINT = 'LOAD_BLUEPRINT';
export const LOAD_BLUEPRINT_SUCCESS = 'LOAD_BLUEPRINT_SUCCESS';
export const LOAD_BLUEPRINT_FAILURE = 'LOAD_BLUEPRINT_FAILURE';
export const UPDATE_BLUEPRINT ='UPDATE_BLUEPRINT';
export const UPDATE_BLUEPRINT_SUCCESS = 'UPDATE_BLUEPRINT_SUCCESS';
export const UPDATE_BLUEPRINT_FAILURE = 'UPDATE_BLUEPRINT_FAILURE';
export const SAVE_BLUEPRINT = 'SAVE_BLUEPRINT';
export const SAVE_BLUEPRINT_SUCCESS = 'SAVE_BLUEPRINT_SUCCESS';
export const SAVE_BLUEPRINT_FAILURE = 'SAVE_BLUEPRINT_FAILURE';

export const SET_BLUEPRINT = 'SET Blueprint';
export const REMOVE_BLUEPRINT = 'Remove Blueprint';

export const SET_BLUEPRINT_STATE = 'SET Blueprint state';


export class LoadBlueprint implements Action {
    readonly type = LOAD_BLUEPRINT;
    constructor(public startLoadSuccess?: boolean) {}
}

export class LoadBlueprintSuccess implements Action {
    readonly type = LOAD_BLUEPRINT_SUCCESS;
    constructor(public payload: IBlueprint) {}
}

export class LoadBlueprintFailure implements Action {
    readonly type = LOAD_BLUEPRINT_FAILURE;
    constructor(public error: any) {}
}

export class SetBlueprintState implements Action {
    readonly type = SET_BLUEPRINT_STATE;
    constructor(public payload: IBlueprintState) {}
}

// export class SetBlueprint implements Action {
//     readonly type = SET_BLUEPRINT;
//     constructor(public payload: Blueprint) {}
// }

// export class RemoveBlueprint implements Action {
//     readonly type = REMOVE_BLUEPRINT;
//     constructor(public payload: Blueprint) {}
// }

export class UpdateBlueprint implements Action {
    readonly type = UPDATE_BLUEPRINT;
    constructor(public payload: IBlueprint) {}
}

export type Actions = LoadBlueprint | LoadBlueprintSuccess | LoadBlueprintFailure | SetBlueprintState;