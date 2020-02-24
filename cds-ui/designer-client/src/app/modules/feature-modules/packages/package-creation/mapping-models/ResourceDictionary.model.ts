import { JsonObject, JsonProperty } from 'json2typescript';

@JsonObject('ResourceDictionary')
export class ResourceDictionary {
    @JsonProperty()
    name: string;
    @JsonProperty('creation_date')
    creationDate: string;
    @JsonProperty('data_type')
    dataType: string;
    @JsonObject('definition')
    definition?: any | null;
    @JsonProperty('description')
    description: string;
    @JsonProperty('entry_schema')
    entrySchema: string;
    @JsonProperty('esource_dictionary_group')
    resourceDictionaryGroup: string;
    @JsonProperty('tags')
    tags: string;
    @JsonProperty('upadted_by')
    updatedBy: string;
}
