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

import { Component, OnInit, ViewChild, ɵConsole } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { JsonEditorComponent, JsonEditorOptions } from 'ang-jsoneditor';
import { Observable } from 'rxjs';
import { ICatalogState } from 'src/app/common/core/store/models/catalogState.model';
import { ICatalog } from 'src/app/common/core/store/models/catalog.model';
import { Store } from '@ngrx/store';
import { IAppState } from 'src/app/common/core/store/state/app.state';
import { SetCatalogState } from 'src/app/common/core/store/actions/catalog.action';
import { CreateCatalogService } from './create-catalog.service';
import { NotificationHandlerService } from 'src/app/common/core/services/notification-handler.service';

@Component({
  selector: 'app-create-catalog',
  templateUrl: './create-catalog.component.html',
  styleUrls: ['./create-catalog.component.scss']
})
export class CreateCatalogComponent implements OnInit {
  
  CatalogFormData: FormGroup;
  @ViewChild(JsonEditorComponent) editor: JsonEditorComponent;
  options = new JsonEditorOptions();
  data:any;
  derivedFrom: any[] = [{derivedFrom: 'tosca.nodes.Component'},{derivedFrom:'tosca.nodes.VNF'},{derivedFrom:'tosca.nodes.Artifact'},{derivedFrom:'tosca.nodes.ResourceSource'}, {derivedFrom:'tosca.nodes.Workflow'},{derivedFrom:'tosca.nodes.Root'}];
  definitionType: any[] = [{definitionType: 'node_type'}];
  ccState: Observable<ICatalogState>;
  catalog: ICatalog;

  constructor(private formBuilder: FormBuilder, private store: Store<IAppState>, private catalogCreateService: CreateCatalogService, private alertService: NotificationHandlerService) { 
    this.ccState = this.store.select('catalog');
    this.CatalogFormData = this.formBuilder.group({
      Model_Name: ['', Validators.required],
      User_id: ['', Validators.required],
      _tags: ['', Validators.required],
      _type: ['', Validators.required],
      Derived_From: ['', Validators.required],
      _description : ['', Validators.required]
    });   
  }
  ngOnInit() {
    this.options.mode = 'text';
    this.options.modes = [ 'text', 'tree', 'view'];
    this.options.statusBar = false; 

    this.ccState.subscribe(
      catalogdata => {
        var catalogState: ICatalogState = { catalog: catalogdata.catalog, isLoadSuccess: catalogdata.isLoadSuccess, isSaveSuccess: catalogdata.isSaveSuccess, isUpdateSuccess: catalogdata.isUpdateSuccess };
        this.catalog = catalogState.catalog;
        console.log( this.catalog );
      });

//    this.catalogCreateService.getDefinition()
//    .subscribe(data=>{
//        console.log(data);
//        data.forEach(element => {
//          this.definitionType.push(element)
//        });          
//    }, error=>{
//      window.alert('error' + error);
//    })
//
//    this.catalogCreateService.getDerivedFrom()
//    .subscribe(data=>{
//        console.log(data);
//        data.forEach(element => {
//          this.derivedFrom.push(element)
//        });          
//    }, error=>{
//      window.alert('error' + error);
//    })
  }
  CreateCatalog(){
    this.catalog = Object.assign({}, this.CatalogFormData.value);
    this.catalog.definition=this.data;
    let catalogState = {
      catalog: this.catalog
    }
    this.store.dispatch(new SetCatalogState(catalogState));
    
      this.catalogCreateService.saveCatalog(this.catalog)
      .subscribe(response=>{
        this.alertService.success("save success")
      },
      error=>{
        this.alertService.error('Error saving resources');
      })
 
  }

  onChange($event) {
    this.data=JSON.parse($event.srcElement.value); 
    console.log(this.data);
  };
}
