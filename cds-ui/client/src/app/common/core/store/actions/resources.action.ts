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
import { IResources } from '../models/resources.model';
import { IResourcesState } from '../models/resourcesState.model';

export const LOAD_RESOURCES = 'LOAD_RESOURCES';
export const LOAD_RESOURCES_SUCCESS = 'LOAD_RESOURCES_SUCCESS';
export const LOAD_RESOURCES_FAILURE = 'LOAD_RESOURCES_FAILURE';
export const UPDATE_RESOURCES ='UPDATE_RESOURCES';
export const SET_RESOURCES_STATE = 'SET Resources state';


export class LoadResources implements Action {
    readonly type = LOAD_RESOURCES;
    constructor(public startLoadSuccess?: boolean) {}
}

export class LoadResourcesSuccess implements Action {
    readonly type = LOAD_RESOURCES_SUCCESS;
    constructor(public payload: IResources) {}
}

export class LoadResourcesFailure implements Action {
    readonly type = LOAD_RESOURCES_FAILURE;
    constructor(public error: any) {}
}

export class SetResourcesState implements Action {
    readonly type = SET_RESOURCES_STATE;
    constructor(public payload: IResourcesState) {}
}

export class UpdateResources implements Action {
    readonly type = UPDATE_RESOURCES;
    constructor(public payload: IResources) {}
}

export type Actions = LoadResources | LoadResourcesSuccess | LoadResourcesFailure | SetResourcesState;