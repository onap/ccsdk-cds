/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2026 Deutsche Telekom AG.
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
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { BlueprintURLs, ResolutionURLs } from '../../../common/constants/app-constants';

export interface ResolutionLookup {
    bpName: string;
    bpVersion: string;
    artifactName: string;
    resolutionKey?: string;
    resourceType?: string;
    resourceId?: string;
}

export interface ResolvedResource {
    name: string;
    value: string;
    status: string;
    dictionaryName: string;
    dictionarySource: string;
    dictionaryVersion: number;
    occurrence: number;
    createdDate: string;
}

export interface ConfigSnapshot {
    resourceType: string;
    resourceId: string;
    status: 'RUNNING' | 'CANDIDATE';
    config_snapshot: string;
    createdDate: string;
}

export interface BlueprintSummary {
    artifactName: string;
    artifactVersion: string;
}

@Injectable({
    providedIn: 'root',
})
export class ResolutionApiService {

    constructor(private http: HttpClient) {
    }

    getBlueprints(): Observable<BlueprintSummary[]> {
        const params = new HttpParams()
            .set('offset', '0')
            .set('limit', '1000')
            .set('sort', 'DATE')
            .set('sortType', 'DESC');
        return this.http.get<any>(BlueprintURLs.getPagedBlueprints, { params }).pipe(
            map(pages => {
                const page = Array.isArray(pages) ? pages[0] : pages;
                return (page && page.content) || [];
            }));
    }

    getResolvedResources(lookup: ResolutionLookup): Observable<ResolvedResource[]> {
        return this.http.get<ResolvedResource[]>(ResolutionURLs.resources, { params: this.toParams(lookup) });
    }

    getTemplate(lookup: ResolutionLookup, occurrence?: number): Observable<string> {
        let params = this.toParams(lookup);
        if (occurrence) {
            params = params.set('occurrence', String(occurrence));
        }
        return this.http.get(ResolutionURLs.template, { params, responseType: 'text' });
    }

    getConfigSnapshot(resourceType: string, resourceId: string, status: 'RUNNING' | 'CANDIDATE'): Observable<string> {
        const params = new HttpParams()
            .set('resourceType', resourceType)
            .set('resourceId', resourceId)
            .set('status', status);
        return this.http.get(ResolutionURLs.configSnapshot, { params, responseType: 'text' });
    }

    getConfigSnapshotsByType(resourceType: string): Observable<ConfigSnapshot[]> {
        const params = new HttpParams().set('resourceType', resourceType);
        return this.http.get<ConfigSnapshot[]>(ResolutionURLs.configSnapshotsByType, { params });
    }

    /**
     * Error bodies from the processor are ErrorPayload JSON, but requests made
     * with responseType 'text' hand them over as an unparsed string.
     */
    errorMessage(error: HttpErrorResponse): string {
        let body = error.error;
        if (typeof body === 'string') {
            try {
                body = JSON.parse(body);
            } catch (e) {
                return body || error.message;
            }
        }
        return (body && body.message) || error.message;
    }

    private toParams(lookup: ResolutionLookup): HttpParams {
        let params = new HttpParams();
        Object.keys(lookup)
            .filter(key => lookup[key])
            .forEach(key => params = params.set(key, lookup[key]));
        return params;
    }
}
