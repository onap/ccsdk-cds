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
import { Injectable } from '@angular/core';
import { Store } from 'src/app/common/core/stores/Store';
import { ResourceDictionary } from '../model/resource-dictionary.model';
import { DictionaryCreationService } from './dictionary-creation.service';
import { MetaData } from '../model/metaData.model';
import { Sources } from '../model/sources.model';
import { SourcesStore } from './sources-template/sources.store';

@Injectable({
    providedIn: 'root'
})
export class DictionaryCreationStore extends Store<ResourceDictionary> {
    constructor(
        private dictionaryCreationService: DictionaryCreationService,
        private sourcesStore: SourcesStore
    ) {
        super(new ResourceDictionary());
    }
    // changeMetaData(metaDataObject: MetaData) {
    changeMetaData(metaDataObject: any) {
        console.log(metaDataObject);
        this.setState({
            ...this.state,
            metaData: metaDataObject
        });
    }

    getSources() {
        this.sourcesStore.state$.subscribe(data => {
            console.log(data);
        });
    }

    SaveResourceDictionary(resourceDictionary: ResourceDictionary) {
        console.log(this.setState);
    }
}
