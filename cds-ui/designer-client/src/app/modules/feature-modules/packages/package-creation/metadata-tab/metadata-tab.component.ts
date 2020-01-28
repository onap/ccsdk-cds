import {Component, OnInit} from '@angular/core';
import {PackageCreationService} from '../package-creation.service';
import {PackageCreationUtils} from '../package-creation.utils';
import {Router} from '@angular/router';
import {FilesContent, FolderNodeElement, MetaDataFile, MetaDataTabModel} from '../mapping-models/metadata/MetaDataTab.model';
import * as JSZip from 'jszip';
import {PackageCreationStore} from '../package-creation.store';


@Component({
    selector: 'app-metadata-tab',
    templateUrl: './metadata-tab.component.html',
    styleUrls: ['./metadata-tab.component.css']
})
export class MetadataTabComponent implements OnInit {

    counter = 0;
    modes: object[] = [
        {name: 'Designer Mode', style: 'mode-icon icon-designer-mode'},
        {name: 'Scripting Mode', style: 'mode-icon icon-scripting-mode'}];
    private metaDataTab: MetaDataTabModel = new MetaDataTabModel();

    private folder: FolderNodeElement = new FolderNodeElement();
    private zipFile: JSZip = new JSZip();
    private filesData: any = [];
    private errorMessage: string;

    constructor(private packageCreationService: PackageCreationService, private packageCreationUtils: PackageCreationUtils,
                private router: Router, private packageCreationStore: PackageCreationStore) {

    }

    ngOnInit() {
        this.packageCreationStore.changeMetaData(this.metaDataTab);
    }

    saveMetaData() {
        this.setModeType(this.metaDataTab);
        this.setEntryPoint(this.metaDataTab);

        this.addToscaMetaDataFile(this.metaDataTab);

        // const vlbDefinition: VlbDefinition = new VlbDefinition();
        // this.fillVLBDefinition(vlbDefinition, this.metaDataTab);

        this.filesData.push(this.folder.TREE_DATA);
        this.saveBluePrint();
        this.packageCreationService.refreshPackages();
        this.router.navigate(['/packages']);

    }

    addToscaMetaDataFile(metaDataTab: MetaDataTabModel) {
        const filename = 'TOSCA.meta';
        FilesContent.putData(filename, MetaDataFile.getObjectInstance(this.metaDataTab));
    }

    private setModeType(metaDataTab: MetaDataTabModel) {
        if (metaDataTab.mode.startsWith('Scripting')) {
            metaDataTab.mode = 'KOTLIN_SCRIPT';
        } else if (metaDataTab.mode.startsWith('Designer')) {
            metaDataTab.mode = 'DEFAULT';
        } else {
            metaDataTab.mode = 'GENERIC_SCRIPT';
        }
    }

    saveBluePrint() {
        this.create();
        this.zipFile.generateAsync({type: 'blob'})
            .then(blob => {
                this.packageCreationService.savePackage(blob);

            });
    }


    create() {
        this.folder.TREE_DATA.forEach((path) => {

            const name = path.name;
            if (path.children) {
                this.zipFile.folder(name);
                path.children.forEach(children => {
                    const name2 = children.name;
                    if (FilesContent.getMapOfFilesNamesAndContent().has(name2)) {
                        this.zipFile.file(name + '/' + name2, FilesContent.getMapOfFilesNamesAndContent().get(name2));
                    } else {
                    }

                });

            }
        });
    }

    private setEntryPoint(metaDataTab: MetaDataTabModel) {
        if (metaDataTab.mode.startsWith('DEFAULT')) {
            metaDataTab.entryFileName = 'Definitions/vLB_CDS.json';
        } else {
            metaDataTab.entryFileName = '';
        }


    }

    /* private fillVLBDefinition(vlbDefinition: VlbDefinition, metaDataTab: MetaDataTabModel) {

         const metadata: Metadata = new Metadata();
         metadata.template_author = 'Shaaban';
         metadata.template_name = metaDataTab.templateName;
         metadata.template_tags = metaDataTab.tags;

         metadata.dictionary_group = 'default';
         metadata.template_version = metaDataTab.version;
         metadata['author-email'] = 'shaaban.altanany.ext@orange.com';
         metadata['user-groups'] = 'ADMIN';
         vlbDefinition.tosca_definitions_version = metaDataTab.version;
         vlbDefinition.metadata = metadata;
         const value = this.packageCreationUtils.transformToJson(vlbDefinition);
         console.log(value);
         FilesContent.putData('vLB_CDS.json', value);
     }*/

    validatePackageNameAndVersion() {
        if (this.metaDataTab.name && this.metaDataTab.version) {
            this.packageCreationService.checkBluePrintNameAndVersion(this.metaDataTab.name, this.metaDataTab.version).then(element => {
                if (element) {
                    this.errorMessage = 'the package with name and version is exists';
                } else {
                    this.errorMessage = ' ';
                }
            });
        }

    }
}
