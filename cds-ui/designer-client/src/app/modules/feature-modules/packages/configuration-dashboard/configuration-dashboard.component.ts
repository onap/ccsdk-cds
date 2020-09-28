import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import {PackageCreationStore} from '../package-creation/package-creation.store';
import {FilesContent, FolderNodeElement, MetaDataTabModel} from '../package-creation/mapping-models/metadata/MetaDataTab.model';
import {MetadataTabComponent} from '../package-creation/metadata-tab/metadata-tab.component';
import * as JSZip from 'jszip';
import {ConfigurationDashboardService} from './configuration-dashboard.service';
import {TemplateTopology, VlbDefinition} from '../package-creation/mapping-models/definitions/VlbDefinition';
import {CBAPackage} from '../package-creation/mapping-models/CBAPacakge.model';
import {PackageCreationUtils} from '../package-creation/package-creation.utils';
import {PackageCreationModes} from '../package-creation/creationModes/PackageCreationModes';
import {PackageCreationBuilder} from '../package-creation/creationModes/PackageCreationBuilder';
import {saveAs} from 'file-saver';
import {DesignerStore} from '../designer/designer.store';
import {ToastrService} from 'ngx-toastr';
import {NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationService} from '../package-creation/package-creation.service';
import {ComponentCanDeactivate} from '../../../../common/core/canDactivate/ComponentCanDeactivate';
import {PackageCreationExtractionService} from '../package-creation/package-creation-extraction.service';

@Component({
    selector: 'app-configuration-dashboard',
    templateUrl: './configuration-dashboard.component.html',
    styleUrls: ['./configuration-dashboard.component.css'],
})
export class ConfigurationDashboardComponent extends ComponentCanDeactivate implements OnInit {
    viewedPackage: BluePrintDetailModel = new BluePrintDetailModel();
    @ViewChild(MetadataTabComponent, {static: false})
    metadataTabComponent: MetadataTabComponent;
    public customActionName = '';

    entryDefinitionKeys: string[] = ['template_tags', 'user-groups',
        'author-email', 'template_version', 'template_name', 'template_author', 'template_description'];
    @ViewChild('nameit', {static: true})
    elementRef: ElementRef;
    uploadedFiles = [];
    zipFile: JSZip = new JSZip();
    filesData: any = [];
    folder: FolderNodeElement = new FolderNodeElement();
    id: any;

    currentBlob = new Blob();
    vlbDefinition: VlbDefinition = new VlbDefinition();
    isSaveEnabled = false;
    versionPattern = '^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$';
    metadataClasses = 'nav-item nav-link active';
    private cbaPackage: CBAPackage = new CBAPackage();
    dataTarget: any = '';

    constructor(
        private route: ActivatedRoute,
        private configurationDashboardService: ConfigurationDashboardService,
        private packageCreationStore: PackageCreationStore,
        private packageCreationService: PackageCreationService,
        private packageCreationUtils: PackageCreationUtils,
        private router: Router,
        private designerStore: DesignerStore,
        private toastService: ToastrService,
        private packageCreationExtractionService: PackageCreationExtractionService
    ) {
        super();

        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                this.cbaPackage = cbaPackage;
            });


    }

    ngOnInit() {
        this.vlbDefinition.topology_template = new TemplateTopology();

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
            });
    }

    private downloadCBAPackage(bluePrintDetailModels: BluePrintDetailModel) {
        this.configurationDashboardService.downloadResource(
            this.viewedPackage.artifactName + '/' + this.viewedPackage.artifactVersion).subscribe(response => {
            const blob = new Blob([response], {type: 'application/octet-stream'});
            this.currentBlob = blob;
            this.packageCreationExtractionService.extractBlobToStore(blob);
        });
    }

    editBluePrint() {
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
        this.zipFile.generateAsync({type: 'blob'})
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
        this.configurationDashboardService.downloadResource(artifactName + '/' + artifactVersion).subscribe(response => {
            const blob = new Blob([response], {type: 'application/octet-stream'});
            saveAs(blob, artifactName + '-' + artifactVersion + '-CBA.zip');
        });
    }

    deployCurrentPackage() {
        this.formTreeData();
        this.deployPackage();

    }

    goToDesignerMode(id) {
        this.router.navigate(['/packages/designer', id, {actionName: this.customActionName}]);
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
        this.packageCreationStore.addTopologyTemplate(this.vlbDefinition.topology_template);
    }

    enrichBluePrint() {

        this.formTreeData();
        this.enrichPackage();
    }


    private enrichPackage() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.packageCreationService.enrichPackage(blob).subscribe(response => {
                    console.log('success');
                    const blobInfo = new Blob([response], {type: 'application/octet-stream'});
                    this.currentBlob = blobInfo;
                    this.packageCreationStore.clear();
                    this.packageCreationExtractionService.extractBlobToStore(this.currentBlob);
                    this.isSaveEnabled = true;
                    this.toastService.info('enriched successfully ');
                });
            }, error => {
                this.toastService.error('error happened when enrich ' + error.message);
                console.error('Error -' + error.message);
            });
    }

    private deployPackage() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.packageCreationService.deploy(blob).subscribe(response => {
                    this.toastService.info('deployed successfully ');
                    const id = response.toString().split('id')[1].split(':')[1].split('"')[1];
                    this.isSaveEnabled = false;
                    this.router.navigate(['/packages/package/' + id]);
                });
            }, error => {
                this.toastService.error('error happened when deploying ' + error.message);
                console.log('Error -' + error.message);
            });
    }

    clickEvent() {
        this.isSaveEnabled = true;
    }

    canDeactivate(): boolean {
        return this.isSaveEnabled;
    }

    checkSkipTypesOfAction() {
        if (this.cbaPackage.templateTopology.node_templates && this.cbaPackage.templateTopology.workflows) {
            this.goToDesignerMode(this.id);
        } else {
            this.dataTarget = '#exampleModalLong';
        }
    }
}
