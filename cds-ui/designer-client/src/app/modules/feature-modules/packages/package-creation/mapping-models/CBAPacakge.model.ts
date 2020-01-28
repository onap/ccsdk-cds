
import {MetaDataTabModel} from './metadata/MetaDataTab.model';

export class Definition {
    public files: Map<string, string> = new Map<string, string>();

    constructor(files: Map<string, string>) {
        this.files = files;
    }
}

export class Scripts {
    public files: Map<string, string> = new Map<string, string>();

    constructor(files: Map<string, string>) {
        this.files = files;
    }
}

export class CBAPackage {

    public metaData: MetaDataTabModel;
    public definitions: Definition;
    public scripts: Scripts;

    constructor() {
        this.definitions = new Definition(new Map<string, string>());
        this.scripts = new Scripts(new Map<string, string>());
        this.metaData = new MetaDataTabModel();
    }

}


