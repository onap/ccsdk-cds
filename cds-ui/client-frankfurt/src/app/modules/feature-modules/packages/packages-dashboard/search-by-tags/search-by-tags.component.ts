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

import {Component, OnDestroy, OnInit} from '@angular/core';
import {PackagesStore} from '../../packages.store';
import {BlueprintModel, BluePrintPage} from '../../model/BluePrint.model';

@Component({
    selector: 'app-search-by-tags',
    templateUrl: './search-by-tags.component.html',
    styleUrls: ['./search-by-tags.component.css']
})

export class SearchByTagsComponent implements OnInit {

    page: BluePrintPage;
    tags: string[] = [];
    viewedTags: string[] = [];
    searchTag = '';
    viewedPackages: BlueprintModel[] = [];
    private checkBoxTages = '';
    private searchPackage = '';

    constructor(private packagesStore: PackagesStore,
    ) {
    }

    ngOnInit() {

    }

    reloadChanges(event: any) {
        this.searchTag = event.target.value;
        this.filterItem(this.searchTag);
    }

    private assignTags() {
        this.viewedTags = this.tags;
    }

    private filterItem(value) {
        if (!value) {
            this.assignTags();
        }
        this.viewedTags = this.tags.filter(
            item => item.toLowerCase().indexOf(value.toLowerCase()) > -1
        );
    }

    reloadPackages(event: any) {

        if (!event.target.checked) {
            this.checkBoxTages = this.checkBoxTages.replace(event.target.id + ',', '')
                .replace(event.target.id, '');
        } else {
            this.checkBoxTages += event.target.id.trim() + ',';
        }
        console.log(this.checkBoxTages);
        if (!this.checkBoxTages.includes(',')) {
            return;
        }
        this.viewedPackages = [];
        this.viewedPackages = this.viewedPackages.filter((value, index, self) => self.indexOf(value) === index);
    }

    searchPackages(event: any) {
        this.searchPackage = event.target.value;
        this.searchPackage = this.searchPackage.trim();
        if (this.searchPackage) {
            this.packagesStore.getPagedPackagesByKeyWord(this.searchPackage, 0, 2);

        } else {
            this.packagesStore.getPagedPackages(0, 2);
        }
    }
}
