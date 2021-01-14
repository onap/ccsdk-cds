/*
* ============LICENSE_START=======================================================
* ONAP : CDS
* ================================================================================
* Copyright (C) 2020 TechMahindra
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
import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { DictionaryCreationStore } from './dictionary-creation.store';
import { DictionaryModel } from '../model/dictionary.model';
import { Definition } from '../model/definition.model';
import { DictionaryMetadataComponent } from './dictionary-metadata/dictionary-metadata.component';
import { SourcesTemplateComponent } from './sources-template/sources-template.component';
import { DictionaryCreationService } from './dictionary-creation.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-resource-dictionary-creation',
  templateUrl: './resource-dictionary-creation.component.html',
  styleUrls: ['./resource-dictionary-creation.component.css']
})
export class ResourceDictionaryCreationComponent implements OnInit {

  constructor(
    private router: Router,
    private dictionaryCreationStore: DictionaryCreationStore,
    private dictionaryService: DictionaryCreationService,
    private toaster: ToastrService
  ) {
  }

  modes: object[] = [
    { name: 'Designer Mode', style: 'mode-icon icon-designer-mode' },
    { name: 'Scripting Mode', style: 'mode-icon icon-scripting-mode' }
  ];

  metaDataTab: DictionaryModel = new DictionaryModel();
  definition: Definition = new Definition();
  createDate = '';

  @ViewChild(DictionaryMetadataComponent, { static: false })
  private metadataTabComponent: DictionaryMetadataComponent;

  @ViewChild(SourcesTemplateComponent, { static: false })
  private sourcesTemplateComponent: SourcesTemplateComponent;

  @ViewChild('nameit', { static: true })
  private elementRef: ElementRef;

  ngOnInit() {
    this.elementRef.nativeElement.focus();
    // this.elementRef2.nativeElement.focus();
  }

  saveDictionaryToStore() {
    this.metadataTabComponent.saveMetaDataToStore();
    // console.log('00000000000');
    // this.dictionaryCreationStore.getSources();
    // this.dictionaryCreationStore.state$.subscribe(dd => {
    //   console.log(dd);
    // });
  }

  createDictionary() {

    console.log('-----');
    this.metadataTabComponent.saveMetaDataToStore();
    this.dictionaryCreationStore.state$.subscribe(res => {
      console.log('---------------------------------------');
      console.log(res);
      this.dictionaryService.save(res.metaData).subscribe(data => {
        this.toaster.success('Dictionary Resource created');
      }, err => {
        this.toaster.error('An error happened ...');
      }, () => {

      });
    }).unsubscribe();
    // this.sourcesTemplateComponent.saveSorcesDataToStore();
  }

  goBackToDashBorad() {
    this.router.navigate(['/resource-dictionary']);
  }

}
