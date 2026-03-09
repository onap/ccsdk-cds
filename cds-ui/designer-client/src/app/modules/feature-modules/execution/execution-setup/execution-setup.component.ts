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

    // Workflow-spec driven input form
    inputGroups: Array<{
        inputName: string;
        type: string;
        description: string;
        required: boolean;
        isComplex: boolean;
        value: string;
        fields: Array<{
            name: string;
            type: string;
            description: string;
            required: boolean;
            value: string;
        }>;
    }> = [];
    loadingSpec = false;
    specLoadFailed = false;

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
            this.buildPayload();
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
        this.inputGroups = [];
        this.loadingSpec = false;
        this.specLoadFailed = false;
        this.buildPayload();
        if (this.blueprintVersion) {
            this.loadActions();
        }
    }

    onBlueprintVersionChange() {
        this.actionName = '';
        this.availableActions = [];
        this.inputGroups = [];
        this.loadingSpec = false;
        this.specLoadFailed = false;
        this.buildPayload();
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
                        this.onActionChange();
                    }
                }
            },
            err => {
                console.error('Failed to load actions', err);
            }
        );
    }

    onActionChange() {
        if (this.actionName) {
            this.loadWorkflowSpec();
        } else {
            this.inputGroups = [];
            this.specLoadFailed = false;
            this.buildPayload();
        }
    }

    loadWorkflowSpec() {
        this.loadingSpec = true;
        this.specLoadFailed = false;
        this.inputGroups = [];
        this.buildPayload();
        this.executionApiService.getWorkflowSpec(
            this.blueprintName, this.blueprintVersion, this.actionName
        ).subscribe(
            (spec: any) => {
                this.loadingSpec = false;
                this.resolveWorkflowSpec(spec);
                this.buildPayload();
            },
            err => {
                this.loadingSpec = false;
                this.specLoadFailed = true;
                console.error('Failed to load workflow spec', err);
            }
        );
    }

    resolveWorkflowSpec(spec: any) {
        this.inputGroups = [];
        if (!spec || !spec.workFlowData || !spec.workFlowData.inputs) {
            return;
        }
        const inputs = spec.workFlowData.inputs;
        const dataTypes = spec.dataTypes || {};

        Object.keys(inputs).forEach(inputName => {
            const inputDef = inputs[inputName];
            const typeName = inputDef.type || 'string';
            const dataType = dataTypes[typeName];

            const group: any = {
                inputName,
                type: typeName,
                description: inputDef.description || '',
                required: !!inputDef.required,
                isComplex: !!dataType,
                value: '',
                fields: [],
            };

            if (dataType && dataType.properties) {
                group.fields = Object.keys(dataType.properties).map(fieldName => {
                    const fieldDef = dataType.properties[fieldName];
                    return {
                        name: fieldName,
                        type: fieldDef.type || 'string',
                        description: fieldDef.description || '',
                        required: !!fieldDef.required,
                        value: '',
                    };
                });
            }

            this.inputGroups.push(group);
        });
    }

    onFieldChange() {
        this.buildPayload();
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

    private buildPayload() {
        const payload = this.defaultPayload();
        payload.actionIdentifiers.blueprintName = this.blueprintName;
        payload.actionIdentifiers.blueprintVersion = this.blueprintVersion;
        payload.actionIdentifiers.actionName = this.actionName;

        if (this.actionName) {
            const requestKey = this.actionName + '-request';
            const requestObj: any = {};

            this.inputGroups.forEach(group => {
                if (group.isComplex) {
                    const nested: any = {};
                    group.fields.forEach(field => {
                        nested[field.name] = this.convertValue(field.value, field.type);
                    });
                    requestObj[group.inputName] = nested;
                } else {
                    requestObj[group.inputName] = this.convertValue(group.value, group.type);
                }
            });

            payload.payload[requestKey] = requestObj;
        }

        this.payloadText = JSON.stringify(payload, null, 2);
    }

    private convertValue(value: string, type: string): any {
        if (!value) {
            return (type === 'integer') ? 0 : (type === 'boolean') ? false : '';
        }
        if (type === 'integer') {
            const num = parseInt(value, 10);
            return isNaN(num) ? 0 : num;
        }
        if (type === 'boolean') {
            return value === 'true';
        }
        return value;
    }

    resetPayload() {
        this.inputGroups.forEach(group => {
            group.value = '';
            group.fields.forEach(field => { field.value = ''; });
        });
        this.buildPayload();
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
