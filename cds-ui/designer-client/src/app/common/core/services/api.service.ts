/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018 IBM Intellectual Property. All rights reserved.

Modifications Copyright (C) 2019 Orange

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

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse, HttpHeaderResponse, HttpParams} from '@angular/common/http';
import {Observable, of} from 'rxjs';

@Injectable()
export class ApiService {

    constructor(private httpClient: HttpClient) {
    }

    get(url: string, params?: {}): Observable<any> {
        console.log('params', params);
        let httpParams = new HttpParams();
        for (const key in params) {
            if (params.hasOwnProperty(key)) {
                httpParams = httpParams.append(key, params[key]);
            }
        }
        const options = {params: httpParams};
        return this.httpClient.get(url, options);
    }

    post(url: string, body: any | null, options?: any): Observable<any> {

        return this.httpClient.post(url, body, options);
    }

    getCustomized(url: string, params?: any): Observable<any> {
        return this.httpClient.get(url, params);
    }
}
