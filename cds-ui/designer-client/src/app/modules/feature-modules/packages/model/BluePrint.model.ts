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

import { Page } from 'src/app/common/model/page';

export class BlueprintModel {


    id: string;
    artifactUUId?: null;
    artifactType: string;
    artifactVersion: string;
    artifactDescription: string;
    internalVersion?: null;
    createdDate: string;
    artifactName: string;
    published: string;
    updatedBy: string;
    tags: string;
}

export class BluePrintPage extends Page<BlueprintModel> {
}
