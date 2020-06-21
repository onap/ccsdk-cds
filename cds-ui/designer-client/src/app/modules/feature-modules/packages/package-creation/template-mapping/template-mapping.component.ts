import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PackageCreationStore } from '../package-creation.store';
import { SharedService } from './shared-service';

@Component({
    selector: 'app-template-mapping',
    templateUrl: './template-mapping.component.html',
    styleUrls: ['./template-mapping.component.css']
})
export class TemplateMappingComponent implements OnInit {
    creationView = false;
    listView = true;

    constructor(
        private route: ActivatedRoute,
        private pakcageStore: PackageCreationStore,
        private sharedService: SharedService
    ) {
    }

    ngOnInit() {

        if (this.route.snapshot.paramMap.has('id')) {
            console.log('Edit mode');
            this.creationView = true;
            this.listView = false;
            console.log('URL contains Id');
            this.sharedService.enableEdit();
        } else {
            console.log('Create mode');
            this.pakcageStore.clear();
            this.sharedService.disableEdit();
        }
    }
    openCreationView() {
        this.creationView = false;
        this.listView = true;
    }

    openListView() {
        this.listView = false;
        this.creationView = false;
    }

    closeCreationView() {
        this.creationView = true;
        this.listView = false;
    }

}
