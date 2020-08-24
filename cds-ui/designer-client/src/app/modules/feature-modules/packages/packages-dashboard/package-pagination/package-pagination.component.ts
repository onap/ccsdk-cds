import { Component, OnInit } from '@angular/core';
import { PackagesStore } from '../../packages.store';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
    selector: 'app-package-pagination',
    templateUrl: './package-pagination.component.html',
    styleUrls: ['./package-pagination.component.css'],
})
export class PackagePaginationComponent implements OnInit {
    pageNumber: number;
    totalCount: number;
    pageSize: number;
    previousPage: number;

    constructor(
        private packagesStore: PackagesStore,
        private ngxLoader: NgxUiLoaderService
    ) {
        this.pageSize = packagesStore.pageSize;

        this.packagesStore.state$
            .subscribe(state => {
                this.pageNumber = state.currentPage + 1;
                this.totalCount = state.totalPackages;
            });
    }

    ngOnInit() {
    }

    public getPageFromService(page) {
        console.log('getPageFromService', page);
        if (isNaN(page)) {
            page = 1;
            console.log('page change to first...', page);
        }
        if (this.previousPage !== page) {
            this.ngxLoader.start(); // start master loader
            this.packagesStore.getPage(page - 1, this.packagesStore.pageSize);
            this.previousPage = page;
        }
    }

}
