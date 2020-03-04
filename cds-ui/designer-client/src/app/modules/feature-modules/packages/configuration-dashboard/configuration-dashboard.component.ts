import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import {PackageCreationStore} from '../package-creation/package-creation.store';
import {FilesContent, FolderNodeElement, MetaDataTabModel} from '../package-creation/mapping-models/metadata/MetaDataTab.model';
import {MetadataTabComponent} from '../package-creation/metadata-tab/metadata-tab.component';
import * as JSZip from 'jszip';
import {ConfigurationDashboardService} from './configuration-dashboard.service';
import {VlbDefinition} from '../package-creation/mapping-models/definitions/VlbDefinition';
import {DslDefinition} from '../package-creation/mapping-models/CBAPacakge.model';
import {PackageCreationUtils} from '../package-creation/package-creation.utils';
import {PackageCreationModes} from '../package-creation/creationModes/PackageCreationModes';
import {PackageCreationBuilder} from '../package-creation/creationModes/PackageCreationBuilder';
import {saveAs} from 'file-saver';
import {DesignerStore} from '../designer/designer.store';

@Component({
    selector: 'app-configuration-dashboard',
    templateUrl: './configuration-dashboard.component.html',
    styleUrls: ['./configuration-dashboard.component.css']
})
export class ConfigurationDashboardComponent implements OnInit {
    viewedPackage: BluePrintDetailModel = new BluePrintDetailModel();
    @ViewChild(MetadataTabComponent, {static: false})
    private metadataTabComponent: MetadataTabComponent;

    entryDefinitionKeys: string[] = ['template_tags', 'user-groups',
        'author-email', 'template_version', 'template_name', 'template_author'];
    @ViewChild('nameit', {static: true})
    private elementRef: ElementRef;

    private zipFile: JSZip = new JSZip();
    private filesData: any = [];
    private folder: FolderNodeElement = new FolderNodeElement();

    private currentBlob = new Blob();

    constructor(private route: ActivatedRoute, private configurationDashboardService: ConfigurationDashboardService,
                private packageCreationStore: PackageCreationStore,
                private packageCreationUtils: PackageCreationUtils,
                private router: Router,
                private designerStore: DesignerStore) {
    }

    ngOnInit() {
        this.elementRef.nativeElement.focus();
        const id = this.route.snapshot.paramMap.get('id');
        this.configurationDashboardService.getPagedPackages(id).subscribe(
            (bluePrintDetailModels) => {
                if (bluePrintDetailModels) {
                    this.viewedPackage = bluePrintDetailModels[0];
                    this.downloadCBAPackage(bluePrintDetailModels);
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
                    console.log(filename);
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
                                this.setImports(filename, fileData);
                            } else if (filename.includes('TOSCA-Metadata/')) {
                                const metaDataTabInfo: MetaDataTabModel = this.getMetaDataTabInfo(fileData);
                                // console.log(metaDataTabInfo);
                                this.setMetaData(metaDataTabInfo, bluePrintDetailModels[0]);
                            }
                        }
                    });
                });
            });
        });
    }

    private setScripts(filename: string, fileData: any) {
        this.packageCreationStore.addScripts(filename, fileData);
    }

    private setImports(filename: string, fileData: any) {
        if (filename.includes('blueprint.json') || filename.includes('vLB_CDS.json')) {
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
            // console.log(definition.topology_template.content);
            if (definition.topology_template.content) {
                this.designerStore.saveSourceContent(definition.topology_template.content);
            }
        } else {
            this.packageCreationStore.addDefinition(filename, fileData);

        }
    }

    private setTemplates(filename: string, fileData: any) {
        this.packageCreationStore.addTemplate(filename, fileData);
    }

    private setMapping(fileName: string, fileData: string) {
        this.packageCreationStore.addMapping(fileName, fileData);
    }

    editBluePrint() {
        this.packageCreationStore.state$.subscribe(
            cbaPackage => {
                console.log(cbaPackage);
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

    private setMetaData(metaDataObject: MetaDataTabModel, bluePrintDetailModel: BluePrintDetailModel) {
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
        console.log(metaDataTabModel.mode);
        return metaDataTabModel;
    }

    saveBluePrintToDataBase() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.packageCreationStore.saveBluePrint(blob);
                this.router.navigate(['/packages']);
            });
    }


    create() {
        FilesContent.getMapOfFilesNamesAndContent().forEach((value, key) => {
            this.zipFile.folder(key.split('/')[0]);
            this.zipFile.file(key, value);
        });

    }

    goBacktoDashboard() {
        this.router.navigate(['/packages']);
    }

    downloadPackage(artifactName: string, artifactVersion: string) {
        this.configurationDashboardService.downloadResource(artifactName + '/' + artifactVersion).subscribe(response => {
            const blob = new Blob([response], {type: 'application/octet-stream'});
            saveAs(blob, artifactName + '-' + artifactVersion + '-CBA.zip');
        });
    }

    deployCurrentPackage() {
        console.log('happened');
        /*   this.zipFile.generateAsync({type: 'blob'})
               .then(blob => {
                   const formData = new FormData();
                   formData.append('file', this.currentBlob);
                   this.configurationDashboardService.deployPost(formData)
                       .subscribe(data => {
                       }, error => {
                       });
                   this.router.navigate(['/packages']);
               });
   */
        this.router.navigate(['/packages']);
    }

    goToDesignerMode() {
        this.router.navigate(['/packages/designer']);
    }
}
