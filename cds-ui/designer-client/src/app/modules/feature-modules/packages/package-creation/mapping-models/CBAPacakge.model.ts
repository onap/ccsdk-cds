import {MetaDataTabModel} from './metadata/MetaDataTab.model';
import {TemplateTopology} from './definitions/CBADefinition';

export class Definition {

    // public metaDataTab: MetaDataTabModel;
    public imports: Map<string, string>;
    public dslDefinition: DslDefinition;

    // public dslDefinition:

    constructor() {
        this.imports = new Map<string, string>();
        // this.metaDataTab = new MetaDataTabModel();
        this.dslDefinition = new DslDefinition();
    }

    public setImports(key: string, value: string) {
        this.imports.set(key, value);
        return this;
    }

    // public setMetaData(metaDataTab: MetaDataTabModel) {
    //     this.metaDataTab = metaDataTab;
    //     return this;
    // }

    public setDslDefinition(dslDefinition: DslDefinition): Definition {
        this.dslDefinition = dslDefinition;
        return this;
    }
}

export class DslDefinition {
    content: string;
}

export class Base {
    public files: Map<string, string>;

    constructor() {
        this.files = new Map<string, string>();
    }

    public setContent(key: string, value: string) {
        this.files.set(key, value);
        return this;
    }

    public getValue(key: string): string {
        return this.files.get(key);
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

export class Plans extends Base {

}

export class Requirements extends Base {
}


export class Template {
    public files: Map<string, string>;

    constructor() {
        this.files = new Map<string, string>();
    }

    public setTemplates(key: string, value: string) {
        this.files.set(key, value);
        return this;
    }

    public getValue(key: string): string {
        return this.files.get(key);
    }
}

export class Mapping extends Base {
}

export class CBAPackage {

    public metaData: MetaDataTabModel;
    public definitions: Definition;
    public scripts: Scripts;
    public templates: Template;
    public mapping: Mapping;
    public templateTopology: TemplateTopology;
    public plans: Plans;
    public requirements: Requirements;


    constructor() {
        this.definitions = new Definition();
        this.scripts = new Scripts();
        this.metaData = new MetaDataTabModel();
        this.templates = new Template();
        this.mapping = new Mapping();
        this.templateTopology = new TemplateTopology();
        this.plans = new Plans();
        this.requirements = new Requirements();
    }


}


