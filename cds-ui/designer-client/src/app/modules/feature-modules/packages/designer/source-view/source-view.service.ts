import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import { ApiService } from 'src/app/common/core/services/api.typed.service';
import { BlueprintDetailModel } from '../../model/Blueprint.detail.model';
import { ModelType } from '../model/ModelType.model';
import { ResourceDictionaryURLs, BlueprintURLs } from 'src/app/common/constants/app-constants';



@Injectable({
    providedIn: 'root'
})
export class SourceViewService {

    constructor(private api1: ApiService<BlueprintDetailModel>) {
    }


    private getBlueprintModel(id: string): Observable<BlueprintDetailModel> {
        return this.api1.getOne(BlueprintURLs.getOneBlueprint + '/' + id);
    }
    getPagedPackages(id: string) {
        return this.getBlueprintModel(id);
    }

}
