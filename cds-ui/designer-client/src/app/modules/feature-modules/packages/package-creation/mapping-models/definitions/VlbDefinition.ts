import {JsonObject, JsonProperty} from 'json2typescript';

@JsonObject
export class VlbDefinition {

    // tslint:disable-next-line:variable-name
    tosca_definitions_version: string;
    metadata: Metadata;
    imports: Import[];
    // dsl_definitions:           DSLDefinitions;
    // topology_template: TopologyTemplate;
}

export class Metadata {
    @JsonProperty('template_author')
    templateAuthor: string;
    'author-email': string;
    'user-groups': string;
    @JsonProperty('template_name')
    templateName: string;
    @JsonProperty('template_version')
    templateVersion: string;
    @JsonProperty('template_tag')
    templateTags: string;
}

export class Import {
    file: string;
}
