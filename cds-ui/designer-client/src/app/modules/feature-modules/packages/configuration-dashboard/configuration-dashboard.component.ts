import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BluePrintDetailModel } from '../model/BluePrint.detail.model';
import { PackageCreationStore } from '../package-creation/package-creation.store';
import { FilesContent, FolderNodeElement } from '../package-creation/mapping-models/metadata/MetaDataTab.model';
import { MetadataTabComponent } from '../package-creation/metadata-tab/metadata-tab.component';
import * as JSZip from 'jszip';
import { ConfigurationDashboardService } from './configuration-dashboard.service';
import { TemplateTopology, CBADefinition } from '../package-creation/mapping-models/definitions/CBADefinition';
import { CBAPackage } from '../package-creation/mapping-models/CBAPacakge.model';
import { PackageCreationUtils } from '../package-creation/package-creation.utils';
import { PackageCreationModes } from '../package-creation/creationModes/PackageCreationModes';
import { PackageCreationBuilder } from '../package-creation/creationModes/PackageCreationBuilder';
import { saveAs } from 'file-saver';
import { DesignerStore } from '../designer/designer.store';
import { ToastrService } from 'ngx-toastr';
import { NgxFileDropEntry } from 'ngx-file-drop';
import { PackageCreationService } from '../package-creation/package-creation.service';
import { ComponentCanDeactivate } from '../../../../common/core/canDactivate/ComponentCanDeactivate';
import { PackageCreationExtractionService } from '../package-creation/package-creation-extraction.service';
import { distinctUntilChanged, takeUntil } from 'rxjs/operators';
import { Subject, throwError } from 'rxjs';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
    selector: 'app-configuration-dashboard',
    templateUrl: './configuration-dashboard.component.html',
    styleUrls: ['./configuration-dashboard.component.css'],
})
export class ConfigurationDashboardComponent extends ComponentCanDeactivate implements OnInit, OnDestroy {
    viewedPackage: BluePrintDetailModel = new BluePrintDetailModel();
    @ViewChild(MetadataTabComponent, { static: false })
    metadataTabComponent: MetadataTabComponent;
    public customActionName = '';

    entryDefinitionKeys: string[] = ['template_tags', 'user-groups',
        'author-email', 'template_version', 'template_name', 'template_author', 'template_description'];
    @ViewChild('nameit', { static: true })
    elementRef: ElementRef;
    uploadedFiles = [];
    zipFile: JSZip = new JSZip();
    filesData: any = [];
    folder: FolderNodeElement = new FolderNodeElement();
    id: any;

    currentBlob = new Blob();
    vlbDefinition: CBADefinition = new CBADefinition();
    isSaveEnabled = false;
    versionPattern = '^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$';
    metadataClasses = 'nav-item nav-link active';
    private cbaPackage: CBAPackage = new CBAPackage();
    dataTarget: any = '';
    ngUnsubscribe = new Subject();
    private designerState: any;

    constructor(
        private route: ActivatedRoute,
        private configurationDashboardService: ConfigurationDashboardService,
        private packageCreationStore: PackageCreationStore,
        private packageCreationService: PackageCreationService,
        private packageCreationUtils: PackageCreationUtils,
        private router: Router,
        private designerStore: DesignerStore,
        private toastService: ToastrService,
        private ngxService: NgxUiLoaderService,
        private packageCreationExtractionService: PackageCreationExtractionService
    ) {
        super();


    }

