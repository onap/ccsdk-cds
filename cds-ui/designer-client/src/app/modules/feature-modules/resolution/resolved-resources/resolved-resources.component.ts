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

import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import {
    BlueprintSummary,
    ResolutionApiService,
    ResolutionLookup,
    ResolvedResource,
} from '../resolution-api.service';

type LoadState = 'idle' | 'loading' | 'loaded' | 'notFound' | 'error';

@Component({
    selector: 'app-resolved-resources',
    templateUrl: './resolved-resources.component.html',
    styleUrls: ['../resolution.css'],
})
export class ResolvedResourcesComponent implements OnInit {

    blueprints: BlueprintSummary[] = [];
    blueprintName = '';
    blueprintVersion = '';
    artifactName = '';
    lookupBy: 'resolutionKey' | 'resource' = 'resolutionKey';
    resolutionKey = '';
    resourceType = '';
    resourceId = '';

    resourcesState: LoadState = 'idle';
    resourcesError = '';
    resources: ResolvedResource[] = [];
    occurrences: number[] = [];
    selectedOccurrence: number = null;

    templateState: LoadState = 'idle';
    templateError = '';
    template = '';

    private lookup: ResolutionLookup;

    constructor(private api: ResolutionApiService) {
    }

    ngOnInit() {
        this.api.getBlueprints().subscribe(
            blueprints => this.blueprints = blueprints,
            () => this.blueprints = []);
    }

    get blueprintNames(): string[] {
        return Array.from(new Set(this.blueprints.map(b => b.artifactName))).sort();
    }

    get blueprintVersions(): string[] {
        return this.blueprints
            .filter(b => b.artifactName === this.blueprintName)
            .map(b => b.artifactVersion);
    }

    onBlueprintNameChange() {
        const versions = this.blueprintVersions;
        this.blueprintVersion = versions.length === 1 ? versions[0] : '';
    }

    get canLookUp(): boolean {
        if (!this.blueprintName || !this.blueprintVersion || !this.artifactName.trim()) {
            return false;
        }
        return this.lookupBy === 'resolutionKey'
            ? !!this.resolutionKey.trim()
            : !!(this.resourceType.trim() && this.resourceId.trim());
    }

    get occurrenceResources(): ResolvedResource[] {
        return this.resources
            .filter(r => r.occurrence === this.selectedOccurrence)
            .sort((a, b) => a.name.localeCompare(b.name));
    }

    lookUp() {
        if (!this.canLookUp) {
            return;
        }
        this.lookup = {
            bpName: this.blueprintName,
            bpVersion: this.blueprintVersion,
            artifactName: this.artifactName.trim(),
        };
        if (this.lookupBy === 'resolutionKey') {
            this.lookup.resolutionKey = this.resolutionKey.trim();
        } else {
            this.lookup.resourceType = this.resourceType.trim();
            this.lookup.resourceId = this.resourceId.trim();
        }

        this.resourcesState = 'loading';
        this.templateState = 'idle';
        this.resources = [];
        this.occurrences = [];
        this.selectedOccurrence = null;
        this.api.getResolvedResources(this.lookup).subscribe(
            resources => {
                this.resources = resources || [];
                this.occurrences = Array.from(new Set(this.resources.map(r => r.occurrence)))
                    .sort((a, b) => b - a);
                this.selectedOccurrence = this.occurrences.length ? this.occurrences[0] : null;
                this.resourcesState = this.resources.length ? 'loaded' : 'notFound';
                this.loadTemplate();
            },
            (error: HttpErrorResponse) => {
                this.resourcesState = 'error';
                this.resourcesError = this.api.errorMessage(error);
                this.loadTemplate();
            });
    }

    onOccurrenceChange() {
        this.loadTemplate();
    }

    private loadTemplate() {
        this.templateState = 'loading';
        this.template = '';
        this.api.getTemplate(this.lookup, this.selectedOccurrence).subscribe(
            template => {
                this.template = template || '';
                this.templateState = this.template ? 'loaded' : 'notFound';
            },
            (error: HttpErrorResponse) => {
                if (error.status === 404) {
                    this.templateState = 'notFound';
                } else {
                    this.templateState = 'error';
                    this.templateError = this.api.errorMessage(error);
                }
            });
    }
}
