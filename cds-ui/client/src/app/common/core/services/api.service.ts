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
import { HttpClient, HttpHeaders, HttpResponse, HttpHeaderResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LoopbackConfig } from '../../constants/app-constants';

@Injectable()
export class ApiService {
  
  constructor(private _http: HttpClient) {
  }
  enrich(uri: string, body: FormData): Observable<any> {
    
    var HTTPOptions = {
      headers: new HttpHeaders({ 'Accept': 'application/zip', }),
      observe: "response" as 'body',// to display the full response & as 'body' for type cast
      'responseType': 'blob' as 'json'
    }
    return this._http.post(LoopbackConfig.url + uri, body, HTTPOptions);

  }
  downloadCBA(uri: string, params?: any): Observable<Blob> {
    // return this._http.get<Blob>(LoopbackConfig.url+uri);
    var HTTPOptions = {
      headers: new HttpHeaders({ 'Accept': 'application/zip; charset=UTF-8', }),
      observe: "response" as 'body',// to display the full response & as 'body' for type cast
      'responseType': 'blob' as 'json'
    }
    return this._http.get<Blob>(LoopbackConfig.url + uri, HTTPOptions);

  }

  post(uri: string, body: FormData): Observable<any> {
    // to do
    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': LoopbackConfig.authtoken,

      })
    };
    return this._http.post(LoopbackConfig.url + uri, body, httpOptions);
  }

  put() {
    // to do
  }

  delete() {
    // to do
  }

}