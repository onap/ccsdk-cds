import {CBAPackage} from '../mapping-models/CBAPacakge.model';

export abstract class PackageCreation {

    abstract setModeType(cbaPackage: CBAPackage);

    abstract setEntryPoint(cbaPackage: CBAPackage);

    createToscaMetaData(cbaPackage: CBAPackage) {

    }

}
