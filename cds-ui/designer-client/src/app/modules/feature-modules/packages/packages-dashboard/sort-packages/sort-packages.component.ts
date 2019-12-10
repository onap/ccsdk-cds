import {Component, OnInit} from '@angular/core';
import {PackagesStore} from '../../packages.store';

@Component({
    selector: 'app-sort-packages',
    templateUrl: './sort-packages.component.html',
    styleUrls: ['./sort-packages.component.css']
})
export class SortPackagesComponent implements OnInit {
    sortTypes: string[];
    selected: string;

    constructor(private packagesStore: PackagesStore) {
        this.sortTypes = Object.keys(SortByToServerValue);
        this.selected = 'Recent';
    }

    ngOnInit() {
    }

    sortPackages(event: any) {
        const key = event.target.name;
        console.log(key);
        this.selected = key;
        this.packagesStore.sortPagedPackages(SortByToServerValue[key]);
    }
}

enum SortByToServerValue {
    Recent = 'DATE',
    Name = 'NAME',
}
