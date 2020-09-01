import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import {PackageCreationStore} from '../package-creation/package-creation.store';
import {FilesContent, FolderNodeElement, MetaDataTabModel} from '../package-creation/mapping-models/metadata/MetaDataTab.model';
import {MetadataTabComponent} from '../package-creation/metadata-tab/metadata-tab.component';
import * as JSZip from 'jszip';
import {ConfigurationDashboardService} from './configuration-dashboard.service';
import {TemplateTopology, VlbDefinition} from '../package-creation/mapping-models/definitions/VlbDefinition';
import {DslDefinition} from '../package-creation/mapping-models/CBAPacakge.model';
import {PackageCreationUtils} from '../package-creation/package-creation.utils';
import {PackageCreationModes} from '../package-creation/creationModes/PackageCreationModes';
import {PackageCreationBuilder} from '../package-creation/creationModes/PackageCreationBuilder';
import {saveAs} from 'file-saver';
import {DesignerStore} from '../designer/designer.store';
import {ToastrService} from 'ngx-toastr';
import {NgxFileDropEntry} from 'ngx-file-drop';

@Component({
    selector: 'app-configuration-dashboard',
    templateUrl: './configuration-dashboard.component.html',
    styleUrls: ['./configuration-dashboard.component.css'],
})
export class ConfigurationDashboardComponent implements OnInit {
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

    constructor(
        private route: ActivatedRoute,
        private configurationDashboardService: ConfigurationDashboardService,
        private packageCreationStore: PackageCreationStore,
        private packageCreationUtils: PackageCreationUtils,
        private router: Router,
        private designerStore: DesignerStore,
        private toastService: ToastrService
    ) {
    }

    ngOnInit() {
        this.vlbDefinition.topology_template = new TemplateTopology();

        this.elementRef.nativeElement.focus();
        this.refreshCurrentPackage();
        const regexp = RegExp(this.versionPattern);
        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                if (cbaPackage && cbaPackage.metaData && cbaPackage.metaData.description
                    && cbaPackage.metaData.name && cbaPackage.metaData.version &&
                    regexp.test(cbaPackage.metaData.version)) {
                    if (!this.metadataClasses.includes('complete')) {
                        this.metadataClasses += ' complete';
                    }
                } else {
                    this.metadataClasses = this.metadataClasses.replace('complete', '');
                }

            });
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
            this.zipFile.loadAsync(blob).then((zip) => {
                Object.keys(zip.files).forEach((filename) => {
                    zip.files[filename].async('string').then((fileData) => {
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
                                this.setImports(filename, fileData, bluePrintDetailModels);
                            } else if (filename.includes('TOSCA-Metadata/')) {
                                const metaDataTabInfo: MetaDataTabModel = this.getMetaDataTabInfo(fileData);
                                this.setMetaData(metaDataTabInfo, bluePrintDetailModels[0]);
                            }
                        }
                    });
                });
            });
        });
    }

    setScripts(filename: string, fileData: any) {
        this.packageCreationStore.addScripts(filename, fileData);
    }

    setImports(filename: string, fileData: any, bluePrintDetailModels: BluePrintDetailModel) {
        if (filename.includes(bluePrintDetailModels[0].artifactName)) {
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
        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                FilesContent.clear();
                let packageCreationModes: PackageCreationModes;
                cbaPackage = PackageCreationModes.mapModeType(cbaPackage);
                cbaPackage.metaData = PackageCreationModes.setEntryPoint(cbaPackage.metaData);
                packageCreationModes = PackageCreationBuilder.getCreationMode(cbaPackage);
                packageCreationModes.execute(cbaPackage, this.packageCreationUtils);
                this.filesData.push(this.folder.TREE_DATA);
                this.saveBluePrintToDataBase();
            });
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
                this.packageCreationStore.saveBluePrint(blob).subscribe(
                    bluePrintDetailModels => {
                        if (bluePrintDetailModels) {
                            const id = bluePrintDetailModels.toString().split('id')[1].split(':')[1].split('"')[1];
                            this.toastService.info('package updated successfully ');
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
        console.log('happened');
        this.router.navigate(['/packages']);
    }

    goToDesignerMode(id) {
        //  this.designerService.setActionName(this.customActionName);
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

        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                FilesContent.clear();
                console.log(cbaPackage);

                let packageCreationModes: PackageCreationModes;
                cbaPackage = PackageCreationModes.mapModeType(cbaPackage);
                cbaPackage.metaData = PackageCreationModes.setEntryPoint(cbaPackage.metaData);
                packageCreationModes = PackageCreationBuilder.getCreationMode(cbaPackage);
                packageCreationModes.execute(cbaPackage, this.packageCreationUtils);
                this.filesData.push(this.folder.TREE_DATA);
                this.enrichPackage();

            });
    }

    private enrichPackage() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.packageCreationStore.enrichBluePrint(blob).subscribe(response => {
                    console.log('success');
                    const blobInfo = new Blob([response], {type: 'application/octet-stream'});
                    saveAs(blobInfo, 'test' + '-' + '1.0.0' + '-CBA.zip');
                    this.toastService.info('enriched successfully ');
                });
            }, error => {
                this.toastService.error('error happened when editing ' + error.message);
                console.log('Error -' + error.message);
            });
    }

    clickEvent() {
        this.isSaveEnabled = true;
    }
}
