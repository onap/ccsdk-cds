import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PackageCreationStore } from '../package-creation.store';
import { SharedService } from './shared-service';

@Component({
    selector: 'app-template-mapping',
    templateUrl: './template-mapping.component.html',
    styleUrls: ['./template-mapping.component.css']
})
export class TemplateMappingComponent implements OnInit, OnDestroy {
    creationView = false;
    listView = true;

    constructor(
        private route: ActivatedRoute,
        private pakcageStore: PackageCreationStore,
        private sharedService: SharedService
    ) {
    }
    ngOnDestroy(): void {
        // this.sharedService.list.unsubscribe();
        // this.sharedService.mode.unsubscribe();
        // this.pakcageStore.unsubscribe();
    }

    ngOnInit() {

        if (this.route.snapshot.paramMap.has('id')) {
            console.log('Edit mode');
            this.creationView = true;
            this.listView = false;
            console.log('URL contains Id');
            if (this.pakcageStore.istemplateExist()) {
                this.sharedService.enableEdit();
            }
        } else {
            console.log('Create mode');
            this.pakcageStore.clear();
            this.sharedService.disableEdit();
        }
    }
    openCreationView() {
        console.log('open creation view');
        this.creationView = false;
        this.listView = true;
    }

    openListView() {
        console.log('open list view');
        this.creationView = true;
        this.listView = false;

    }

}
