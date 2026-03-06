import { DesignerComponent } from './designer.component';
import { DesignerStore } from './designer.store';
import { FunctionsStore } from './functions.store';
import { PackageCreationStore } from '../package-creation/package-creation.store';
import { PackageCreationUtils } from '../package-creation/package-creation.utils';
import { GraphUtil } from './graph.util';
import { GraphGenerator } from './graph.generator.util';
import { DesignerService } from './designer.service';
import { PackageCreationService } from '../package-creation/package-creation.service';
import { PackageCreationExtractionService } from '../package-creation/package-creation-extraction.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { ToastrService } from 'ngx-toastr';
import { ActivatedRoute, Router } from '@angular/router';
import { EMPTY } from 'rxjs';
import { DesignerDashboardState } from './model/designer.dashboard.state';
import { ActionElementTypeName } from 'src/app/common/constants/app-constants';

function buildComponent(): {
    component: DesignerComponent;
    designerStoreSpy: jasmine.SpyObj<DesignerStore>;
    graphUtilSpy: jasmine.SpyObj<GraphUtil>;
} {
    const mockState: DesignerDashboardState = {
        template: { workflows: {}, node_templates: {} } as any,
        sourceContent: null,
        actionName: '',
        functionName: ''
    };

    const designerStoreSpy = jasmine.createSpyObj<DesignerStore>('DesignerStore', [
        'deleteWorkflow', 'addDeclarativeWorkFlow', 'setCurrentAction',
        'setCurrentFunction', 'renameAction', 'saveSourceContent',
        'setInputsAndOutputsToSpecificWorkflow', 'addNodeTemplate',
        'addStepToDeclarativeWorkFlow', 'addDgGenericNodeTemplate',
        'addDgGenericDependency'
    ]);
    Object.defineProperty(designerStoreSpy, 'state$', { get: () => EMPTY });
    Object.defineProperty(designerStoreSpy, 'state', { get: () => mockState });

    const functionsStoreSpy = jasmine.createSpyObj<FunctionsStore>('FunctionsStore', ['retrieveFuntions']);
    Object.defineProperty(functionsStoreSpy, 'state$', { get: () => EMPTY });

    const packageCreationStoreSpy = jasmine.createSpyObj<PackageCreationStore>('PackageCreationStore', ['addTopologyTemplate']);
    Object.defineProperty(packageCreationStoreSpy, 'state$', { get: () => EMPTY });

    const activatedRouteMock = {
        snapshot: { paramMap: { get: (_: string) => null } },
        paramMap: EMPTY
    } as unknown as ActivatedRoute;

    const graphUtilSpy = jasmine.createSpyObj<GraphUtil>('GraphUtil', [
        'generateNewActionName', 'createCustomActionWithName', 'buildPaletteGraphFromList',
        'getFunctionTypeFromPaletteFunction', 'dropFunctionOverActionWithPosition',
        'getParent', 'canEmpedMoreChildern', 'isEmptyParent', 'getDgGenericChild',
        'getFunctionNameFromBoardFunction'
    ]);

    const component = new DesignerComponent(
        designerStoreSpy,
        functionsStoreSpy,
        packageCreationStoreSpy,
        jasmine.createSpyObj<PackageCreationUtils>('PackageCreationUtils', ['transformToJson']),
        graphUtilSpy,
        jasmine.createSpyObj<GraphGenerator>('GraphGenerator', ['clear', 'populate']),
        activatedRouteMock,
        jasmine.createSpyObj<Router>('Router', ['navigate']),
        jasmine.createSpyObj<DesignerService>('DesignerService', ['getPagedPackages', 'publishBlueprint']),
        jasmine.createSpyObj<PackageCreationService>('PackageCreationService', [
            'downloadPackage', 'savePackage', 'enrichAndDeployPackage', 'enrichPackage'
        ]),
        jasmine.createSpyObj<PackageCreationExtractionService>('PackageCreationExtractionService', ['extractBlobToStore']),
        activatedRouteMock,
        jasmine.createSpyObj<NgxUiLoaderService>('NgxUiLoaderService', ['start', 'stop']),
        jasmine.createSpyObj<ToastrService>('ToastrService', ['success', 'error'])
    );

    return { component, designerStoreSpy, graphUtilSpy };
}

