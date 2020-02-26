import { CBAPackage } from '../mapping-models/CBAPacakge.model';
import { ModeType } from '../mapping-models/ModeType';
import { FilesContent, MetaDataTabModel } from '../mapping-models/metadata/MetaDataTab.model';
import { PackageCreationUtils } from '../package-creation.utils';


export abstract class PackageCreationModes {

    protected constructor() {
    }

    public static setEntryPoint(metaDataTab: MetaDataTabModel) {
        if (metaDataTab.mode.startsWith(ModeType.Designer)) {
            metaDataTab.entryFileName = 'Definitions/vLB_CDS.json';
        } else {
            // TODO Not implemented
            metaDataTab.entryFileName = '';
        }
        return metaDataTab;
    }

    public static mapModeType(cbaPackage: CBAPackage) {
        if (cbaPackage.metaData.mode.startsWith('Scripting')) {
            cbaPackage.metaData.mode = ModeType.Scripting;
        } else if (cbaPackage.metaData.mode.startsWith('Designer')) {
            cbaPackage.metaData.mode = ModeType.Designer;
        } else {
            cbaPackage.metaData.mode = ModeType.Generic;
        }
        return cbaPackage;
    }

    getValueOfMetaData(metaDataTab: MetaDataTabModel): string {
        let tags = '';
        let count = 0;
        for (const tag of metaDataTab.templateTags) {
            count++;
            if (count === metaDataTab.templateTags.size) {
                tags += tag;
            } else {
                tags += tag + ', ';
            }
        }
        return 'TOSCA-Meta-File-Version: 1.0.0\n' +
            'CSAR-Version: 1.0\n' +
            'Created-By: Shaaban Ebrahim <shaaban.eltanany.ext@orange.con>\n' +
            'Entry-Definitions:' + metaDataTab.entryFileName + '\n' +
            'Template-Name:' + metaDataTab.name + '\n' +
            'Template-Version:' + metaDataTab.version + '\n' +
            'Template-Type: ' + metaDataTab.mode + '\n' +
            'Template-Tags:' + tags;

    }

    protected addToscaMetaDataFile(metaDataTab: MetaDataTabModel) {
        const filename = 'TOSCA-Metadata/TOSCA.meta';
        FilesContent.putData(filename, this.getValueOfMetaData(metaDataTab));
    }


    abstract execute(cbaPackage: CBAPackage, packageCreationUtils: PackageCreationUtils);


}
