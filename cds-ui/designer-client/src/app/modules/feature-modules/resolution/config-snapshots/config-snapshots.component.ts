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

import { Component } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { ConfigSnapshot, ResolutionApiService } from '../resolution-api.service';

type LoadState = 'idle' | 'loading' | 'loaded' | 'error';

@Component({
    selector: 'app-config-snapshots',
    templateUrl: './config-snapshots.component.html',
    styleUrls: ['../resolution.css'],
})
export class ConfigSnapshotsComponent {

    resourceType = '';
    resourceId = '';

    snapshotState: LoadState = 'idle';
    snapshotError = '';
    shownType = '';
    shownId = '';
    running = '';
    candidate = '';

    listState: LoadState = 'idle';
    listError = '';
    listedType = '';
    snapshots: ConfigSnapshot[] = [];

    constructor(private api: ResolutionApiService) {
    }

    get canShow(): boolean {
        return !!(this.resourceType.trim() && this.resourceId.trim());
    }

    get canList(): boolean {
        return !!this.resourceType.trim();
    }

    get identical(): boolean {
        return !!this.running && this.running === this.candidate;
    }

    show() {
        if (!this.canShow) {
            return;
        }
        const type = this.resourceType.trim();
        const id = this.resourceId.trim();
        this.snapshotState = 'loading';
        forkJoin(
            this.api.getConfigSnapshot(type, id, 'RUNNING'),
            this.api.getConfigSnapshot(type, id, 'CANDIDATE'),
        ).subscribe(
            ([running, candidate]) => {
                this.shownType = type;
                this.shownId = id;
                this.running = running || '';
                this.candidate = candidate || '';
                this.snapshotState = 'loaded';
            },
            (error: HttpErrorResponse) => {
                this.snapshotState = 'error';
                this.snapshotError = this.api.errorMessage(error);
            });
    }

    list() {
        if (!this.canList) {
            return;
        }
        const type = this.resourceType.trim();
        this.listState = 'loading';
        this.api.getConfigSnapshotsByType(type).subscribe(
            snapshots => {
                this.listedType = type;
                this.snapshots = snapshots || [];
                this.listState = 'loaded';
            },
            (error: HttpErrorResponse) => {
                this.listState = 'error';
                this.listError = this.api.errorMessage(error);
            });
    }

    showSnapshotsOf(snapshot: ConfigSnapshot) {
        this.resourceType = snapshot.resourceType;
        this.resourceId = snapshot.resourceId;
        this.show();
    }
}
