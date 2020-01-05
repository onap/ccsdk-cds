/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/

import {Injectable} from '@angular/core';

import {Store} from '../../../../common/core/stores/Store';

import {CBAPackage, Definition} from './mapping-models/CBAPacakge.model';
import {Metadata} from './mapping-models/definitions/VlbDefinition';
import {PackageCreationService} from './package-creation.service';


@Injectable({
    providedIn: 'root'
})
export class PackageCreationStore extends Store<CBAPackage> {

    constructor(private packageCreationService: PackageCreationService) {
        super(new CBAPackage());
    }

    changeMetaData(metaDataObject: Metadata) {

        this.setState({
            ...this.state,
            metaData: metaDataObject
        });
    }

    addDefinition(name: string, content: string) {

        this.setState({
            ...this.state,
            definitions: new Definition(this.state.definitions.files.set(name, content))
        });
    }
}
