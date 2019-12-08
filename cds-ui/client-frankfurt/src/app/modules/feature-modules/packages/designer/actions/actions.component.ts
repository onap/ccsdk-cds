import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'app-actions',
    templateUrl: './actions.component.html',
    styleUrls: ['./actions.component.css']
})
export class ActionsComponent implements OnInit {
    actions: string[] = [];

    constructor() {
      this.actions.push('action 1 ');
      this.actions.push('action 2');
    }

    ngOnInit() {
    }

}
