import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'app-action-attributes',
    templateUrl: './action-attributes.component.html',
    styleUrls: ['./action-attributes.component.css']
})
export class ActionAttributesComponent implements OnInit {

    actionAttributesSideBar: boolean;

    constructor() {
    }

    ngOnInit() {
    }

    _toggleSidebar2() {
        this.actionAttributesSideBar = !this.actionAttributesSideBar;
    }
}
