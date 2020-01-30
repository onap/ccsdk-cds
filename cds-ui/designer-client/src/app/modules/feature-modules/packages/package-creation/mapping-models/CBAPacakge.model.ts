import {MetaDataTabModel} from './metadata/MetaDataTab.model';

export class Definition {

    public metaDataTab: MetaDataTabModel;
    public imports: Map<string, string>;

    // public dslDefinition:

    constructor() {
        this.imports = new Map<string, string>();
        this.metaDataTab = new MetaDataTabModel();
    }

    public setImports(key: string, value: string) {
        this.imports.set(key, value);
        return this;
    }

    public setMetaData(metaDataTab: MetaDataTabModel) {
        this.metaDataTab = metaDataTab;
        return this;
    }

}

export class Scripts {
    public files: Map<string, string>;

    constructor() {
        this.files = new Map<string, string>();
    }

    public setScripts(key: string, value: string) {
        this.files.set(key, value);
        return this;
    }
}

export class CBAPackage {

    public metaData: MetaDataTabModel;
    public definitions: Definition;
    public scripts: Scripts;


    constructor() {
        this.definitions = new Definition();
        this.scripts = new Scripts();
        this.metaData = new MetaDataTabModel();
    }


}


