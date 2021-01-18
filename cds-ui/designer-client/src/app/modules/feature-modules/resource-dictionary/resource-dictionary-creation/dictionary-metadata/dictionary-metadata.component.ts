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
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DictionaryModel } from '../../model/dictionary.model';
import { DictionaryCreationService } from '../dictionary-creation.service';
import { DictionaryCreationStore } from '../dictionary-creation.store';
import { MetaData } from '../../model/metaData.model';

@Component({
    selector: 'app-dictionary-metadata',
    templateUrl: './dictionary-metadata.component.html',
    styleUrls: ['./dictionary-metadata.component.css']
})
export class DictionaryMetadataComponent implements OnInit {
    packageNameAndVersionEnables = true;
    counter = 0;
    tags = new Set<string>();
    metaDataTab: MetaData = new MetaData();
    errorMessage: string;

    constructor(
        private dictionaryCreationStore: DictionaryCreationStore
    ) { }

    ngOnInit() {
        this.dictionaryCreationStore.state$.subscribe(element => {
            console.log(this.metaDataTab);
            if (element && element.metaData) {
                this.metaDataTab = element.metaData;
                this.metaDataTab.property.entry_schema = element.metaData.property.entry_schema;
                this.tags = new Set(element.metaData.tags.split(','));
                this.tags.delete('');
                this.tags.delete(' ');

                //  console.log(element);
                // console.log(element.metaData.property['entry_schema']);
            }
        });

    }

    // getSources() {
    //     this.dictionaryCreationService.getSources().subscribe(res => {
    //         console.log(res);
    //     });
    // }

    removeTag(value) {
        this.tags.delete(value);
        this.mergeTags();
    }


    addTag(event) {
        const value = event.target.value;
        console.log(value);
        if (value && value.trim().length > 0) {
            event.target.value = '';
            this.tags.add(value);
            // merge
            this.mergeTags();
        }
    }

    mergeTags() {
        let tag = '';
        this.tags.forEach((val, index) => {
            tag += val + ', ';
        });
        this.metaDataTab.tags = tag.trim();
        this.saveMetaDataToStore();
    }

    saveMetaDataToStore() {
        console.log(this.metaDataTab);
        this.dictionaryCreationStore.changeMetaData(this.metaDataTab);
    }
}
