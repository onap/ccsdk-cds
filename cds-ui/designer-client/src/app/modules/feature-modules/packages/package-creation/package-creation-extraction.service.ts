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

    private zipFile: JSZip = new JSZip();
    private entryDefinitionKeys: string[] = ['template_tags', 'user-groups',
        'author-email', 'template_version', 'template_name', 'template_author', 'template_description'];

    private toscaMetaDataKeys: string[] = ['TOSCA-Meta-File-Version', 'CSAR-Version',
        'Created-By', 'Entry-Definitions', 'Template-Name', 'Template-Version', 'Template-Type', 'Template-Tags'];
    @ViewChild(MetadataTabComponent, {static: false})
    private metadataTabComponent: MetadataTabComponent;

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

    private setScripts(filename: string, fileData: any) {
        this.packageCreationStore.addScripts(filename, fileData);
    }

    private setImports(filename: string, fileData: any, packageName: string) {
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
            console.log(definition);
            console.log(definition.topology_template);
            const content = {};
            const workflow = 'workflows';
            content[workflow] = definition.topology_template.workflows;
            const nodeTemplates = 'node_templates';
            content[nodeTemplates] = definition.topology_template.node_templates;
            this.designerStore.saveSourceContent(JSON.stringify(content));

        }
        this.packageCreationStore.addDefinition(filename, fileData);

    }

    private setTemplates(filename: string, fileData: any) {
        this.packageCreationStore.addTemplate(filename, fileData);
    }

    private setMapping(fileName: string, fileData: string) {
        this.packageCreationStore.addMapping(fileName, fileData);
    }

    private setMetaData(metaDataObject: MetaDataTabModel) {
        this.packageCreationStore.changeMetaData(metaDataObject);
    }

    private getMetaDataTabInfo(fileData: string) {
        const metaDataTabModel = new MetaDataTabModel();

        const arrayOfLines = fileData.split('\n');
        const map = new Map<string, string>();
        for (const currentLine of arrayOfLines) {
            const currentKey = currentLine.split(':')[0];
            const currentValue = currentLine.split(':')[1];
            map.set(currentKey, currentValue);
        }
        metaDataTabModel.entryFileName = map.get(this.toscaMetaDataKeys[3]);
        metaDataTabModel.name = map.get(this.toscaMetaDataKeys[4]);
        metaDataTabModel.version = map.get(this.toscaMetaDataKeys[5]).trim();
        metaDataTabModel.mode = map.get(this.toscaMetaDataKeys[6]);
        if (map.get(this.toscaMetaDataKeys[7])) {
            metaDataTabModel.templateTags = new Set<string>(map.get(this.toscaMetaDataKeys[7]).split(','));
        }
        return metaDataTabModel;
    }

    private setPackageDescription(templateDescription: string) {
        const metaData = this.packageCreationStore.getMetaData();
        metaData.description = templateDescription;
        this.setMetaData(metaData);

    }
}