    ngOnInit() {
        this.ngxService.start();
        this.vlbDefinition.topology_template = new TemplateTopology();
        this.packageCreationStore.state$
            .pipe(distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
                takeUntil(this.ngUnsubscribe))
            .subscribe(
                cbaPackage => {
                    this.cbaPackage = cbaPackage;
                });
        this.designerStore.state$.pipe(
            distinctUntilChanged((a: any, b: any) => JSON.stringify(a) === JSON.stringify(b)),
            takeUntil(this.ngUnsubscribe))
            .subscribe(state => {
                this.designerState = state;
                this.vlbDefinition.topology_template.content = this.packageCreationUtils.transformToJson(state.template);
            });
        this.elementRef.nativeElement.focus();
        this.refreshCurrentPackage();
        const regexp = RegExp(this.versionPattern);
        if (this.cbaPackage && this.cbaPackage.metaData && this.cbaPackage.metaData.description
            && this.cbaPackage.metaData.name && this.cbaPackage.metaData.version &&
            regexp.test(this.cbaPackage.metaData.version)) {
            if (!this.metadataClasses.includes('complete')) {
                this.metadataClasses += ' complete';
            }
        } else {
            this.metadataClasses = this.metadataClasses.replace('complete', '');
            this.isSaveEnabled = false;
        }


    }

    private refreshCurrentPackage(id?) {
        this.id = this.route.snapshot.paramMap.get('id');
        console.log(this.id);
        id = id ? id : this.id;
        this.configurationDashboardService.getPagedPackages(id).subscribe(
            (bluePrintDetailModels) => {
                if (bluePrintDetailModels) {
                    this.viewedPackage = bluePrintDetailModels[0];
                    this.downloadCBAPackage(bluePrintDetailModels);
                    this.packageCreationStore.clear();
                }
            }, err => { },
            () => {
                //  this.ngxService.stop();
            });
    }

    private downloadCBAPackage(bluePrintDetailModels: BluePrintDetailModel) {
        this.configurationDashboardService.downloadResource(
            this.viewedPackage.artifactName + '/' + this.viewedPackage.artifactVersion).subscribe(response => {
                const blob = new Blob([response], { type: 'application/octet-stream' });
                this.currentBlob = blob;
                this.packageCreationExtractionService.extractBlobToStore(blob);
            }, err => { },
                () => {
                    this.ngxService.stop();
                });
    }

    editBluePrint() {
        this.ngxService.start();
        this.configurationDashboardService.deletePackage(this.viewedPackage.id).subscribe(res => {
            this.formTreeData();
            this.saveBluePrintToDataBase();

        });
    }

    private formTreeData() {
        FilesContent.clear();
        let packageCreationModes: PackageCreationModes;
        this.cbaPackage = PackageCreationModes.mapModeType(this.cbaPackage);
        this.cbaPackage.metaData = PackageCreationModes.setEntryPoint(this.cbaPackage.metaData);
        packageCreationModes = PackageCreationBuilder.getCreationMode(this.cbaPackage);
        packageCreationModes.execute(this.cbaPackage, this.packageCreationUtils);
        this.filesData.push(this.folder.TREE_DATA);
    }

    saveMetaData() {
        this.metadataTabComponent.saveMetaDataToStore();
    }

    saveBluePrintToDataBase() {
        this.create();
        this.zipFile.generateAsync({ type: 'blob' })
            .then(blob => {
                this.packageCreationService.savePackage(blob).subscribe(
                    bluePrintDetailModels => {
                        if (bluePrintDetailModels) {
                            const id = bluePrintDetailModels.toString().split('id')[1].split(':')[1].split('"')[1];
                            this.toastService.info('package updated successfully ');
                            this.isSaveEnabled = false;
                            this.router.navigate(['/packages/package/' + id]);
                            this.refreshCurrentPackage(id);
                        }
                    }, error => {
                        this.toastService.error('error happened when editing ' + error.message);
                        console.log('Error -' + error.message);
                    }, () => {
                        this.ngxService.stop();
                    });
            });
    }

    deletePackage() {
        this.configurationDashboardService.deletePackage(this.id).subscribe(res => {
            console.log('Deleted');
            console.log(res);
            this.isSaveEnabled = false;
            this.router.navigate(['/packages']);
        }, err => {
            console.log(err);
        });
    }

    create() {
        this.zipFile = new JSZip();
        FilesContent.getMapOfFilesNamesAndContent().forEach((value, key) => {
            this.zipFile.folder(key.split('/')[0]);
            this.zipFile.file(key, value);
        });

    }

