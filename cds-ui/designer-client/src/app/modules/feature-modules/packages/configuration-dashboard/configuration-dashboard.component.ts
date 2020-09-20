import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import {PackageCreationStore} from '../package-creation/package-creation.store';
import {FilesContent, FolderNodeElement, MetaDataTabModel} from '../package-creation/mapping-models/metadata/MetaDataTab.model';
import {MetadataTabComponent} from '../package-creation/metadata-tab/metadata-tab.component';
import * as JSZip from 'jszip';
import {ConfigurationDashboardService} from './configuration-dashboard.service';
import {TemplateTopology, VlbDefinition} from '../package-creation/mapping-models/definitions/VlbDefinition';
import {CBAPackage, DslDefinition} from '../package-creation/mapping-models/CBAPacakge.model';
import {PackageCreationUtils} from '../package-creation/package-creation.utils';
import {PackageCreationModes} from '../package-creation/creationModes/PackageCreationModes';
import {PackageCreationBuilder} from '../package-creation/creationModes/PackageCreationBuilder';
import {saveAs} from 'file-saver';
import {DesignerStore} from '../designer/designer.store';
import {ToastrService} from 'ngx-toastr';
import {NgxFileDropEntry} from 'ngx-file-drop';
import {PackageCreationService} from '../package-creation/package-creation.service';
import {ComponentCanDeactivate} from '../../../../common/core/canDactivate/ComponentCanDeactivate';

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

    constructor(
        private route: ActivatedRoute,
        private configurationDashboardService: ConfigurationDashboardService,
        private packageCreationStore: PackageCreationStore,
        private packageCreationService: PackageCreationService,
        private packageCreationUtils: PackageCreationUtils,
        private router: Router,
        private designerStore: DesignerStore,
        private toastService: ToastrService
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

    private refreshCurrentPackage() {
        this.id = this.route.snapshot.paramMap.get('id');
        this.configurationDashboardService.getPagedPackages(this.id).subscribe(
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
            bluePrintDetailModels[0].artifactName + '/' + bluePrintDetailModels[0].artifactVersion).subscribe(response => {
            const blob = new Blob([response], {type: 'application/octet-stream'});
            this.currentBlob = blob;
            this.extractBlobToStore(blob, bluePrintDetailModels[0]);
        });
    }

    private extractBlobToStore(blob: Blob, bluePrintDetailModel: BluePrintDetailModel) {
        this.zipFile.loadAsync(blob).then((zip) => {
            Object.keys(zip.files).forEach((filename) => {
                zip.files[filename].async('string').then((fileData) => {
                    console.log(filename);
                    if (fileData) {
                        if (filename.includes('Scripts/')) {
                            this.setScripts(filename, fileData);
                        } else if (filename.includes('Templates/')) {
                            if (filename.includes('-mapping.')) {
                                this.setMapping(filename, fileData);
                            } else if (filename.includes('-template.')) {
                                this.setTemplates(filename, fileData);
                            }

                        } else if (filename.includes('Definitions/')) {
                            this.setImports(filename, fileData, bluePrintDetailModel);
                        } else if (filename.includes('TOSCA-Metadata/')) {
                            const metaDataTabInfo: MetaDataTabModel = this.getMetaDataTabInfo(fileData);
                            this.setMetaData(metaDataTabInfo, bluePrintDetailModel);
                        }
                    }
                });
            });
        });
    }

    setScripts(filename: string, fileData: any) {
        this.packageCreationStore.addScripts(filename, fileData);
    }

    setImports(filename: string, fileData: any, bluePrintDetailModels: BluePrintDetailModel) {
        console.log(filename);
        if (filename.includes(bluePrintDetailModels.artifactName)) {
            let definition = new VlbDefinition();
            definition = fileData as VlbDefinition;
            definition = JSON.parse(fileData);
            const dslDefinition = new DslDefinition();
            dslDefinition.content = this.packageCreationUtils.transformToJson(definition.dsl_definitions);
            const mapOfCustomKeys = new Map<string, string>();
            for (const metadataKey in definition.metadata) {
                if (!this.entryDefinitionKeys.includes(metadataKey + '')) {
                    mapOfCustomKeys.set(metadataKey + '', definition.metadata[metadataKey + '']);
                }
            }
            this.packageCreationStore.changeDslDefinition(dslDefinition);
            this.packageCreationStore.setCustomKeys(mapOfCustomKeys);
            if (definition.topology_template && definition.topology_template.content) {
                this.designerStore.saveSourceContent(definition.topology_template.content);
            }

        }
        this.packageCreationStore.addDefinition(filename, fileData);

    }

    setTemplates(filename: string, fileData: any) {
        this.packageCreationStore.addTemplate(filename, fileData);
    }

    setMapping(fileName: string, fileData: string) {
        this.packageCreationStore.addMapping(fileName, fileData);
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

    setMetaData(metaDataObject: MetaDataTabModel, bluePrintDetailModel: BluePrintDetailModel) {
        metaDataObject.description = bluePrintDetailModel.artifactDescription;
        this.packageCreationStore.changeMetaData(metaDataObject);

    }

    saveMetaData() {
        this.metadataTabComponent.saveMetaDataToStore();

    }

    getMetaDataTabInfo(fileData: string) {
        const metaDataTabModel = new MetaDataTabModel();
        const arrayOfLines = fileData.split('\n');
        metaDataTabModel.entryFileName = arrayOfLines[3].split(':')[1];
        metaDataTabModel.name = arrayOfLines[4].split(':')[1];
        metaDataTabModel.version = arrayOfLines[5].split(':')[1];
        metaDataTabModel.mode = arrayOfLines[6].split(':')[1];
        metaDataTabModel.templateTags = new Set<string>(arrayOfLines[7].split(':')[1].split(','));
        return metaDataTabModel;
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
                            this.id = id;
                            this.router.navigate(['/packages/package/' + id]);
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
        //  this.designerService.setActionName(this.customActionName);
        this.packageCreationStore.state$.subscribe(cba => {
            console.log(cba);
            sessionStorage.setItem('cba', this.packageCreationUtils.transformToJson(cba));
        });
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
                    this.extractBlobToStore(this.currentBlob, this.viewedPackage);
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

}
