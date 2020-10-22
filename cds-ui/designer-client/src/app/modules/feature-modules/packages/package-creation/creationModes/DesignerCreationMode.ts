import {PackageCreationModes} from './PackageCreationModes';
import {CBAPackage, Plans, Requirements, Scripts} from '../mapping-models/CBAPacakge.model';
import {FilesContent} from '../mapping-models/metadata/MetaDataTab.model';
import {CBADefinition, Import, Metadata} from '../mapping-models/definitions/CBADefinition';
import {PackageCreationUtils} from '../package-creation.utils';


export class DesignerCreationMode extends PackageCreationModes {

    // Refactor methods params to be in constructor level
    constructor() {
        super();
    }

    execute(cbaPackage: CBAPackage, packageCreationUtils: PackageCreationUtils) {
        this.addToscaMetaDataFile(cbaPackage.metaData);
        this.createDefinitionsFolder(cbaPackage, packageCreationUtils);
        this.addScriptsFolder(cbaPackage.scripts);
        this.addTemplateFolder(cbaPackage);
        this.addPlansFolder(cbaPackage.plans);
        this.addRequirementsFolder(cbaPackage.requirements);
    }

    private addScriptsFolder(scripts: Scripts) {
        scripts.files.forEach((value, key) => {
            FilesContent.putData(key, value);
        });
    }

    private addRequirementsFolder(requirements: Requirements) {
        requirements.files.forEach((value, key) => {
            FilesContent.putData(key, value);
        });
    }

    private addPlansFolder(plans: Plans) {
        plans.files.forEach((value, key) => {
            FilesContent.putData(key, value);
        });
    }

    private addTemplateFolder(cbaPackage: CBAPackage) {
        // Create Template Files Folder
        cbaPackage.templates.files.forEach((value, key) => {
            FilesContent.putData(key, value);
        });
        // Create Mapping Files Folder
        cbaPackage.mapping.files.forEach((value, key) => {
            FilesContent.putData(key, value);
        });
    }

    private createDefinitionsFolder(cbaPackage: CBAPackage, packageCreationUtils: PackageCreationUtils) {
        if (cbaPackage.definitions.imports && cbaPackage.definitions.imports.size > 0) {
            cbaPackage.definitions.imports.forEach((valueOfFile, key) => {
                FilesContent.putData(key, valueOfFile);
            });
        }

        const filenameEntry = 'Definitions/' + cbaPackage.metaData.name.trim() + '.json';
        const cbaDefinition: CBADefinition = new CBADefinition();
        const metadata: Metadata = new Metadata();

        metadata.template_author = 'Shaaban Ebrahim';
        metadata.template_name = cbaPackage.metaData.name;
        metadata.template_version = cbaPackage.metaData.version;
        metadata['author-email'] = 'shaaban.eltanany.ext@orange.com';
        metadata['user-groups'] = 'test';
        metadata.template_description = cbaPackage.metaData.description;
        if (cbaPackage.metaData.mapOfCustomKey && cbaPackage.metaData.mapOfCustomKey.size > 0) {
            cbaPackage.metaData.mapOfCustomKey.forEach((customKeyValue, key) => {
                metadata[key] = customKeyValue;
            });
        }
        // create Tags
        let fullTags = '';
        let setCount = 0;
        if (cbaPackage.metaData.templateTags && cbaPackage.metaData.templateTags.size > 0) {
            cbaPackage.metaData.templateTags.forEach(val => {
                setCount++;
                if (setCount === cbaPackage.metaData.templateTags.size) {
                    fullTags += val;
                } else {
                    fullTags += val + ', ';
                }
            });
        }
        metadata.template_tags = fullTags;
        cbaDefinition.metadata = metadata;
        const files: Import[] = [];
        let insideVlbDefinition: CBADefinition = null;
        if (cbaPackage.definitions.imports && cbaPackage.definitions.imports.size > 0) {
            cbaPackage.definitions.imports.forEach((valueOfFile, key) => {
                if (!key.includes(cbaPackage.metaData.name)) {
                    if (!key.includes('resources_definition_types')) {
                        files.push({file: key});
                    }
                } else {
                    insideVlbDefinition = JSON.parse(valueOfFile);
                }
            });
        }
        cbaDefinition.imports = files;
        if (cbaPackage.definitions && cbaPackage.definitions.dslDefinition &&
            cbaPackage.definitions.dslDefinition.content) {
            cbaDefinition.dsl_definitions = JSON.parse(cbaPackage.definitions.dslDefinition.content);
        }

        // cbaDefinition.imports = files;
        if (cbaPackage.templateTopology && cbaPackage.templateTopology.content) {
            cbaDefinition.topology_template = JSON.parse(cbaPackage.templateTopology.content);
        } else if (insideVlbDefinition && insideVlbDefinition.topology_template) {
            cbaDefinition.topology_template = insideVlbDefinition.topology_template;
        }

        const value = packageCreationUtils.transformToJson(cbaDefinition);
        FilesContent.putData(filenameEntry, value);
        console.log(FilesContent.getMapOfFilesNamesAndContent());
    }
}
