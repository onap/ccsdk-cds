import {PackageCreationModes} from './PackageCreationModes';
import {CBAPackage, Definition, Scripts} from '../mapping-models/CBAPacakge.model';
import {FilesContent} from '../mapping-models/metadata/MetaDataTab.model';
import {Import, Metadata, VlbDefinition} from '../mapping-models/definitions/VlbDefinition';
import {PackageCreationUtils} from '../package-creation.utils';


export class DesignerCreationMode extends PackageCreationModes {

    constructor() {
        super();
    }

    execute(cbaPackage: CBAPackage, packageCreationUtils: PackageCreationUtils) {
        this.addToscaMetaDataFile(cbaPackage.metaData);
        this.createDefinitionsFolder(cbaPackage.definitions, packageCreationUtils);
        this.addScriptsFolder(cbaPackage.scripts);
    }


    /* private createDefinitionsFolder(definition: Definition) {
         definition.imports.forEach((key, value) => {
             console.log(key);
             FilesContent.putData(key, value);
         });

     }*/

    private addScriptsFolder(scripts: Scripts) {
        scripts.files.forEach((key, value) => {
            FilesContent.putData(key, value);
        });
    }

    private createDefinitionsFolder(definition: Definition, packageCreationUtils: PackageCreationUtils) {
        definition.imports.forEach((key, valueOfFile) => {
            FilesContent.putData(key, valueOfFile);
        });

        const filenameEntry = 'Definitions/vLB_CDS.json';
        const vlbDefinition: VlbDefinition = new VlbDefinition();
        const metadata: Metadata = new Metadata();

        metadata.template_author = 'Shaaban Ebrahim';
        metadata.template_name = definition.metaDataTab.name;
        metadata.template_tags = definition.metaDataTab.tags;
        metadata.template_version = definition.metaDataTab.version;
        metadata['author-email'] = 'shaaban.eltanany.ext@orange.com';
        metadata['user-groups'] = 'test';
        definition.metaDataTab.mapOfCustomKey.forEach((key, customKeyValue) => {
            metadata[key] = customKeyValue;
        });
        vlbDefinition.metadata = metadata;
        // const files = Import[definition.imports.size];
        const files: Import[] = [];
        definition.imports.forEach((key, valueOfFile) => {
            files.push({file: valueOfFile});
        });
        console.log(vlbDefinition);
        vlbDefinition.imports = files;
        /*  vlbDefinition.imports = this.definition.imports; /!*[{
              this.this.definition.imports.forEach(key,value =>{

              });
              file: 'Definitions/data_types.json'
          }]; */

        const value = packageCreationUtils.transformToJson(vlbDefinition);
        FilesContent.putData(filenameEntry, value);
        console.log('hello there');
        console.log(FilesContent.getMapOfFilesNamesAndContent());

    }
}
