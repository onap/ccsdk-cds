import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {PackagesStore} from '../../packages.store';
import {map} from 'rxjs/operators';

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

    constructor(private packagesStore: PackagesStore) {
        this.pageSize = packagesStore.pageSize;

        this.packagesStore.state$
            .subscribe(state => {
                this.pageNumber = state.currentPage;
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
            this.packagesStore.getPage(page - 1, this.packagesStore.pageSize);
            this.previousPage = page;
        }
    }

}
