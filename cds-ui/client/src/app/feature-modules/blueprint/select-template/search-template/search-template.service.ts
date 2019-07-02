import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiService } from '../../../../common/core/services/api.service';

@Injectable({
  providedIn: 'root'
})
export class SearchTemplateService {

  constructor(private _http: HttpClient, private api: ApiService) { }

  searchByTags(uri: string, searchText: String): Observable<any>{
    return this.api.post(uri, searchText);
  }
}
