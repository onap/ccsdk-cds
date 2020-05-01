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
import { Sources } from '../../model/sources.model';
import { Store } from 'src/app/common/core/stores/Store';
import { Injectable } from '@angular/core';
import { DictionaryCreationService } from '../dictionary-creation.service';
import { Definition } from '../../model/definition.model';

@Injectable({
    providedIn: 'root'
})
export class SourcesStore extends Store<Sources> {
    constructor(private dictionaryCreationService: DictionaryCreationService) {
        super(new Sources());
    }

    public getAllSources() {
        console.log('getting all sources...');
        this.getSources();
    }

    protected getSources() {
        this.dictionaryCreationService.getSources()
            .subscribe((sou) => {
                console.log(sou);
                this.setState({
                    ...this.state,
                    sources: sou
                });
        });
    }

    public changeSources(sou) {
        this.setState({
            ...this.state,
            sources: sou
        });
    }

    public saveSources(sources) {
        console.log(sources);
    }
}
