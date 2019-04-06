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

import { Component, OnInit, ViewChild, Input } from '@angular/core';
import { MatPaginator, MatTableDataSource, MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { SearchDialog } from '../../../../common/shared/components/search-dialog/search-dialog.component';
import { ResourceMappingService } from './resource-mapping.service';


@Component({
  selector: 'app-resource-mapping',
  templateUrl: './resource-mapping.component.html',
  styleUrls: ['./resource-mapping.component.scss']
})
export class ResourceMappingComponent {

  @Input('paramData') paramData: any;
  dialogRef: any;
  animal: string;
  name: string;
  selectedParam: any;

  resorceDictionaryName: string = '';

  constructor(public dialog: MatDialog, private resourceMappingService: ResourceMappingService) {
  }

  openDialog(paramValue): void {
    const dialogRef = this.dialog.open(SearchDialog, {
      width: '250px',
      data: { name: paramValue, animal: this.animal }
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
      this.animal = result;
    });
  }

  getResourceDictionaryByName(param) {
    let popup;
    this.selectedParam = param;
    this.resourceMappingService.getResourceDictionaryByName(this.resorceDictionaryName)
      .subscribe(dictionaryObj => {
        popup = this.dialog.open(SearchDialog, {
          width: '250px',
          data: { name: dictionaryObj, animal: this.animal }
        })
      },
        error => {
          console.log(error);
        })
        popup.afterClosed().subscribe(result=>{
          this.paramData.resourceAccumulatorResolvedData.forEach(element => {
            if(element.id == this.selectedParam) {
              // element.
            }
          });
        });
  }

}
