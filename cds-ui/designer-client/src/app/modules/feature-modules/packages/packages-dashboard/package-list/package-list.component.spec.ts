import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { of, throwError } from 'rxjs';

import { PackageListComponent } from './package-list.component';
import { PackagesStore } from '../../packages.store';
import { ConfigurationDashboardService } from '../../configuration-dashboard/configuration-dashboard.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { TourService } from 'ngx-tour-md-menu';
import { ToastrService } from 'ngx-toastr';
import { getBluePrintPageMock } from '../../blueprint.page.mock';
import { PackagesDashboardState } from '../../model/packages-dashboard.state';

describe('PackageListComponent', () => {
    let component: PackageListComponent;
    let fixture: ComponentFixture<PackageListComponent>;
    let toastServiceSpy: jasmine.SpyObj<ToastrService>;
    let configDashboardMock: jasmine.SpyObj<ConfigurationDashboardService>;
    let packagesStoreMock;

    beforeEach(async(() => {
        const dashBoard = new PackagesDashboardState();
        dashBoard.filteredPackages = getBluePrintPageMock();
        packagesStoreMock = { state$: of(dashBoard), getAll: jasmine.createSpy('getAll') };

        configDashboardMock = jasmine.createSpyObj('ConfigurationDashboardService',
            ['downloadResource', 'deletePackage']);
        configDashboardMock.deletePackage.and.returnValue(of({}));

        const ngxLoaderMock = jasmine.createSpyObj('NgxUiLoaderService', ['start', 'stop']);
        const tourServiceMock = jasmine.createSpyObj('TourService', ['initialize', 'start']);

        toastServiceSpy = jasmine.createSpyObj('ToastrService', ['success', 'error']);

        TestBed.configureTestingModule({
            declarations: [PackageListComponent],
            imports: [RouterTestingModule],
            providers: [
                { provide: PackagesStore, useValue: packagesStoreMock },
                { provide: ConfigurationDashboardService, useValue: configDashboardMock },
                { provide: NgxUiLoaderService, useValue: ngxLoaderMock },
                { provide: TourService, useValue: tourServiceMock },
                { provide: ToastrService, useValue: toastServiceSpy }
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

    it('deletePackage should show success toast and reload the packages', () => {
        const navigateSpy = spyOn(TestBed.get(Router), 'navigate');
        component.deletePackage('id-1');
        expect(configDashboardMock.deletePackage).toHaveBeenCalledWith('id-1');
        expect(toastServiceSpy.success).toHaveBeenCalled();
        expect(navigateSpy).toHaveBeenCalledWith(['/packages']);
        expect(packagesStoreMock.getAll).toHaveBeenCalledTimes(2);
    });

    it('deletePackage should show error toast when deletion fails', () => {
        configDashboardMock.deletePackage.and.returnValue(throwError({ message: 'boom' }));
        component.deletePackage('id-1');
        expect(toastServiceSpy.error).toHaveBeenCalledWith('Error has occured during deleting package boom');
    });
});
