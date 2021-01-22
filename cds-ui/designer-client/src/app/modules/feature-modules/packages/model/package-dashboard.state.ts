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


import { BlueprintDetailModel } from './Blueprint.detail.model';
import { Mapping, Scripts, Template } from '../package-creation/mapping-models/CBAPacakge.model';

export class PackageDashboardState {
    configuration: BlueprintDetailModel;
    public scripts: Scripts;
    public templates: Template;
    public mapping: Mapping;
    public imports: Map<string, string>;

    constructor() {
        this.scripts = new Scripts();
        this.templates = new Template();
        this.mapping = new Mapping();
        this.imports = new Map<string, string>();
    }

}
