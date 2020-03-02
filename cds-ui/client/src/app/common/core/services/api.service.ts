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

@Injectable()
export class ApiService {
  
  constructor(private _http: HttpClient) {
  }

  get(url: string, params?: any): Observable<any> {
    return this._http.get(url,params);
  }

  post(url: string, body: any | null, options?:any): Observable<any> {
    return this._http.post(url, body,options);
  }
  
  put() {
    // to do
  }

  delete(url: string, params?: any): Observable<any> {
    return this._http.delete(url,params);
  }

}