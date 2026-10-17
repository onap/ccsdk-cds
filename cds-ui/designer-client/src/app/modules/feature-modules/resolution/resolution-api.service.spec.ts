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

import { TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ResolutionApiService } from './resolution-api.service';

describe('ResolutionApiService', () => {
    let service: ResolutionApiService;
    let http: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
        });
        service = TestBed.get(ResolutionApiService);
        http = TestBed.get(HttpTestingController);
    });

    afterEach(() => http.verify());

    it('looks up resolved resources and drops empty keys', () => {
        service.getResolvedResources({
            bpName: 'vFW', bpVersion: '1.0.0', artifactName: 'baseconfig', resolutionKey: 'key-1', resourceId: '',
        }).subscribe(resources => expect(resources.length).toBe(1));

        const req = http.expectOne(r => r.url === '/resolution/resources');
        expect(req.request.params.keys().sort()).toEqual(['artifactName', 'bpName', 'bpVersion', 'resolutionKey']);
        expect(req.request.params.get('resolutionKey')).toBe('key-1');
        req.flush([{ name: 'hostname' }]);
    });

    it('requests a rendered template as text for a given occurrence', () => {
        service.getTemplate({ bpName: 'vFW', bpVersion: '1.0.0', artifactName: 'baseconfig', resolutionKey: 'key-1' }, 2)
            .subscribe(template => expect(template).toBe('hostname vfw-1'));

        const req = http.expectOne(r => r.url === '/resolution/template');
        expect(req.request.responseType).toBe('text');
        expect(req.request.params.get('occurrence')).toBe('2');
        req.flush('hostname vfw-1');
    });

    it('requests a config snapshot by status', () => {
        service.getConfigSnapshot('PNF', 'pnf-1', 'CANDIDATE').subscribe(snapshot => expect(snapshot).toBe('<config/>'));

        const req = http.expectOne(r => r.url === '/resolution/config-snapshot');
        expect(req.request.params.get('resourceType')).toBe('PNF');
        expect(req.request.params.get('resourceId')).toBe('pnf-1');
        expect(req.request.params.get('status')).toBe('CANDIDATE');
        req.flush('<config/>');
    });

    it('lists config snapshots by resource type', () => {
        service.getConfigSnapshotsByType('PNF').subscribe(snapshots => expect(snapshots.length).toBe(0));

        const req = http.expectOne(r => r.url === '/resolution/config-snapshots-by-type');
        expect(req.request.params.get('resourceType')).toBe('PNF');
        req.flush([]);
    });

    it('unwraps the blueprint page returned by the paged endpoint', () => {
        service.getBlueprints().subscribe(blueprints =>
            expect(blueprints).toEqual([{ artifactName: 'vFW', artifactVersion: '1.0.0' }]));

        const req = http.expectOne(r => r.url === '/controllerblueprint/paged');
        req.flush([{ content: [{ artifactName: 'vFW', artifactVersion: '1.0.0' }] }]);
    });

    it('reads the message from processor error payloads, also from text responses', () => {
        const payload = { code: 404, status: 'RESOURCE_NOT_FOUND', message: 'No records found' };
        expect(service.errorMessage(new HttpErrorResponse({ error: payload, status: 404 })))
            .toBe('No records found');
        expect(service.errorMessage(new HttpErrorResponse({ error: JSON.stringify(payload), status: 404 })))
            .toBe('No records found');
        expect(service.errorMessage(new HttpErrorResponse({ error: 'Bad gateway', status: 502 })))
            .toBe('Bad gateway');
    });
});
