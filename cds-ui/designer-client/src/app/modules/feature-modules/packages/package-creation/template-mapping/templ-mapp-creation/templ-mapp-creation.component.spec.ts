import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { of } from 'rxjs';

import { TemplMappCreationComponent } from './templ-mapp-creation.component';
import { PackageCreationStore } from '../../package-creation.store';
import { TemplateStore, TemplateInfo } from '../../template.store';
import { PackageCreationUtils } from '../../package-creation.utils';
import { ToastrService } from 'ngx-toastr';
import { SharedService } from '../shared-service';
import { PackageCreationService } from '../../package-creation.service';
import { TourService } from 'ngx-tour-md-menu';

describe('TemplMappCreationComponent', () => {
    let component: TemplMappCreationComponent;
    let fixture: ComponentFixture<TemplMappCreationComponent>;
    let packageCreationServiceSpy: jasmine.SpyObj<PackageCreationService>;

    const templateInfo = new TemplateInfo();
    templateInfo.fileName = 'Templates/test-mapping.json';

    const packageCreationStoreMock = {
        state: {
            templates: { files: new Map<string, string>() },
            mapping: { files: new Map<string, string>() }
        },
        state$: of({}),
        fileExist: jasmine.createSpy('fileExist').and.returnValue(false),
        addMapping: jasmine.createSpy('addMapping'),
        addTemplate: jasmine.createSpy('addTemplate')
    };

    beforeEach(async(() => {
        packageCreationServiceSpy = jasmine.createSpyObj('PackageCreationService',
            ['getTemplateAndMapping']);
        packageCreationServiceSpy.getTemplateAndMapping.and.returnValue(of([]));

        const templateStoreMock = { state$: of(templateInfo) };
        const packageCreationUtilsMock = jasmine.createSpyObj('PackageCreationUtils', ['transformToJson']);
        packageCreationUtilsMock.transformToJson.and.returnValue('{}');
        const toastrMock = jasmine.createSpyObj('ToastrService', ['success', 'error']);
        const sharedServiceMock = { isEdit: () => of(false), deleteFromList: jasmine.createSpy() };
        const tourServiceMock = jasmine.createSpyObj('TourService', ['goto']);

        TestBed.configureTestingModule({
            declarations: [TemplMappCreationComponent],
            providers: [
                { provide: PackageCreationStore, useValue: packageCreationStoreMock },
                { provide: TemplateStore, useValue: templateStoreMock },
                { provide: PackageCreationUtils, useValue: packageCreationUtilsMock },
                { provide: ToastrService, useValue: toastrMock },
                { provide: SharedService, useValue: sharedServiceMock },
                { provide: PackageCreationService, useValue: packageCreationServiceSpy },
                { provide: TourService, useValue: tourServiceMock }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TemplMappCreationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize unmappedVars as empty array', () => {
        expect(component.unmappedVars).toEqual([]);
    });

    describe('extractTemplateVars', () => {
        it('should extract VTL dollar-brace variables', () => {
            const vars = (component as any).extractTemplateVars('${stream-count} and ${hostname}', 'vtl');
            expect(vars).toContain('stream-count');
            expect(vars).toContain('hostname');
        });

        it('should extract VTL bare dollar variables', () => {
            const vars = (component as any).extractTemplateVars('$name and $value', 'vtl');
            expect(vars).toContain('name');
            expect(vars).toContain('value');
        });

        it('should extract Jinja2 double-brace variables', () => {
            const vars = (component as any).extractTemplateVars('{{ hostname }} and {{ ip_address }}', 'j2');
            expect(vars).toContain('hostname');
            expect(vars).toContain('ip_address');
        });

        it('should return empty array for empty content', () => {
            expect((component as any).extractTemplateVars('', 'vtl')).toEqual([]);
        });

        it('should return empty array for null content', () => {
            expect((component as any).extractTemplateVars(null, 'vtl')).toEqual([]);
        });

        it('should deduplicate repeated variables', () => {
            const vars = (component as any).extractTemplateVars('$foo $foo $bar', 'vtl');
            expect(vars.length).toBe(2);
            expect(vars).toContain('foo');
            expect(vars).toContain('bar');
        });

        it('should return empty for unknown extension', () => {
            const vars = (component as any).extractTemplateVars('$foo', 'xml');
            expect(vars).toEqual([]);
        });
    });

    describe('updateUnmappedVars', () => {
        it('should list all template vars as unmapped when mappingRes is empty', () => {
            component.templateFileContent = '$stream_count $hostname';
            component.templateExt = 'vtl';
            component.mappingRes = [];
            component.updateUnmappedVars();
            expect(component.unmappedVars).toContain('stream_count');
            expect(component.unmappedVars).toContain('hostname');
        });

        it('should exclude already-mapped variables', () => {
            component.templateFileContent = '$stream_count $hostname';
            component.templateExt = 'vtl';
            component.mappingRes = [{ name: 'stream_count' } as any];
            component.updateUnmappedVars();
            expect(component.unmappedVars).not.toContain('stream_count');
            expect(component.unmappedVars).toContain('hostname');
        });

        it('should produce empty unmappedVars when all vars are mapped', () => {
            component.templateFileContent = '$foo';
            component.templateExt = 'vtl';
            component.mappingRes = [{ name: 'foo' } as any];
            component.updateUnmappedVars();
            expect(component.unmappedVars).toEqual([]);
        });

        it('should handle Jinja2 variables correctly', () => {
            component.templateFileContent = '{{ hostname }} {{ port }}';
            component.templateExt = 'j2';
            component.mappingRes = [{ name: 'hostname' } as any];
            component.updateUnmappedVars();
            expect(component.unmappedVars).toContain('port');
            expect(component.unmappedVars).not.toContain('hostname');
        });
    });

    describe('textChanges', () => {
        it('should update templateFileContent', () => {
            component.textChanges('new content $var1', 'Templates/test-template.vtl');
            expect(component.templateFileContent).toBe('new content $var1');
        });

        it('should update unmappedVars after content change', () => {
            component.templateExt = 'vtl';
            component.mappingRes = [];
            component.textChanges('$newVar', 'Templates/test-template.vtl');
            expect(component.unmappedVars).toContain('newVar');
        });

        it('should clear unmappedVars when template is empty', () => {
            component.unmappedVars = ['stale'];
            component.textChanges('', 'Templates/test-template.vtl');
            expect(component.unmappedVars).toEqual([]);
        });
    });
});
