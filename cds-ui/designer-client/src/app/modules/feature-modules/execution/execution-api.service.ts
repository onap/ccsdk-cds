import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../../common/core/services/api.typed.service';
import { ExecutionURLs, BlueprintURLs } from '../../../common/constants/app-constants';
import { BluePrintPage } from '../packages/model/BluePrint.model';

@Injectable({
    providedIn: 'root',
})
export class ExecutionApiService {

    constructor(private api: ApiService<any>) {
    }

    executeBlueprint(payload: any): Observable<any> {
        return this.api.post(ExecutionURLs.execute, payload);
    }

    getPagedPackages(pageNumber: number, pageSize: number, sortBy: string): Observable<BluePrintPage[]> {
        const sortType = sortBy.includes('DATE') ? 'DESC' : 'ASC';
        return this.api.get(BlueprintURLs.getPagedBlueprints, {
            offset: pageNumber,
            limit: pageSize,
            sort: sortBy,
            sortType,
        });
    }

    getBlueprintByNameAndVersion(name: string, version: string): Observable<any> {
        return this.api.get(BlueprintURLs.getBlueprintByName + name + '/version/' + version);
    }

    getWorkflows(name: string, version: string): Observable<any> {
        return this.api.getCustomized(BlueprintURLs.getWorkflows + name + '/' + version);
    }

    getWorkflowSpec(name: string, version: string, workflowName: string): Observable<any> {
        return this.api.post(BlueprintURLs.getWorkflowSpec, {
            blueprintName: name,
            version,
            workflowName,
        });
    }
}
