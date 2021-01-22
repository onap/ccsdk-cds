import {async, ComponentFixture, TestBed} from '@angular/core/testing';

import {PackagesHeaderComponent} from './packages-header.component';
import {PackagesStore} from '../../packages.store';
import {BrowserDynamicTestingModule, platformBrowserDynamicTesting} from '@angular/platform-browser-dynamic/testing';
import {PackagesDashboardState} from '../../model/packages-dashboard.state';
import {getBlueprintPageMock} from '../../blueprint.page.mock';
import {of} from 'rxjs';
import {By} from '@angular/platform-browser';

fdescribe('PackagesHeaderComponent', () => {
    let component: PackagesHeaderComponent;
    let fixture: ComponentFixture<PackagesHeaderComponent>;
    let packageStoreStub: Partial<PackagesStore>;
    let packageDashboardState;
    beforeEach(() => {
        packageDashboardState = new PackagesDashboardState();
        packageDashboardState.totalPackagesWithoutSearchorFilters = 9;

        packageStoreStub = {state$: of(packageDashboardState)};
        TestBed.resetTestEnvironment();
        TestBed.initTestEnvironment(BrowserDynamicTestingModule,
            platformBrowserDynamicTesting());
        TestBed.configureTestingModule({
            declarations: [PackagesHeaderComponent],
            providers: [
                {provide: PackagesStore, useValue: packageStoreStub}
            ]
        });
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PackagesHeaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should display the number of packages', () => {
        component.numberOfPackages = 10;
        const numberOfPackage = fixture.debugElement.query(By.css('#numberOfPackages'));
        const numberOfPackageElement: HTMLElement = numberOfPackage.nativeElement;
        fixture.detectChanges();
        expect(numberOfPackageElement.textContent).toContain('' + 10);
    });

    it('should equals number of packages at store ', async(() => {
        packageDashboardState.totalPackagesWithoutSearchorFilters = 17;
        packageStoreStub = {state$: of(packageDashboardState)};

        fixture = TestBed.createComponent(PackagesHeaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        const numberOfPackage = fixture.debugElement.query(By.css('#numberOfPackages'));
        const numberOfPackageElement: HTMLElement = numberOfPackage.nativeElement;
        fixture.whenStable().then(() => {
            fixture.detectChanges();
            expect(numberOfPackageElement.textContent).toContain('' + 17);
        });

    }));

});
