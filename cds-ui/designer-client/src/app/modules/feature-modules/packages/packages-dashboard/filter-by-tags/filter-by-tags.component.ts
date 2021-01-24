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

import {Component, ElementRef, OnInit, QueryList, ViewChildren} from '@angular/core';
import {PackagesStore} from '../../packages.store';
import {BlueprintModel, BlueprintPage} from '../../model/Blueprint.model';

@Component({
    selector: 'app-filter-by-tags',
    templateUrl: './filter-by-tags.component.html',
    styleUrls: ['./filter-by-tags.component.css']
})

export class TagsFilteringComponent implements OnInit {

    page: BlueprintPage;
    tags: string[] = [];
    viewedTags: string[] = [];
    searchTag = '';
    viewedPackages: BlueprintModel[] = [];
    checkBoxTages = '';
    currentPage = 0;
    @ViewChildren('checkboxes') checkboxes: QueryList<ElementRef>;

    constructor(private packagesStore: PackagesStore,
    ) {
        this.refreshTags();
    }

    private refreshTags() {
        this.packagesStore.state$.subscribe(state => {
            console.log(state);
            if (state.page) {
                this.viewedPackages = state.page.content;
                this.tags = [];
                if (state.currentPage !== this.currentPage) {
                    this.checkBoxTages = '';
                    this.currentPage = state.currentPage;
                }
                this.viewedPackages.forEach(element => {
                    element.tags.split(',').forEach(tag => {
                        this.tags.push(tag.trim());
                    });
                    this.tags.push('All');
                    this.tags = this.tags.filter((value, index, self) => self.indexOf(value) === index && value);
                    this.assignTags();
                });
            }
        });
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
        const tagsSelected = this.checkBoxTages.split(',').filter(item => {
            if (item) {
                return true;
            }
        }).map((item) => {
            return item.trim();
        });

        this.packagesStore.filterByTags(tagsSelected);
    }


    resetFilter() {
        this.checkBoxTages = '';
        this.checkboxes.forEach((element) => {
            element.nativeElement.checked = false;
            this.packagesStore.getAll();
        });
    }
}
