import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PackageCreationStore } from '../package-creation.store';

@Component({
    selector: 'app-template-mapping',
    templateUrl: './template-mapping.component.html',
    styleUrls: ['./template-mapping.component.css']
})
export class TemplateMappingComponent implements OnInit {
    creationView = true;
    listView = false;

    constructor(private route: ActivatedRoute, private pakcageStore: PackageCreationStore) {
    }

    ngOnInit() {
        if (this.route.snapshot.paramMap.has('id')) {
            console.log('Edit mode');
            this.creationView = false;
            this.listView = false;
        } else {
            console.log('Create mode');
            this.pakcageStore.clear();
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

}
