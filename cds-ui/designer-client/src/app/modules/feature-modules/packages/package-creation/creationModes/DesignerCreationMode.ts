import {PackageCreationModes} from './PackageCreationModes';
import {CBAPackage, Definition, Scripts} from '../mapping-models/CBAPacakge.model';
import {FilesContent} from '../mapping-models/metadata/MetaDataTab.model';

export class DesignerCreationMode extends PackageCreationModes {

    execute(cbaPackage: CBAPackage) {
        this.addToscaMetaDataFile(cbaPackage.metaData);
        this.createDefinitionsFolder(cbaPackage.definitions);
        this.addScriptsFolder(cbaPackage.scripts);
    }


    private createDefinitionsFolder(definition: Definition) {
        definition.imports.forEach((key, value) => {
            console.log(key);
            FilesContent.putData(key, value);
        });

    }

    private addScriptsFolder(scripts: Scripts) {
        scripts.files.forEach((key, value) => {
            FilesContent.putData(key, value);
        });
    }

}
