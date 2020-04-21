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

import { Page } from 'src/app/common/model/page';

export class DictionaryModel {

    public name: string;
    public tags: string;
    public dataType: string;
    public description: string;
    public entrySchema: string;
    public updatedBy: string;
    public createdDate: string;
    public definition: object;
}

export class DictionaryPage extends Page<DictionaryModel> {
}