describe('DesignerComponent', () => {
    it('should create', () => {
        const { component } = buildComponent();
        expect(component).toBeTruthy();
    });

    describe('deleteCurrentAction', () => {
        let component: DesignerComponent;
        let designerStoreSpy: jasmine.SpyObj<DesignerStore>;
        let mockActionCell: any;

        beforeEach(() => {
            ({ component, designerStoreSpy } = buildComponent());

            mockActionCell = {
                attributes: { type: ActionElementTypeName },
                attr: (key: string) => key === '#label/text' ? 'myAction' : undefined,
                remove: jasmine.createSpy('remove')
            };

            // Provide a minimal mock boardGraph
            component.boardGraph = { getCells: () => [mockActionCell] } as any;
            component.currentActionName = 'myAction';
            component.actions = ['myAction', 'otherAction'];
            component.actionAttributesSideBar = true;
            component.functionAttributeSidebar = false;
            component.steps = ['step1', 'step2'];
            component.elementPointerDownEvt = { clientX: 100, clientY: 200 };
        });

        it('should remove the matching action cell from the board graph', () => {
            component.deleteCurrentAction();
            expect(mockActionCell.remove).toHaveBeenCalled();
        });

        it('should call deleteWorkflow on the store with the action name', () => {
            component.deleteCurrentAction();
            expect(designerStoreSpy.deleteWorkflow).toHaveBeenCalledWith('myAction');
        });

        it('should remove the action from the actions array', () => {
            component.deleteCurrentAction();
            expect(component.actions).not.toContain('myAction');
            expect(component.actions).toContain('otherAction');
        });

        it('should close the action attributes sidebar', () => {
            component.deleteCurrentAction();
            expect(component.actionAttributesSideBar).toBe(false);
        });

        it('should close the function attributes sidebar', () => {
            component.functionAttributeSidebar = true;
            component.deleteCurrentAction();
            expect(component.functionAttributeSidebar).toBe(false);
        });

        it('should reset currentActionName to empty string', () => {
            component.deleteCurrentAction();
            expect(component.currentActionName).toBe('');
        });

        it('should clear steps so the left-panel sub-list no longer renders', () => {
            component.deleteCurrentAction();
            expect(component.steps).toEqual([]);
        });

        it('should clear elementPointerDownEvt to prevent spurious function-pane openings', () => {
            component.deleteCurrentAction();
            expect(component.elementPointerDownEvt).toBeNull();
        });

        it('should close sidebars before calling deleteWorkflow so cleanup survives store emission errors', () => {
            // deleteWorkflow is called last - both sidebars must be false by the time it runs
            let sidebarStateAtStoreCall: boolean;
            designerStoreSpy.deleteWorkflow.and.callFake(() => {
                sidebarStateAtStoreCall = component.actionAttributesSideBar;
            });
            component.deleteCurrentAction();
            expect(sidebarStateAtStoreCall).toBe(false);
        });

        it('should not remove unrelated cells', () => {
            const otherCell = {
                attributes: { type: ActionElementTypeName },
                attr: (key: string) => key === '#label/text' ? 'otherAction' : undefined,
                remove: jasmine.createSpy('remove')
            };
            component.boardGraph = { getCells: () => [mockActionCell, otherCell] } as any;

            component.deleteCurrentAction();

            expect(mockActionCell.remove).toHaveBeenCalled();
            expect(otherCell.remove).not.toHaveBeenCalled();
        });

        it('should not throw when no matching cell exists in the graph', () => {
            component.boardGraph = { getCells: () => [] } as any;
            expect(() => component.deleteCurrentAction()).not.toThrow();
            expect(designerStoreSpy.deleteWorkflow).toHaveBeenCalledWith('myAction');
        });
    });

    describe('insertCustomActionIntoBoard', () => {
        let component: DesignerComponent;
        let designerStoreSpy: jasmine.SpyObj<DesignerStore>;
        let graphUtilSpy: jasmine.SpyObj<GraphUtil>;

        beforeEach(() => {
            ({ component, designerStoreSpy, graphUtilSpy } = buildComponent());
            component.boardGraph = {} as any;
            component.actions = [];
        });

        it('should create Action1 when no existing ActionN exists', () => {
            component.actions = ['resource-resolution'];

            component.insertCustomActionIntoBoard();

            expect(graphUtilSpy.createCustomActionWithName).toHaveBeenCalledWith('Action1', component.boardGraph as any);
            expect(designerStoreSpy.addDeclarativeWorkFlow).toHaveBeenCalledWith('Action1');
            expect(component.actions).toContain('Action1');
        });

        it('should reuse Action1 after it was deleted', () => {
            component.actions = [];

            component.insertCustomActionIntoBoard();

            expect(graphUtilSpy.createCustomActionWithName).toHaveBeenCalledWith('Action1', component.boardGraph as any);
            expect(designerStoreSpy.addDeclarativeWorkFlow).toHaveBeenCalledWith('Action1');
            expect(component.actions).toContain('Action1');
        });

        it('should pick the first available ActionN gap', () => {
            component.actions = ['Action1', 'Action3'];

            component.insertCustomActionIntoBoard();

            expect(graphUtilSpy.createCustomActionWithName).toHaveBeenCalledWith('Action2', component.boardGraph as any);
            expect(designerStoreSpy.addDeclarativeWorkFlow).toHaveBeenCalledWith('Action2');
            expect(component.actions).toContain('Action2');
        });
    });
});
