import {Injectable, ViewChild} from '@angular/core';
import {MetaDataTabModel} from './mapping-models/metadata/MetaDataTab.model';
import {VlbDefinition} from './mapping-models/definitions/VlbDefinition';
import {DslDefinition} from './mapping-models/CBAPacakge.model';
import {PackageCreationStore} from './package-creation.store';
import * as JSZip from 'jszip';
import {PackageCreationUtils} from './package-creation.utils';
import {MetadataTabComponent} from './metadata-tab/metadata-tab.component';
import {DesignerStore} from '../designer/designer.store';

@Injectable({
    providedIn: 'root'
})
export class PackageCreationExtractionService {

    zipFile: JSZip = new JSZip();
    entryDefinitionKeys: string[] = ['template_tags', 'user-groups',
        'author-email', 'template_version', 'template_name', 'template_author', 'template_description'];
    @ViewChild(MetadataTabComponent, {static: false})
    metadataTabComponent: MetadataTabComponent;

    constructor(private packageCreationStore: PackageCreationStore,
                private packageCreationUtils: PackageCreationUtils,
                private designerStore: DesignerStore) {
    }

    public extractBlobToStore(blob) {
        let packageName = null;
        this.zipFile.loadAsync(blob).then((zip) => {
            Object.keys(zip.files).filter(fileName => fileName.includes('TOSCA-Metadata/'))
                .forEach((filename) => {
                    zip.files[filename].async('string').then((fileData) => {
                        if (fileData) {
                            if (filename.includes('TOSCA-Metadata/')) {
                                const metaDataTabInfo: MetaDataTabModel = this.getMetaDataTabInfo(fileData);
                                packageName = metaDataTabInfo.name;
                                this.setMetaData(metaDataTabInfo);
                                console.log('found file ' + packageName);
                            }
                        }
                    });
                });
        });

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
                            this.setImports(filename, fileData, packageName);
                        }
                    }
                });
            });
        });
    }

    setScripts(filename: string, fileData: any) {
        this.packageCreationStore.addScripts(filename, fileData);
    }

    setImports(filename: string, fileData: any, packageName: string) {
        console.log(filename);
        if (filename.includes(packageName)) {
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
            this.setPackageDescription(definition.metadata.template_description);
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

    setMetaData(metaDataObject: MetaDataTabModel) {
        this.packageCreationStore.changeMetaData(metaDataObject);
    }

    getMetaDataTabInfo(fileData: string) {
        const metaDataTabModel = new MetaDataTabModel();
        const arrayOfLines = fileData.split('\n');
        metaDataTabModel.entryFileName = arrayOfLines[3].split(':')[1];
        metaDataTabModel.name = arrayOfLines[4].split(':')[1];
        metaDataTabModel.version = arrayOfLines[5].split(':')[1];
        metaDataTabModel.mode = arrayOfLines[6].split(':')[1];
        console.log(arrayOfLines[7]);
        if (arrayOfLines[7].split(':')) {
            metaDataTabModel.templateTags = new Set<string>(arrayOfLines[7].split(':')[1].split(','));
        }
        return metaDataTabModel;
    }

    private setPackageDescription(templateDescription: string) {
        const metaData = this.packageCreationStore.getMetaData();
        metaData.description = templateDescription;
        this.setMetaData(metaData);

    }
}
