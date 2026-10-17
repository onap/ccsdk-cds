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
import { ConfigSnapshotsComponent } from './config-snapshots.component';
import { ResolutionApiService } from '../resolution-api.service';

describe('ConfigSnapshotsComponent', () => {
    let fixture: ComponentFixture<ConfigSnapshotsComponent>;
    let component: ConfigSnapshotsComponent;
    let api: jasmine.SpyObj<ResolutionApiService>;

    beforeEach(async(() => {
        api = jasmine.createSpyObj('ResolutionApiService',
            ['getConfigSnapshot', 'getConfigSnapshotsByType', 'errorMessage']);
        api.errorMessage.and.returnValue('boom');
        TestBed.configureTestingModule({
            imports: [FormsModule],
            declarations: [ConfigSnapshotsComponent],
            providers: [{ provide: ResolutionApiService, useValue: api }],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(ConfigSnapshotsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    function snapshots(running: string, candidate: string) {
        api.getConfigSnapshot.and.callFake((type, id, status) => of(status === 'RUNNING' ? running : candidate));
    }

    it('shows running and candidate side by side', () => {
        snapshots('<running/>', '<candidate/>');
        component.resourceType = 'PNF';
        component.resourceId = 'pnf-1';

        component.show();
        fixture.detectChanges();

        expect(api.getConfigSnapshot).toHaveBeenCalledWith('PNF', 'pnf-1', 'RUNNING');
        expect(api.getConfigSnapshot).toHaveBeenCalledWith('PNF', 'pnf-1', 'CANDIDATE');
        const panes = fixture.nativeElement.querySelectorAll('pre');
        expect(panes[0].textContent).toBe('<running/>');
        expect(panes[1].textContent).toBe('<candidate/>');
        expect(fixture.nativeElement.textContent).not.toContain('are identical');
    });

    it('tells when a snapshot is missing or both are identical', () => {
        snapshots('<same/>', '<same/>');
        component.resourceType = 'PNF';
        component.resourceId = 'pnf-1';
        component.show();
        fixture.detectChanges();
        expect(fixture.nativeElement.textContent).toContain('are identical');

        snapshots('<running/>', '');
        component.show();
        fixture.detectChanges();
        expect(fixture.nativeElement.textContent).toContain('No candidate snapshot is stored.');
    });

    it('needs both resource type and ID to show snapshots', () => {
        component.resourceType = 'PNF';
        expect(component.canShow).toBe(false);
        expect(component.canList).toBe(true);
        component.show();
        expect(api.getConfigSnapshot).not.toHaveBeenCalled();
    });

    it('lists resources of a type and opens one of them', () => {
        api.getConfigSnapshotsByType.and.returnValue(of([
            { resourceType: 'PNF', resourceId: 'pnf-2', status: 'RUNNING', config_snapshot: 'x', createdDate: 'd' },
        ]));
        snapshots('<running/>', '');
        component.resourceType = 'PNF';

        component.list();
        fixture.detectChanges();
        const show: HTMLButtonElement = fixture.nativeElement.querySelector('tbody button');
        show.click();

        expect(component.resourceId).toBe('pnf-2');
        expect(api.getConfigSnapshot).toHaveBeenCalledWith('PNF', 'pnf-2', 'RUNNING');
    });

    it('shows processor errors', () => {
        api.getConfigSnapshot.and.returnValue(throwError(new HttpErrorResponse({ status: 500 })));
        component.resourceType = 'PNF';
        component.resourceId = 'pnf-1';

        component.show();
        fixture.detectChanges();

        expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('boom');
    });
});
