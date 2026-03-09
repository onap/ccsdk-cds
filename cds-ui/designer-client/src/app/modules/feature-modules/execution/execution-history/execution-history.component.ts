import { Component, OnInit } from '@angular/core';

interface ExecutionRecord {
    requestId: string;
    blueprintName: string;
    blueprintVersion: string;
    actionName: string;
    status: string;
    timestamp: string;
}

@Component({
    selector: 'app-execution-history',
    templateUrl: './execution-history.component.html',
    styleUrls: ['./execution-history.component.css'],
})
export class ExecutionHistoryComponent implements OnInit {

    executions: ExecutionRecord[] = [];
    selectedExecution: ExecutionRecord = null;

    constructor() {
    }

    ngOnInit() {
        this.loadHistory();
    }

    loadHistory() {
        // History is populated from local session storage for now.
        // API-based audit history can be added when the backend enhancement is available.
        const raw = sessionStorage.getItem('cds-execution-history');
        if (raw) {
            try {
                this.executions = JSON.parse(raw);
            } catch (_) {
                this.executions = [];
            }
        }
    }

    selectExecution(exec: ExecutionRecord) {
        this.selectedExecution = exec;
    }

    clearHistory() {
        this.executions = [];
        sessionStorage.removeItem('cds-execution-history');
        this.selectedExecution = null;
    }

    get hasHistory(): boolean {
        return this.executions.length > 0;
    }
}
