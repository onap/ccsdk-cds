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

import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';
import { ResolvedResourcesComponent } from './resolved-resources.component';
import { ResolutionApiService } from '../resolution-api.service';

describe('ResolvedResourcesComponent', () => {
    let fixture: ComponentFixture<ResolvedResourcesComponent>;
    let component: ResolvedResourcesComponent;
    let api: jasmine.SpyObj<ResolutionApiService>;

    const resource = (name: string, occurrence: number) => ({
        name, value: name + '-value', status: 'success', dictionaryName: name, dictionarySource: 'input',
        dictionaryVersion: 0, occurrence, createdDate: '2026-10-17T10:00:00.000Z',
    });

    beforeEach(async(() => {
        api = jasmine.createSpyObj('ResolutionApiService',
            ['getBlueprints', 'getResolvedResources', 'getTemplate', 'errorMessage']);
        api.getBlueprints.and.returnValue(of([
            { artifactName: 'vFW', artifactVersion: '1.0.0' },
            { artifactName: 'vFW', artifactVersion: '2.0.0' },
            { artifactName: 'vDNS', artifactVersion: '1.0.0' },
        ]));
        api.errorMessage.and.callFake((e: HttpErrorResponse) => 'boom ' + e.status);
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [ResolvedResourcesComponent],
            providers: [{ provide: ResolutionApiService, useValue: api }],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ResolvedResourcesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    function fillResolutionKeyLookup() {
        component.blueprintName = 'vFW';
        component.blueprintVersion = '1.0.0';
        component.artifactName = 'baseconfig';
        component.resolutionKey = 'key-1';
    }

    it('offers each blueprint name once and preselects a single version', () => {
        expect(component.blueprintNames).toEqual(['vDNS', 'vFW']);
        component.blueprintName = 'vDNS';
        component.onBlueprintNameChange();
        expect(component.blueprintVersion).toBe('1.0.0');
        component.blueprintName = 'vFW';
        component.onBlueprintNameChange();
        expect(component.blueprintVersion).toBe('');
        expect(component.blueprintVersions).toEqual(['1.0.0', '2.0.0']);
    });

    it('needs either a resolution key or a resource type and ID', () => {
        fillResolutionKeyLookup();
        expect(component.canLookUp).toBe(true);
        component.lookupBy = 'resource';
        expect(component.canLookUp).toBe(false);
        component.resourceType = 'vnf';
        component.resourceId = 'vnf-1';
        expect(component.canLookUp).toBe(true);
    });

    it('shows the latest occurrence and loads its template', () => {
        api.getResolvedResources.and.returnValue(of([resource('b', 1), resource('a', 2), resource('b', 2)]));
        api.getTemplate.and.returnValue(of('hostname vfw-1'));
        fillResolutionKeyLookup();

        component.lookUp();
        fixture.detectChanges();

        expect(api.getResolvedResources).toHaveBeenCalledWith(
            { bpName: 'vFW', bpVersion: '1.0.0', artifactName: 'baseconfig', resolutionKey: 'key-1' });
        expect(component.occurrences).toEqual([2, 1]);
        expect(api.getTemplate).toHaveBeenCalledWith(jasmine.any(Object), 2);
        const rows = fixture.nativeElement.querySelectorAll('tbody tr');
        expect(rows.length).toBe(2);
        expect(rows[0].querySelector('th').textContent).toBe('a');
        expect(fixture.nativeElement.querySelector('pre').textContent).toBe('hostname vfw-1');
    });

    it('looks up by resource type and ID without sending a resolution key', () => {
        api.getResolvedResources.and.returnValue(of([]));
        api.getTemplate.and.returnValue(of('x'));
        fillResolutionKeyLookup();
        component.lookupBy = 'resource';
        component.resourceType = 'vnf';
        component.resourceId = 'vnf-1';

        component.lookUp();

        expect(api.getResolvedResources).toHaveBeenCalledWith(
            { bpName: 'vFW', bpVersion: '1.0.0', artifactName: 'baseconfig', resourceType: 'vnf', resourceId: 'vnf-1' });
    });

    it('says so when nothing is stored', () => {
        api.getResolvedResources.and.returnValue(of([]));
        api.getTemplate.and.returnValue(throwError(new HttpErrorResponse({ status: 404 })));
        fillResolutionKeyLookup();

        component.lookUp();
        fixture.detectChanges();

        expect(component.resourcesState).toBe('notFound');
        expect(component.templateState).toBe('notFound');
        const text = fixture.nativeElement.textContent;
        expect(text).toContain('No resolved resources are stored for these keys.');
        expect(text).toContain('No rendered template is stored for these keys.');
    });

    it('shows processor errors', () => {
        api.getResolvedResources.and.returnValue(throwError(new HttpErrorResponse({ status: 500 })));
        api.getTemplate.and.returnValue(throwError(new HttpErrorResponse({ status: 503 })));
        fillResolutionKeyLookup();

        component.lookUp();
        fixture.detectChanges();

        const alerts = fixture.nativeElement.querySelectorAll('[role="alert"]');
        expect(alerts.length).toBe(2);
        expect(alerts[0].textContent).toContain('boom 500');
        expect(alerts[1].textContent).toContain('boom 503');
    });
});
