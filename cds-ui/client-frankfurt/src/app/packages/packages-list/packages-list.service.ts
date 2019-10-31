import {Component, OnInit} from '@angular/core';

import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ApiService} from '../../common/core/services/api.service';
import {BlueprintURLs} from '../../common/constants/app-constants';

@Injectable({
    providedIn: 'root'
})
 export class PackagesListService {

    constructor(private httpClient: HttpClient, private api: ApiService) {
    }

    getAllPackages(): Observable<any> {
        return this.api.get(BlueprintURLs.getAllBlueprints);
    }

}
