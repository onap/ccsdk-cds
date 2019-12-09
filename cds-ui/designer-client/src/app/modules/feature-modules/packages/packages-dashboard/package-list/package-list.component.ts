import {Component, OnInit} from '@angular/core';
import {BlueprintModel} from '../../model/BluePrint.model';
import {PackagesStore} from '../../packages.store';

@Component({
    selector: 'app-packages-list',
    templateUrl: './package-list.component.html',
    styleUrls: ['./package-list.component.css']
})
export class PackageListComponent implements OnInit {

    viewedPackages: BlueprintModel[] = [];


    constructor(private packagesStore: PackagesStore) {
        console.log('PackageListComponent');
        this.packagesStore.state$.subscribe(state => {
            console.log(state);
            if (state.page) {
                this.viewedPackages = state.page.content;
            }
        });
    }

    ngOnInit() {
        this.packagesStore.getAll();
    }

    testDispatch(bluePrint: BlueprintModel) {
        console.log(bluePrint.id);
    }
}
