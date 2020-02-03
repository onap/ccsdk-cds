import {CBAPackage} from '../mapping-models/CBAPacakge.model';
import {ModeType} from '../mapping-models/ModeType';
import {DesignerCreationMode} from './DesignerCreationMode';
import {PackageCreationModes} from './PackageCreationModes';


export class PackageCreationBuilder {

    constructor() {
    }

    public static getCreationMode(cbaPackage: CBAPackage): PackageCreationModes {
        let creationMode: PackageCreationModes;
        /*if (cbaPackage.metaData.mode.includes(ModeType.Generic)) {
            creationMode = new GenericCreationMode();
        } else */
        if (cbaPackage.metaData.mode.includes(ModeType.Designer)) {
            creationMode = new DesignerCreationMode();
        } /*else if (cbaPackage.metaData.mode.includes(ModeType.Scripting)) {
            creationMode = new ScriptingCreationMode();
        }*/
        return creationMode;
    }
}
