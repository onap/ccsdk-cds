import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';

import { PackageListComponent } from './package-list.component';
import { PackagesStore } from '../../packages.store';
import { ConfigurationDashboardService } from '../../configuration-dashboard/configuration-dashboard.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { TourService } from 'ngx-tour-md-menu';
import { ToastrService } from 'ngx-toastr';
import { PackageCreationStore } from '../../package-creation/package-creation.store';
import { getBluePrintPageMock } from '../../blueprint.page.mock';
import { PackagesDashboardState } from '../../model/packages-dashboard.state';

describe('PackageListComponent', () => {
    let component: PackageListComponent;
    let fixture: ComponentFixture<PackageListComponent>;
    let toastServiceSpy: jasmine.SpyObj<ToastrService>;
    let packageCreationStoreSpy: jasmine.SpyObj<PackageCreationStore>;

    beforeEach(async(() => {
        const dashBoard = new PackagesDashboardState();
        dashBoard.filteredPackages = getBluePrintPageMock();
        const packagesStoreMock = { state$: of(dashBoard), getAll: jasmine.createSpy('getAll') };

        const configDashboardMock = jasmine.createSpyObj('ConfigurationDashboardService',
            ['downloadResource', 'deletePackage']);
        configDashboardMock.deletePackage.and.returnValue(of({}));

        const ngxLoaderMock = jasmine.createSpyObj('NgxUiLoaderService', ['start', 'stop']);
        const tourServiceMock = jasmine.createSpyObj('TourService', ['initialize', 'start']);

        toastServiceSpy = jasmine.createSpyObj('ToastrService', ['success', 'error']);
        packageCreationStoreSpy = jasmine.createSpyObj('PackageCreationStore',
            ['clear', 'changeMetaData', 'addTemplate', 'addMapping', 'addScripts', 'changeDslDefinition']);

        TestBed.configureTestingModule({
            declarations: [PackageListComponent],
            imports: [RouterTestingModule],
            providers: [
                { provide: PackagesStore, useValue: packagesStoreMock },
                { provide: ConfigurationDashboardService, useValue: configDashboardMock },
                { provide: NgxUiLoaderService, useValue: ngxLoaderMock },
                { provide: TourService, useValue: tourServiceMock },
                { provide: ToastrService, useValue: toastServiceSpy },
                { provide: PackageCreationStore, useValue: packageCreationStoreSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(PackageListComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should populate viewedPackages from store state', () => {
        expect(component.viewedPackages.length).toBe(2);
    });

    it('should expose loadCbaFromZip method', () => {
        expect(typeof component.loadCbaFromZip).toBe('function');
    });

    it('loadCbaFromZip should do nothing when no file selected', () => {
        const event = { target: { files: [] } } as unknown as Event;
        component.loadCbaFromZip(event);
        expect(packageCreationStoreSpy.clear).not.toHaveBeenCalled();
    });

    it('loadCbaFromZip should clear store and show error on corrupt zip', async () => {
        const corruptBlob = new Blob(['not a zip'], { type: 'application/zip' });
        const file = new File([corruptBlob], 'test.zip', { type: 'application/zip' });
        const event = { target: { files: [file] } } as unknown as Event;

        component.loadCbaFromZip(event);
        expect(packageCreationStoreSpy.clear).toHaveBeenCalled();

        // Allow async rejection to settle
        await new Promise(resolve => setTimeout(resolve, 50));
        expect(toastServiceSpy.error).toHaveBeenCalled();
    });
});
