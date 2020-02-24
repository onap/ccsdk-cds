import {Injectable} from '@angular/core';
import {ApiService} from '../../../../common/core/services/api.typed.service';
import {BlueprintURLs} from '../../../../common/constants/app-constants';
import {Observable} from 'rxjs';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';


@Injectable({
    providedIn: 'root'
})
export class ConfigurationDashboardService {


    constructor(private api: ApiService<BluePrintDetailModel>) {

    }

    getBluePrintModel(id: string): Observable<BluePrintDetailModel> {
        return this.api.getOne(BlueprintURLs.getOneBlueprint + '/' + id);
    }
}
