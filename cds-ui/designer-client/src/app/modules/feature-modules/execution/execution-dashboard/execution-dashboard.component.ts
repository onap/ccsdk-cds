import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
    selector: 'app-execution-dashboard',
    templateUrl: './execution-dashboard.component.html',
    styleUrls: ['./execution-dashboard.component.css'],
})
export class ExecutionDashboardComponent implements OnInit {

    activeTab = 'setup';

    // Pre-filled from query params when navigating from package detail
    prefilledName = '';
    prefilledVersion = '';

    constructor(private route: ActivatedRoute) {
    }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            if (params.name) {
                this.prefilledName = params.name;
            }
            if (params.version) {
                this.prefilledVersion = params.version;
            }
        });
    }

    selectTab(tab: string) {
        this.activeTab = tab;
    }
}
