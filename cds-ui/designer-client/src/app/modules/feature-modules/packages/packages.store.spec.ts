import { TestBed } from '@angular/core/testing';
import { PackagesStore } from './packages.store';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { PackagesApiService } from './packages-api.service';
import { of } from 'rxjs';
import { BlueprintPage } from './model/Blueprint.model';
import { getBlueprintPageMock } from './blueprint.page.mock';
import { PackagesDashboardState } from './model/packages-dashboard.state';

describe('PackagesStore', () => {
    //    store: PackagesStore;

    const MOCK_BLUEPRINTS_PAGE: BlueprintPage = getBlueprintPageMock();


    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                HttpClientTestingModule
            ],
            providers: [
                PackagesStore,
                PackagesApiService
            ]
        });
        httpMock = TestBed.get(HttpTestingController);

    });

    it('should correctly get page of packages', () => {
        const packagesServiceSpy = jasmine.createSpyObj('PackagesListService', ['getPagedPackages']);

        // set the value to return when the ` getPagedPackages` spy is called.
        packagesServiceSpy.getPagedPackages.and.returnValue(of([MOCK_BLUEPRINTS_PAGE]));
        // store = new PackagesStore(packagesServiceSpy);

        // Todo check the Abbas's code
        /*store.getPagedPackages(0, 2);
        store.state$.subscribe(page => {
            expect(store.state).toEqual(MOCK_BLUEPRINTS_PAGE);
        });*/

    });

    it('should correctly get all packages', () => {
        const packagesServiceSpy = jasmine.createSpyObj('PackagesListService', ['getPagedPackages']);

        // set the value to return when the `getPagedPackages` spy is called.
        packagesServiceSpy.getPagedPackages.and.returnValue(of([MOCK_BLUEPRINTS_PAGE]));
        // store = new PackagesStore(packagesServiceSpy);
        // store.getAll();
        // store.state$.subscribe(page => {
        //     expect(store.state.page).toEqual(MOCK_BLUEPRINTS_PAGE);
        // });

    });
});

