import { ResourceDictionary } from './ResourceDictionary.model';
import { JsonObject, JsonProperty } from 'json2typescript';

// Convert ResourceDictionary object to store Mapping
export class MappingAdapter {

    constructor(private resourceDictionary: ResourceDictionary) { }

    ToMapping(): Mapping {
        const mapping = new Mapping();
        mapping.name = this.resourceDictionary.name;
        mapping.dictionaryName = this.resourceDictionary.name;
        // mapping.property = this.resourceDictionary.definition.property;
        mapping.inputParam = false;
        mapping.dictionarySource = 'sdnc';
        mapping.dependencies = [];
        mapping.version = 0;
        return mapping;
    }
}
@JsonObject('Mapping')
export class Mapping {
    @JsonProperty('name')
    name: string;
    // @JsonObject('property')
    // property?: {};
    @JsonProperty('input-param')
    inputParam: boolean;
    @JsonProperty('dictionary-name')
    dictionaryName: string;
    @JsonProperty('dictionary-source')
    dictionarySource: string;
    @JsonProperty()
    dependencies: [];
    @JsonProperty()
    version: number;
}
