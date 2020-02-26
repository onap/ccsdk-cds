import {Injectable} from '@angular/core';
import {ApiService} from '../../../../common/core/services/api.typed.service';
import {BlueprintURLs} from '../../../../common/constants/app-constants';
import {Observable} from 'rxjs';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import * as JSZip from 'jszip';

@Injectable({
    providedIn: 'root'
})
export class ConfigurationDashboardService {

    private zipFile: JSZip = new JSZip();

    constructor(private api: ApiService<BluePrintDetailModel>) {

    }

    getBluePrintModel(id: string): Observable<BluePrintDetailModel> {
        return this.api.getOne(BlueprintURLs.getOneBlueprint + '/' + id);
    }


    public downloadResource(id: string): string {
        this.api.getCustomized(id, {responseType: 'blob'})
            .subscribe(response => {
                const blob = new Blob([response], {type: 'application/octet-stream'});
                this.zipFile.loadAsync(blob).then((zip) => {
                    Object.keys(zip.files).forEach((filename) => {
                        zip.files[filename].async('string').then((fileData) => {
                            if (fileData) {
                                console.log(filename);
                                console.log(fileData);
                            }
                        });
                    });
                });
                // saveAs(blob, 'CBA.zip');
            });
        return 'Download Success';
    }

}
