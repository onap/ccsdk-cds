import {Metadata} from './definitions/VlbDefinition';

export class Definition {
    public files: Map<string, string> = new Map<string, string>();

    constructor(files: Map<string, string>) {
        this.files = files;
    }
}

export class CBAPackage {
    public metaData: Metadata;
    public definitions: Definition;

    constructor() {
        this.definitions = new Definition(new Map<string, string>());
        this.metaData = new Metadata();
    }

}