    discardChanges() {
        this.refreshCurrentPackage();
    }

    downloadPackage(artifactName: string, artifactVersion: string) {
        this.ngxService.start();
        this.configurationDashboardService.downloadResource(artifactName + '/' + artifactVersion).subscribe(response => {
            const blob = new Blob([response], { type: 'application/octet-stream' });
            saveAs(blob, artifactName + '-' + artifactVersion + '-CBA.zip');

        }, err => { }, () => {
            this.ngxService.stop();
        });
    }

    deployCurrentPackage() {
        this.ngxService.start();
        this.formTreeData();
        this.deployPackage();

    }

    goToDesignerMode(id) {
        this.router.navigate(['/packages/designer', id, { actionName: this.customActionName }]);
    }

    public dropped(files: NgxFileDropEntry[]) {

    }

    public fileOver(event) {
        console.log(event);
    }

    public fileLeave(event) {
        console.log(event);
    }

    textChanged($event: {}) {
        this.cbaPackage.templateTopology.node_templates = this.designerState.template.node_templates;
        this.cbaPackage.templateTopology.workflows = this.designerState.template.workflows;
        this.cbaPackage.templateTopology.content = this.vlbDefinition.topology_template.content;
    }

    enrichBluePrint() {
        this.ngxService.start();
        this.packageCreationStore.addTopologyTemplate(this.cbaPackage.templateTopology);
        this.formTreeData();
        this.enrichPackage();
        this.designerStore.clear();
        this.packageCreationStore.clear();
    }


    private enrichPackage() {
        this.create();
        this.zipFile.generateAsync({ type: 'blob' })
            .then(blob => {
                this.packageCreationService.enrichPackage(blob).subscribe(response => {
                    console.log('success');
                    const blobInfo = new Blob([response], { type: 'application/octet-stream' });
                    this.currentBlob = blobInfo;
                    this.packageCreationStore.clear();
                    this.packageCreationExtractionService.extractBlobToStore(this.currentBlob);
                    this.isSaveEnabled = true;
                    this.toastService.success('Enriched done successfully');
                }, err => {
                    this.handleError(err);
                }, () => {
                    this.ngxService.stop();
                });
            }, error => {
                this.toastService.error('Error occurs during enrichment process' + error.message);
                console.error('Error -' + error.message);
            }, () => {
                this.ngxService.stop();
            });
    }

    private deployPackage() {
        this.create();
        this.zipFile.generateAsync({ type: 'blob' })
            .then(blob => {
                this.packageCreationService.deploy(blob).subscribe(response => {
                    this.toastService.info('deployed successfully ');
                    const id = response.toString().split('id')[1].split(':')[1].split('"')[1];
                    this.isSaveEnabled = false;
                    this.router.navigate(['/packages/package/' + id]);
                }, err => {
                    this.handleError(err);
                }, () => {
                    this.ngxService.stop();
                });
            }, error => {
                this.handleError(error);
            }, () => {
                this.ngxService.stop();
            });
    }

    clickEvent() {
        this.isSaveEnabled = true;
    }

    canDeactivate(): boolean {
        return this.isSaveEnabled;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    checkSkipTypesOfAction() {
        console.log(this.cbaPackage);
        if (this.cbaPackage.templateTopology && this.cbaPackage.templateTopology.node_templates
            && this.cbaPackage.templateTopology.workflows) {
            this.goToDesignerMode(this.id);
        } else {
            this.dataTarget = '#exampleModalLong';
        }
    }

    handleError(error) {
        let errorMessage = '';
        if (error.error instanceof ErrorEvent) {
            // client-side error
            errorMessage = `Error: ${error.error.message}`;
        } else {
            // server-side error
            errorMessage = `Error Code: ${error.status}\nMessage: ${error.message}`;
        }
        this.toastService.error('error happened when deploying ' + errorMessage);
        console.log('Error -' + errorMessage);
        this.ngxService.stop();
        this.toastService.error('error happened when deploying' + error.message);
        return throwError(errorMessage);
    }
}

