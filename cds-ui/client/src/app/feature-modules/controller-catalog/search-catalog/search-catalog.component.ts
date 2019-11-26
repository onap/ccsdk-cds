/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2019 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/

import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog ,MatDialogRef } from '@angular/material';
import { MatAutocompleteTrigger } from '@angular/material';
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { SearchCatalogService } from './search-catalog.service';
import { CatalogDataDialogComponent } from './catalog-data-dialog/catalog-data-dialog.component';
import { ICatalog } from 'src/app/common/core/store/models/catalog.model'; 
import { CreateCatalogService } from '../create-catalog/create-catalog.service';
import { NotificationHandlerService } from 'src/app/common/core/services/notification-handler.service';
import { ICatalogState } from 'src/app/common/core/store/models/catalogState.model';
import { IAppState } from 'src/app/common/core/store/state/app.state';


@Component({
  selector: 'app-search-catalog',
  templateUrl: './search-catalog.component.html',
  styleUrls: ['./search-catalog.component.scss']
})
export class SearchCatalogComponent implements OnInit {
  myControl: FormGroup;
  searchText: string = '';
  
  options: any[] = [];
  private dialogRef: MatDialogRef<CatalogDataDialogComponent>;
  data: any;
  catalog: ICatalog;
  ccState: Observable<ICatalogState>;
  @ViewChild('catalogSelect', { read: MatAutocompleteTrigger }) catalogSelect: MatAutocompleteTrigger;
  
  constructor(private _formBuilder: FormBuilder, private store: Store<IAppState>, private searchCatalogService: SearchCatalogService, private dialog: MatDialog, private catalogCreateService: CreateCatalogService, private alertService: NotificationHandlerService)  { 
    this.ccState = this.store.select('catalog');
  }
 
 ngOnInit() {

  this.ccState.subscribe(
    catalogdata => {
      var catalogState: ICatalogState = { catalog: catalogdata.catalog, isLoadSuccess: catalogdata.isLoadSuccess, isSaveSuccess: catalogdata.isSaveSuccess, isUpdateSuccess: catalogdata.isUpdateSuccess };
      this.catalog = catalogState.catalog;
      console.log( this.catalog );
    });

    this.myControl = this._formBuilder.group({
      search_input: ['', Validators.required]
    });
  }
  fetchCatalogByName() {
    // this.searchCatalogService.searchByTags(this.searchText)
    // .subscribe(data=>{
    //     console.log(data);
    //     data.forEach(element => {
    //       this.options.push(element)
    //     });          
    //   this.catalogSelect.openPanel();
    // }, error=>{
    //   window.alert('Catalog not matching the search tag' + error);
    // })

    this.options=[  {
      "modelName": "tosca.nodes.Artifact",
      "derivedFrom": "tosca.nodes.Root",
      "definitionType": "node_type",
      "definition": {
        "description": "This is Deprecated Artifact Node Type.",
        "version": "1.0.0",
        "derived_from": "tosca.nodes.Root"
      },
      "description": "This is Deprecated Artifact Node Type.",
      "version": "1.0.0",
      "tags": "tosca.nodes.Artifact,tosca.nodes.Root,node_type",
      "creationDate": "2019-09-16T07:35:24.000Z",
      "updatedBy": "System"
    }];
   }

   editInfo(item: ICatalog, option: string) {

      this.dialogRef = this.dialog.open(CatalogDataDialogComponent, {
        height: '500px',
        width: '700px',
        disableClose: true,
        data: {item, option}
      });

      this.dialogRef.afterClosed().subscribe(result => {
        if(result == undefined || result == null){
          console.log("dialogbox is closed");
        }else{
        	this.catalog.modelName=result['modelName'];
            this.catalog.derivedFrom=result['derivedFrom'];
            this.catalog.definitionType=result['definitionType'];
            this.catalog.definition=result['definition'];
            this.catalog.tags=result['tags'];
            this.catalog.updatedBy=result['updatedBy'];
            console.log(this.catalog);
            this.catalogCreateService.saveCatalog(this.catalog)
            .subscribe(response=>{
              this.alertService.success("save success"+ response)
            },
	          error=>{
	          this.alertService.error('Error saving resources');
	          })  
        } 
      });
  }
}
