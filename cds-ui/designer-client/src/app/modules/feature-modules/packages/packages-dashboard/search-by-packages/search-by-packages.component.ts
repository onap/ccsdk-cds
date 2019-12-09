import {Component, OnInit} from '@angular/core';
import {PackagesStore} from '../../packages.store';

@Component({
    selector: 'app-packages-search',
    templateUrl: './search-by-packages.component.html',
    styleUrls: ['./search-by-packages.component.css']
})
export class PackagesSearchComponent implements OnInit {

    private searchQuery = '';

    constructor(private packagesStore: PackagesStore) {
    }

    ngOnInit() {
    }


    searchPackages(event: any) {
        this.searchQuery = event.target.value;
        this.searchQuery = this.searchQuery.trim();
        this.packagesStore.search(this.searchQuery);
    }
}
