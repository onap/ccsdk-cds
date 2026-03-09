import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'app-live-view',
    templateUrl: './live-view.component.html',
    styleUrls: ['./live-view.component.css'],
})
export class LiveViewComponent implements OnInit {

    lastResponse: any = null;
    lastResponseText = '';

    constructor() {
    }

    ngOnInit() {
        // Load the last execution response from session storage
        const raw = sessionStorage.getItem('cds-last-execution-response');
        if (raw) {
            try {
                this.lastResponse = JSON.parse(raw);
                this.lastResponseText = JSON.stringify(this.lastResponse, null, 2);
            } catch (_) {
                this.lastResponse = null;
            }
        }
    }

    get status(): string {
        if (!this.lastResponse) {
            return 'idle';
        }
        if (this.lastResponse.status && this.lastResponse.status.code === 200) {
            return 'success';
        }
        if (this.lastResponse.status && this.lastResponse.status.code >= 400) {
            return 'failure';
        }
        return 'completed';
    }

    get statusLabel(): string {
        switch (this.status) {
            case 'success': return 'Execution Successful';
            case 'failure': return 'Execution Failed';
            case 'completed': return 'Execution Completed';
            default: return 'No Execution Data';
        }
    }
}
