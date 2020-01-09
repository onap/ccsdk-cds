import {Any, JsonObject, JsonProperty} from 'json2typescript';


@JsonObject
export class VlbDefinition {

    // tslint:disable-next-line:variable-name
    tosca_definitions_version: string;
    metadata: Metadata;
    imports: Import[];
    // dsl_definitions:           DSLDefinitions;
    // topology_template: TopologyTemplate;
}

@JsonObject('metadata')
export class Metadata {
    @JsonProperty('template_author')
     // tslint:disable-next-line:variable-name
    template_author: string;
    'author-email': string;
    'user-groups': string;
    @JsonProperty('template_name')
        // tslint:disable-next-line:variable-name
    template_name: string;
    @JsonProperty('template_version')
        // tslint:disable-next-line:variable-name
    template_version: string;
    @JsonProperty('template_tag')
        // tslint:disable-next-line:variable-name
    template_tags: string;

    @JsonProperty('dictionary_group')
        // tslint:disable-next-line:variable-name
    dictionary_group: string;


    /* @JsonProperty('custom_keys', {String}, false)
     mapOfCustomKeys: Map<string, string> = new Map<string, string>();*/
}

export class Import {
    file: string;
}
