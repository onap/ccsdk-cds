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
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';

import { ResourceDictionaryCreationComponent } from './resource-dictionary-creation.component';
import { DictionaryCreationService } from './dictionary-creation.service';
import { DictionaryCreationStore } from './dictionary-creation.store';
import { MetaData } from '../model/metaData.model';

describe('ResourceDictionaryCreationComponent', () => {
  let fixture: ComponentFixture<ResourceDictionaryCreationComponent>;
  let component: ResourceDictionaryCreationComponent;
  let dictionaryService: jasmine.SpyObj<DictionaryCreationService>;
  let toaster: jasmine.SpyObj<ToastrService>;
  let router: jasmine.SpyObj<Router>;
  let store: DictionaryCreationStore;

  function createComponent(name: string | null, beforeCreate = () => {}) {
    TestBed.configureTestingModule({
      declarations: [ResourceDictionaryCreationComponent],
      providers: [
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: convertToParamMap(name ? { name } : {}) } } },
        { provide: Router, useValue: router },
        { provide: DictionaryCreationService, useValue: dictionaryService },
        { provide: ToastrService, useValue: toaster }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    });
    store = TestBed.get(DictionaryCreationStore);
    beforeCreate();
    fixture = TestBed.createComponent(ResourceDictionaryCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  function heading(): string {
    return fixture.nativeElement.querySelector('h1').textContent.trim();
  }

  function deleteButton(): HTMLButtonElement {
    return fixture.nativeElement.querySelector('button[data-target="#removeDictionaryModal"]');
  }

  beforeEach(() => {
    dictionaryService = jasmine.createSpyObj('DictionaryCreationService',
      ['getDictionaryByName', 'delete', 'save', 'getSources']);
    toaster = jasmine.createSpyObj('ToastrService', ['success', 'error']);
    router = jasmine.createSpyObj('Router', ['navigate']);
  });

  describe('without a dictionary name', () => {
    it('starts from empty metadata and offers no delete', () => {
      createComponent(null, () => store.changeMetaData({ ...new MetaData(), name: 'left over' }));

      expect(dictionaryService.getDictionaryByName).not.toHaveBeenCalled();
      expect(store.state.metaData.name).toBe('');
      expect(heading()).toBe('Dictionary Name');
      expect(deleteButton()).toBeNull();
    });
  });

  describe('with a dictionary name', () => {
    const loaded = { ...new MetaData(), name: 'hostname' };

    it('loads the entry into the store', () => {
      dictionaryService.getDictionaryByName.and.returnValue(of(loaded));
      createComponent('hostname');

      expect(dictionaryService.getDictionaryByName).toHaveBeenCalledWith('hostname');
      expect(store.state.metaData).toBe(loaded);
      expect(heading()).toBe('hostname');
      expect(deleteButton()).not.toBeNull();
    });

    it('reports an entry that cannot be loaded', () => {
      dictionaryService.getDictionaryByName.and.returnValue(throwError({ status: 500 }));
      createComponent('hostname');

      expect(toaster.error).toHaveBeenCalledWith('Could not load dictionary resource hostname');
    });

    it('deletes the entry and returns to the dashboard', () => {
      dictionaryService.getDictionaryByName.and.returnValue(of(loaded));
      dictionaryService.delete.and.returnValue(of(null));
      createComponent('hostname');

      component.deleteDictionary();

      expect(dictionaryService.delete).toHaveBeenCalledWith('hostname');
      expect(toaster.success).toHaveBeenCalledWith('Dictionary Resource deleted');
      expect(router.navigate).toHaveBeenCalledWith(['/resource-dictionary']);
    });

    it('stays on the page when the delete fails', () => {
      dictionaryService.getDictionaryByName.and.returnValue(of(loaded));
      dictionaryService.delete.and.returnValue(throwError({ status: 500 }));
      createComponent('hostname');

      component.deleteDictionary();

      expect(toaster.error).toHaveBeenCalledWith('Could not delete dictionary resource hostname');
      expect(router.navigate).not.toHaveBeenCalled();
    });
  });
});
