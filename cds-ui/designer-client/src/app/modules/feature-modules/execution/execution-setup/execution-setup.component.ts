import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { ExecutionApiService } from '../execution-api.service';

@Component({
    selector: 'app-execution-setup',
    templateUrl: './execution-setup.component.html',
    styleUrls: ['./execution-setup.component.css'],
})
export class ExecutionSetupComponent implements OnInit {

    @Input() prefilledName = '';
    @Input() prefilledVersion = '';
    @Output() executionCompleted = new EventEmitter<any>();

    blueprintName = '';
    blueprintVersion = '';
    actionName = '';
    isExecuting = false;

    availablePackages: any[] = [];
    availableVersions: string[] = [];
    availableActions: string[] = [];

    payloadText = JSON.stringify(this.defaultPayload(), null, 2);
    aceOptions: any = {
        maxLines: 30,
        minLines: 15,
        autoScrollEditorIntoView: true,
        showPrintMargin: false,
    };

    lastResponse: any = null;
    lastResponseText = '';

    constructor(
        private executionApiService: ExecutionApiService,
        private toastService: ToastrService,
    ) {
    }

    ngOnInit() {
        this.loadPackages();
        if (this.prefilledName) {
            this.blueprintName = this.prefilledName;
        }
        if (this.prefilledVersion) {
            this.blueprintVersion = this.prefilledVersion;
        }
        if (this.prefilledName && this.prefilledVersion) {
            this.updatePayload();
        }
    }

    loadPackages() {
        this.executionApiService.getPagedPackages(0, 1000, 'DATE').subscribe(
            (pages: any) => {
                const page = Array.isArray(pages) ? pages[0] : pages;
                const content = page && page.content ? page.content : [];
                this.availablePackages = content;
            },
            err => {
                console.error('Failed to load packages', err);
            }
        );
    }

    onBlueprintNameChange() {
        const matching = this.availablePackages.filter(p => p.artifactName === this.blueprintName);
        this.availableVersions = matching.map(p => p.artifactVersion);
        if (this.availableVersions.length === 1) {
            this.blueprintVersion = this.availableVersions[0];
        } else {
            this.blueprintVersion = '';
        }
        this.actionName = '';
        this.availableActions = [];
        this.updatePayload();
        if (this.blueprintVersion) {
            this.loadActions();
        }
    }

    onBlueprintVersionChange() {
        this.actionName = '';
        this.availableActions = [];
        this.updatePayload();
        if (this.blueprintVersion) {
            this.loadActions();
        }
    }

    loadActions() {
        this.executionApiService.getWorkflows(this.blueprintName, this.blueprintVersion).subscribe(
            (result: any) => {
                if (result && Array.isArray(result.workflows)) {
                    this.availableActions = result.workflows.slice().sort();
                    if (this.availableActions.length === 1) {
                        this.actionName = this.availableActions[0];
                        this.updatePayload();
                    }
                }
            },
            err => {
                console.error('Failed to load actions', err);
            }
        );
    }

    get uniquePackageNames(): string[] {
        const names = new Set(this.availablePackages.map(p => p.artifactName));
        return Array.from(names).sort();
    }

    canExecute(): boolean {
        return this.blueprintName.length > 0
            && this.blueprintVersion.length > 0
            && this.payloadText.length > 0
            && !this.isExecuting;
    }

    executeBlueprint() {
        if (!this.canExecute()) {
            return;
        }

        let payload: any;
        try {
            payload = JSON.parse(this.payloadText);
        } catch (e) {
            this.toastService.error('Invalid JSON payload. Please correct the syntax.');
            return;
        }

        this.isExecuting = true;
        this.lastResponse = null;
        this.lastResponseText = '';

        this.executionApiService.executeBlueprint(payload).subscribe(
            response => {
                this.isExecuting = false;
                this.lastResponse = response;
                this.lastResponseText = JSON.stringify(response, null, 2);
                this.toastService.success('Execution completed successfully.');
                this.executionCompleted.emit(response);
            },
            error => {
                this.isExecuting = false;
                const msg = (error.error && error.error.message) || error.message || 'Execution failed';
                this.toastService.error(msg);
                this.lastResponseText = JSON.stringify(error.error || error, null, 2);
            }
        );
    }

    private updatePayload() {
        const payload = this.defaultPayload();
        payload.actionIdentifiers.blueprintName = this.blueprintName;
        payload.actionIdentifiers.blueprintVersion = this.blueprintVersion;
        payload.actionIdentifiers.actionName = this.actionName;
        this.payloadText = JSON.stringify(payload, null, 2);
    }

    onActionChange() {
        this.updatePayload();
    }

    private defaultPayload(): any {
        return {
            commonHeader: {
                originatorId: 'CDS-UI',
                requestId: this.generateUUID(),
                subRequestId: this.generateUUID(),
                timestamp: new Date().toISOString(),
            },
            actionIdentifiers: {
                blueprintName: '',
                blueprintVersion: '',
                actionName: '',
                mode: 'sync',
            },
            payload: {},
        };
    }

    resetPayload() {
        this.updatePayload();
    }

    private generateUUID(): string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
            // tslint:disable-next-line:no-bitwise
            const r = Math.random() * 16 | 0;
            // tslint:disable-next-line:no-bitwise
            const v = c === 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }
}
