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
import { Store } from '../../../common/core/stores/Store';
import { Observable, of } from 'rxjs';
import { DictionaryDashboardState } from './model/dictionary-dashboard.state';
import { DictionaryPage } from './model/dictionary.model';
import { DictionaryApiService } from './dictionary-api.service';

@Injectable({
    providedIn: 'root'
})
export class DictionaryStore extends Store<DictionaryDashboardState> {
    // TDOD fixed for now as there is no requirement to change it from UI
    public pageSize = 5;
    private dictionaryContent: DictionaryPage = new DictionaryPage();

    constructor(private dictionaryServiceList: DictionaryApiService) {
        super(new DictionaryDashboardState());
    }

    public search(command: string) {
        if (command) {
            this.searchPagedDictionary(command, 0, this.pageSize);
        } else {
            this.getPagedDictionary(0, this.pageSize);
        }
    }

    public getAll() {
        console.log('getting all packages...');
        this.getPagedDictionary(0, this.pageSize);
    }

    public getPage(pageNumber: number, pageSize: number) {
        if (this.isCommandExist()) {
            this.searchPagedDictionary(this.state.command, pageNumber, pageSize);
        } else {
            this.getPagedDictionary(pageNumber, pageSize);
        }
    }

    public sortPagedDictionary(sortBy: string) {
        if (this.isCommandExist()) {
            this.searchPagedDictionary(this.state.command, this.state.currentPage, this.pageSize, sortBy);
        } else {
            this.getPagedDictionary(this.state.currentPage, this.pageSize, sortBy);
        }

    }   

    public filterByTags(tags: string[]) {
        console.log(this.state.currentPage);
        this.getPagedDictionaryByTags(this.state.command, this.state.currentPage,
            this.pageSize, this.state.sortBy, tags);

    }

    protected getPagedDictionary(pageNumber: number, pageSize: number, sortBy: string = this.state.sortBy) {

        this.dictionaryServiceList.getPagedDictionary(pageNumber, pageSize, sortBy)
            .subscribe((pages: DictionaryPage[]) => {
                console.log(pages);
                this.setState({
                    ...this.state,
                    page: pages,
                    filteredPackages: pages,
                    command: '',
                    totalPackages: pages['totalElements'],
                    currentPage: pageNumber,
                    // this param is set only in get all as it represents the total number of pacakges in the server
                    totalDictionariesWithoutSearchorFilters: pages['totalElements'],
                    tags: [],
                    sortBy
                });
            });
    }

    private searchPagedDictionary(keyWord: string, pageNumber: number, pageSize: number, sortBy: string = this.state.sortBy) {
        this.dictionaryServiceList.getPagedDictionaryByKeyWord(keyWord, pageNumber, pageSize, sortBy)
            .subscribe((pages: DictionaryPage[]) => {
                console.log(pages);
                this.setState({
                    ...this.state,
                    page: pages,
                    filteredPackages: pages,
                    command: keyWord,
                    totalPackages: pages['totalElements'],
                    currentPage: pageNumber,
                    tags: [],
                    sortBy
                });
            });
    }

    private isCommandExist() {
        return this.state.command;
    }

    private getPagedDictionaryByTags(keyWord: string, currentPage1: number, pageSize: number, sortBy1: string, tagsSearchable: string[]) {
        this.getPagedDictionaryByKeyWordFilteredByTags(tagsSearchable)
            .subscribe((pages: DictionaryPage[]) => {
                this.setState({
                    ...this.state,
                    page: this.state.page,
                    filteredPackages: pages,
                    command: keyWord,
                    tags: tagsSearchable,
                    //  totalPackages: pages.totalElements,
                    currentPage: currentPage1,
                    sortBy: sortBy1,
                    totalPackages: this.state.page['totalElements'],
                });
            });
    }

    private getPagedDictionaryByKeyWordFilteredByTags(tagsSearchable: string[]): Observable<any> {
        this.dictionaryContent.content = [];
        if (tagsSearchable && tagsSearchable.length !== 0 && !tagsSearchable.includes('All')) {
            tagsSearchable.forEach(tag => {
                if (tag) {
                    this.state.page['content'].forEach(dictionaryModel => {
                        if (tag.endsWith(',')) {
                            tag = tag.replace(',', '');
                        }
                        dictionaryModel.tags.split(',').forEach(dictionaryModelTag => {
                            if (dictionaryModelTag === tag) {
                                this.dictionaryContent.content.push(dictionaryModel);
                            }
                        });
                    });
                } else {
                    this.getPagedDictionary(this.state.currentPage, this.pageSize);
                    return of(this.state.page);
                }
            });
            this.dictionaryContent.content = this.dictionaryContent.content.filter((value, index, self) => self.indexOf(value) === index);
            console.log('the lenght is ' + this.dictionaryContent.content.length);
            return of(this.dictionaryContent);
        } else {
            this.getPagedDictionary(this.state.currentPage, this.pageSize);
            return of(this.state.page);
        }
    }
}
