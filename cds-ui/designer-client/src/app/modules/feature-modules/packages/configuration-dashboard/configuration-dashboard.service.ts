import { Injectable } from '@angular/core';
import { ApiService } from '../../../../common/core/services/api.typed.service';
import { BlueprintURLs } from '../../../../common/constants/app-constants';
import { Observable } from 'rxjs';
import { BluePrintDetailModel } from '../model/BluePrint.detail.model';


@Injectable({
    providedIn: 'root'
})
export class ConfigurationDashboardService {
    constructor(private api: ApiService<BluePrintDetailModel>) {

    }

    private getBluePrintModel(id: string): Observable<BluePrintDetailModel> {
        return this.api.getOne(BlueprintURLs.getOneBlueprint + '/' + id);
    }

    getPagedPackages(id: string) {
        return this.getBluePrintModel(id);
    }

    public downloadResource(path: string) {
        return this.api.getCustomized(BlueprintURLs.download + path, { responseType: 'blob' });
    }

    deployPost(body: any | null): Observable<any> {
        return this.api.post(BlueprintURLs.deploy, body, { responseType: 'text' });
    }
    deletePackage(id: string) {
        return this.api.delete(BlueprintURLs.getOneBlueprint + '/' + id, { observe: 'response' });
    }
}
