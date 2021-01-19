import { JsonObject, JsonProperty } from 'json2typescript';

/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2020 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
@JsonObject
export class MetaData {
    @JsonProperty('name')
    public name: string;
    public tags: string;
    @JsonProperty('updated-by')
    public ['updated-by'] = 'ahmed.eldeeb.ext@orange.com';
    public property: Property;
    public sources: any;

    constructor() {
        this.name = '';
        this.tags = '';
        // this.updatedBy = '';
        this.property = new Property();
        this.sources = {};
    }
}

@JsonObject()
export class Property {
    public description: string;
    type: string;
    required: boolean;
    @JsonProperty('entry_schema')
    // tslint:disable-next-line: variable-name
    entry_schema: EntrySchema = new EntrySchema();

    constructor() {
        this.description = '';
        this.type = '';
        this.entry_schema = new EntrySchema();
        this.required = false;
    }

}
@JsonObject()
export class EntrySchema {
    type: string;
    constraints: [];
    constructor() {
        this.type = '';
        this.constraints = [];
    }
}
