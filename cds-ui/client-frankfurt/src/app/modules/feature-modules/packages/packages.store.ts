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
import {BluePrintPage} from './model/BluePrint.model';
import {Store} from '../../../common/core/stores/Store';
import {PackagesListService} from './packages-list.service';
import {PackagesDashboardState} from './model/packages-dashboard.state';


@Injectable({
    providedIn: 'root'
})
export class PackagesStore extends Store<PackagesDashboardState> {
    // TDOD fixed for now as there is no requirement to change it from UI
    public pageSize = 5;

    constructor(private packagesServiceList: PackagesListService) {
        super(new PackagesDashboardState());
    }

    public getAll() {
        console.log('getting all packages...');
        this.getPagedPackages(0, this.pageSize);
    }

    public search(command: string) {
        if (command) {
            this.searchPagedPackages(command, 0, this.pageSize);
        } else {
            this.getPagedPackages(0, this.pageSize);
        }
    }

    public getPage(pageNumber: number, pageSize: number) {
        if (this.isCommandExist()) {
            this.searchPagedPackages(this.state.command, pageNumber, pageSize);
        } else {
            this.getPagedPackages(pageNumber, pageSize);
        }
    }

    public sortPagedPackages(sortBy: string) {
        if (this.isCommandExist()) {
            this.searchPagedPackages(this.state.command, this.state.currentPage, this.pageSize, sortBy);
        } else {
            this.getPagedPackages(this.state.currentPage, this.pageSize, sortBy);
        }

    }

    private getPagedPackages(pageNumber: number, pageSize: number, sortBy: string = this.state.sortBy) {

        this.packagesServiceList.getPagedPackages(pageNumber, pageSize, sortBy)
            .subscribe((pages: BluePrintPage[]) => {
                this.setState({
                    ...this.state,
                    page: pages[0],
                    command: '',
                    totalPackages: pages[0].totalElements,
                    currentPage: pageNumber,
                    // this param is set only in get all as it represents the total number of pacakges in the server
                    totalPackagesWithoutSearchorFilters: pages[0].totalElements,
                    sortBy
                });
            });
    }

    private searchPagedPackages(keyWord: string, pageNumber: number, pageSize: number, sortBy: string = this.state.sortBy) {
        this.packagesServiceList.getPagedPackagesByKeyWord(keyWord, pageNumber, pageSize, sortBy)
            .subscribe((pages: BluePrintPage[]) => {
                this.setState({
                    ...this.state,
                    page: pages[0],
                    command: keyWord,
                    totalPackages: pages[0].totalElements,
                    currentPage: pageNumber,
                    sortBy
                });
            });
    }

    private isCommandExist() {
        return this.state.command;
    }
}
