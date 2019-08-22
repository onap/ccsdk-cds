/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/


import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, observable } from 'rxjs';
import { ApiService } from '../../../../common/core/services/api.service';
import { saveAs } from 'file-saver';
import { BlueprintURLs } from '../../../../common/constants/app-constants';
import { NotificationHandlerService } from 'src/app/common/core/services/notification-handler.service';

@Injectable()
export class EditorService {
    constructor(private _http: HttpClient, private api: ApiService,
        private alertService: NotificationHandlerService,) {
    }

    enrich(body: FormData): Observable<any> {
        return this.api.post(BlueprintURLs.enrich, body, { responseType: 'blob' });
    }
    downloadCBA(artifactDetails: string): string {
        this.api.get(BlueprintURLs.download+artifactDetails, { responseType: 'blob' })
            .subscribe(response => {
                let blob = new Blob([response], { 'type': "application/octet-stream" });
                saveAs(blob, "CBA.zip");
                this.alertService.success('Blueprint downloaded successfully' );
            });
        return "Download Success";

    }
    saveBlueprint(body: any | null, options?: any): Observable<any> {

        return this.api.post(BlueprintURLs.save, body, options);
    }
    publishBlueprint(body: any | null, options?: any): Observable<any> {

        return this.api.post(BlueprintURLs.publish, body, options);
    }

    deployPost(body: any | null, options?: any): Observable<any> {

        return this.api.post(BlueprintURLs.deploy, body, { responseType: 'text' });
    }
}