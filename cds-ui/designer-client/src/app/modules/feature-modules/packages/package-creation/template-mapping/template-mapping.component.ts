import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'app-template-mapping',
    templateUrl: './template-mapping.component.html',
    styleUrls: ['./template-mapping.component.css']
})
export class TemplateMappingComponent implements OnInit {
    creationView = true;
    listView = false;

    constructor() {
    }

    ngOnInit() {
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
