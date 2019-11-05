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
import { Component, OnInit, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material'; 
import { Store } from '@ngrx/store';
import { Observable } from 'rxjs';

import { ICatalog } from 'src/app/common/core/store/models/catalog.model'; 
import { ICatalogState } from 'src/app/common/core/store/models/catalogState.model';
import { IAppState } from 'src/app/common/core/store/state/app.state';

@Component({
  selector: 'app-catalog-data-dialog',
  templateUrl: './catalog-data-dialog.component.html',
  styleUrls: ['./catalog-data-dialog.component.scss']
})
export class CatalogDataDialogComponent implements OnInit{

  catalog:any=[];
  
  CatalogFormData: FormGroup;
  ccState: Observable<ICatalogState>;
  isDisabled: boolean=true;
  optionSelected:string;
  // derivedFrom: any[] = [{derivedFrom: 'tosca.nodes.Component'},{derivedFrom:'tosca.nodes.VNF'},{derivedFrom:'tosca.nodes.Artifact'},{derivedFrom:'tosca.nodes.ResourceSource'}, {derivedFrom:'tosca.nodes.Workflow'},{derivedFrom:'tosca.nodes.Root'}];
  // definitionType: any[] = [{definitionType: 'node_type'}];
  property:any=[];
  constructor(public dialogRef: MatDialogRef<CatalogDataDialogComponent>, @Inject(MAT_DIALOG_DATA) public item: any,private formBuilder: FormBuilder, private store: Store<IAppState> ) {
    console.log(item);
    this.optionSelected=item['option'];
    for (let key in item['item']) {
      this.catalog.push(item['item'][key]);
    }
    console.log(this.catalog);
    for (let key in this.catalog) {
      this.property.push(this.catalog[key]);  
    }
    if(this.optionSelected == 'Info'){
      this.isDisabled = true;
    }
    else{
      this.isDisabled = false;
    }

    this.ccState = this.store.select('catalog');
    this.CatalogFormData = this.formBuilder.group({
      Model_Name: [{value:this.property[0], disabled: this.isDisabled}, Validators.required],
      User_id: [{value:this.property[8], disabled: this.isDisabled}, Validators.required],
      _tags: [{value:this.property[6], disabled: this.isDisabled}, Validators.required],
      _type: [{value:this.property[2], disabled: this.isDisabled}, Validators.required],
      Derived_From: [{value:this.property[1], disabled: this.isDisabled}, Validators.required],
    });   
  }
  
  ngOnInit(){
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  onClickSave(){
    //this.catalog = Object.assign({}, this.CatalogFormData.value);
    this.dialogRef.close(this.CatalogFormData.value);
  }
}
