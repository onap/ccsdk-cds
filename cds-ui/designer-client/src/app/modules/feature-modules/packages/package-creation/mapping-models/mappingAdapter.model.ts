import { ResourceDictionary } from './ResourceDictionary.model';
import { JsonObject, JsonProperty, JsonConvert } from 'json2typescript';

// Convert ResourceDictionary object to store Mapping.
export class MappingAdapter {

    constructor(
        private resourceDictionary: ResourceDictionary,
        private dependancies: Map<string, Array<string>>,
        private dependanciesSource: Map<string, string>) { }

    ToMapping(): Mapping {
       // console.log(this.resourceDictionary.definition.property);
        const mapping = new Mapping();
        mapping.name = this.resourceDictionary.name;
        mapping.dictionaryName = this.resourceDictionary.name;
        mapping.property = Object.assign({}, this.resourceDictionary.definition.property);
        mapping.inputParam = false;
        mapping.dictionarySource = this.dependanciesSource.get(mapping.name);
        if (this.dependancies.get(mapping.name)) {
            mapping.dependencies = this.dependancies.get(mapping.name);
        } else {
            mapping.dependencies = [];
        }
        mapping.version = 0;
        return mapping;
    }
}

@JsonObject('Mapping')
export class Mapping {
    @JsonProperty('name')
    name: string;
    @JsonProperty()
    property: any;
    @JsonProperty('input-param', Boolean)
    inputParam: boolean;
    @JsonProperty('dictionary-name')
    dictionaryName: string;
    @JsonProperty('dictionary-source')
    dictionarySource: string;
    @JsonProperty()
    dependencies: string[];
    @JsonProperty()
    version: number;
}
