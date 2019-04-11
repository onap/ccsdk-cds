/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
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
import { LoopbackConfig } from '../../../../common/constants/app-constants';
import { saveAs } from 'file-saver';

@Injectable()
export class EditorService {
    // blueprintUrl = '../../constants/blueprint.json';

    constructor(private _http: HttpClient, private api: ApiService) {
    }

    enrich(uri: string, body: FormData): Observable<any> {
        return this.api.post(LoopbackConfig.url + uri, body, { responseType: 'blob' });
    }
    downloadCBA(uri: string): string {
        this.api.get(LoopbackConfig.url + uri, { responseType: 'blob' })
            .subscribe(response => {
                let blob = new Blob([response], { 'type': "application/octet-stream" });
                saveAs(blob, "CBA.zip");
            });
        return "Download Success";

    }
    post(uri: string, body: any | null, options?: any): Observable<any> {

        return this.api.post(LoopbackConfig.url + uri, body, options);
    }

    deployPost(uri: string, body: any | null, options?: any): Observable<any> {

        return this.api.post(LoopbackConfig.url + uri, body, { responseType: 'text' });
    }
}