import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { BehaviorSubject, of, throwError } from 'rxjs';

import { PackageCreationComponent } from './package-creation.component';
import { PackageCreationStore } from './package-creation.store';
import { PackageCreationService } from './package-creation.service';
import { PackageCreationUtils } from './package-creation.utils';
import { ToastrService } from 'ngx-toastr';
import { TourService } from 'ngx-tour-md-menu';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DesignerStore } from '../designer/designer.store';
import { CBAPackage } from './mapping-models/CBAPacakge.model';
import { MetadataTabComponent } from './metadata-tab/metadata-tab.component';

describe('PackageCreationComponent', () => {
    let component: PackageCreationComponent;
    let fixture: ComponentFixture<PackageCreationComponent>;
    let packageCreationServiceSpy: jasmine.SpyObj<PackageCreationService>;
    let toastServiceSpy: jasmine.SpyObj<ToastrService>;
    let ngxServiceSpy: jasmine.SpyObj<NgxUiLoaderService>;
    let state$: BehaviorSubject<CBAPackage>;
    let router: Router;

    beforeEach(async(() => {
        packageCreationServiceSpy = jasmine.createSpyObj('PackageCreationService',
            ['savePackage', 'importDataDictionary', 'enrichCurrentPackage', 'deploy', 'checkBluePrintNameAndVersion']);
        packageCreationServiceSpy.savePackage.and.returnValue(of('{"id":"test-123"}'));
        packageCreationServiceSpy.importDataDictionary.and.returnValue(of({}));
        packageCreationServiceSpy.deploy.and.returnValue(of({}));
        packageCreationServiceSpy.checkBluePrintNameAndVersion.and.returnValue(Promise.resolve(false));
        packageCreationServiceSpy.enrichCurrentPackage.and.returnValue(
            Promise.resolve(of(new Blob(['zip'], { type: 'application/zip' }))));

        toastServiceSpy = jasmine.createSpyObj('ToastrService', ['success', 'error']);
        ngxServiceSpy = jasmine.createSpyObj('NgxUiLoaderService', ['start', 'stop']);

        state$ = new BehaviorSubject<CBAPackage>(new CBAPackage());
        const storeMock = {
            state$,
            changeMetaData: jasmine.createSpy('changeMetaData'),
        };
        const tourServiceMock = jasmine.createSpyObj('TourService', ['goto', 'initialize', 'start']);
        tourServiceMock.currentStep = null;
        tourServiceMock.events$ = of({});
        const designerStoreMock = { state$: of({ template: '' }) };
        const packageCreationUtilsMock = jasmine.createSpyObj('PackageCreationUtils', ['transformToJson']);
        packageCreationUtilsMock.transformToJson.and.returnValue('{}');

        TestBed.configureTestingModule({
            declarations: [PackageCreationComponent, MetadataTabComponent],
            imports: [RouterTestingModule, FormsModule],
            providers: [
                { provide: PackageCreationStore, useValue: storeMock },
                { provide: PackageCreationService, useValue: packageCreationServiceSpy },
                { provide: PackageCreationUtils, useValue: packageCreationUtilsMock },
                { provide: ToastrService, useValue: toastServiceSpy },
                { provide: TourService, useValue: tourServiceMock },
                { provide: NgxUiLoaderService, useValue: ngxServiceSpy },
                { provide: DesignerStore, useValue: designerStoreMock }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(PackageCreationComponent);
        component = fixture.componentInstance;
        router = TestBed.get(Router);
        spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
        fixture.detectChanges();
    });

    function fillMetadata(tags: string[] = ['test']) {
        const metadataTab = component.metadataTabComponent;
        metadataTab.metaDataTab.name = 'my-package';
        metadataTab.metaDataTab.version = '1.0.0';
        tags.forEach(tag => metadataTab.tags.add(tag));
        metadataTab.updateValidityState();
    }

    function packageWithTemplate(): CBAPackage {
        const cbaPackage = new CBAPackage();
        cbaPackage.metaData.name = 'my-package';
        cbaPackage.metaData.version = '1.0.0';
        cbaPackage.metaData.mode = 'Designer Mode';
        cbaPackage.metaData.templateTags.add('test');
        cbaPackage.templates.files.set('Templates/a-template.vtl', 'hostname ${hostname}');
        return cbaPackage;
    }

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('lifecycle state initialisation', () => {
        it('should start with ddImported false', () => {
            expect(component.lifecycle.ddImported).toBeFalsy();
        });

        it('should start with enriched false', () => {
            expect(component.lifecycle.enriched).toBeFalsy();
        });

        it('should start with published false', () => {
            expect(component.lifecycle.published).toBeFalsy();
        });

        it('should start with empty enrichError', () => {
            expect(component.lifecycle.enrichError).toBe('');
        });

        it('ddImportFile should be null initially', () => {
            expect(component.ddImportFile).toBeNull();
        });
    });

    describe('importDataDictionary', () => {
        it('should do nothing when ddImportFile is null', () => {
            component.ddImportFile = null;
            component.importDataDictionary();
            expect(packageCreationServiceSpy.importDataDictionary).not.toHaveBeenCalled();
        });

        it('should call importDataDictionary service and set ddImported on success', (done) => {
            const mockFile = new File(['[{"name":"stream-count"}]'], 'dd.json',
                { type: 'application/json' });
            component.ddImportFile = mockFile;
            packageCreationServiceSpy.importDataDictionary.and.returnValue(of({}));

            component.importDataDictionary();

            setTimeout(() => {
                expect(packageCreationServiceSpy.importDataDictionary)
                    .toHaveBeenCalledWith([{ name: 'stream-count' }]);
                expect(component.lifecycle.ddImported).toBeTruthy();
                expect(toastServiceSpy.success).toHaveBeenCalled();
                done();
            }, 100);
        });

        it('should show error toast on import failure', (done) => {
            const mockFile = new File(['[{}]'], 'dd.json', { type: 'application/json' });
            component.ddImportFile = mockFile;
            packageCreationServiceSpy.importDataDictionary.and.returnValue(
                throwError({ status: 400, message: 'Bad Request' }));

            component.importDataDictionary();

            setTimeout(() => {
                expect(toastServiceSpy.error).toHaveBeenCalled();
                expect(component.lifecycle.ddImported).toBeFalsy();
                done();
            }, 100);
        });

        it('should show error toast on invalid JSON file', (done) => {
            const mockFile = new File(['not valid json'], 'dd.json', { type: 'application/json' });
            component.ddImportFile = mockFile;

            component.importDataDictionary();

            setTimeout(() => {
                expect(toastServiceSpy.error).toHaveBeenCalled();
                done();
            }, 100);
        });
    });

    describe('publishCurrentPackage', () => {
        it('should not call deploy when no enriched blob', () => {
            component.publishCurrentPackage();
            expect(packageCreationServiceSpy.deploy).not.toHaveBeenCalled();
        });

        it('should call deploy and set published on success', () => {
            (component as any).enrichedBlob = new Blob(['zip'], { type: 'application/zip' });
            component.lifecycle.enriched = true;

            component.publishCurrentPackage();

            expect(packageCreationServiceSpy.deploy).toHaveBeenCalled();
            expect(ngxServiceSpy.start).toHaveBeenCalled();
        });

        it('should set lifecycle.published true on successful deploy', () => {
            (component as any).enrichedBlob = new Blob(['zip'], { type: 'application/zip' });
            packageCreationServiceSpy.deploy.and.returnValue(of({}));

            component.publishCurrentPackage();

            expect(component.lifecycle.published).toBeTruthy();
            expect(toastServiceSpy.success).toHaveBeenCalled();
            expect(ngxServiceSpy.stop).toHaveBeenCalled();
        });

        it('should open the published package and not ask about unsaved changes', () => {
            state$.next(packageWithTemplate());
            (component as any).enrichedBlob = new Blob(['zip'], { type: 'application/zip' });
            packageCreationServiceSpy.deploy.and.returnValue(of('{"id":"bp-42","artifactName":"my-package"}'));

            component.publishCurrentPackage();

            expect(router.navigate).toHaveBeenCalledWith(['/packages/package/bp-42']);
            expect(component.canDeactivate()).toBeFalsy();
        });

        it('should show error toast on publish failure', () => {
            (component as any).enrichedBlob = new Blob(['zip'], { type: 'application/zip' });
            packageCreationServiceSpy.deploy.and.returnValue(
                throwError({ status: 500, message: 'Internal Server Error' }));

            component.publishCurrentPackage();

            expect(toastServiceSpy.error).toHaveBeenCalled();
            expect(component.lifecycle.published).toBeFalsy();
        });
    });

    describe('isSaveEnabled reflects metadata validity', () => {
        it('should be false with empty package (no name/version)', () => {
            expect(component.isSaveEnabled).toBeFalsy();
        });

        it('should be false without a tag', () => {
            fillMetadata([]);
            expect(component.isSaveEnabled).toBeFalsy();
        });

        it('should be true with name, version and a tag', () => {
            fillMetadata();
            expect(component.isSaveEnabled).toBeTruthy();
        });
    });

    describe('enrichment', () => {
        it('should not allow enrich without a template', () => {
            fillMetadata();
            expect(component.canEnrich()).toBeFalsy();
        });

        it('should allow enrich once a template exists', () => {
            fillMetadata();
            state$.next(packageWithTemplate());
            expect(component.canEnrich()).toBeTruthy();
        });

        it('should discard the enriched package when the package changes', async () => {
            fillMetadata();
            state$.next(packageWithTemplate());

            component.enrichCurrentPackage();
            await fixture.whenStable();
            expect(component.lifecycle.enriched).toBeTruthy();

            const changed = packageWithTemplate();
            changed.templates.files.set('Templates/a-template.vtl', 'hostname ${new-hostname}');
            state$.next(changed);

            expect(component.lifecycle.enriched).toBeFalsy();
            component.publishCurrentPackage();
            expect(packageCreationServiceSpy.deploy).not.toHaveBeenCalled();
        });

        it('should keep the enriched package when the package did not change', async () => {
            fillMetadata();
            state$.next(packageWithTemplate());

            component.enrichCurrentPackage();
            await fixture.whenStable();
            state$.next(packageWithTemplate());

            expect(component.lifecycle.enriched).toBeTruthy();
        });
    });

    describe('canDeactivate', () => {
        it('should not ask on an untouched page', () => {
            expect(component.canDeactivate()).toBeFalsy();
        });

        it('should ask once a name was entered, even without a valid version', () => {
            component.metadataTabComponent.metaDataTab.name = 'my-package';
            expect(component.canDeactivate()).toBeTruthy();
        });
    });
});
