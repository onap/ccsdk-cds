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
import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';

import { DictionaryMetadataComponent } from './dictionary-metadata.component';
import { DictionaryCreationService } from '../dictionary-creation.service';
import { DictionaryCreationStore } from '../dictionary-creation.store';
import { MetaData } from '../../model/metaData.model';

describe('DictionaryMetadataComponent', () => {
  let component: DictionaryMetadataComponent;
  let fixture: ComponentFixture<DictionaryMetadataComponent>;
  let store: DictionaryCreationStore;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [DictionaryMetadataComponent],
      providers: [{ provide: DictionaryCreationService, useValue: {} }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    store = TestBed.get(DictionaryCreationStore);
    fixture = TestBed.createComponent(DictionaryMetadataComponent);
    component = fixture.componentInstance;
  });

  function nameInput(): HTMLInputElement {
    return fixture.nativeElement.querySelector('input[placeholder="Dictionary Name"]');
  }

  it('lets the user type a name for a new entry', async(() => {
    fixture.detectChanges();

    fixture.whenStable().then(() => expect(nameInput().readOnly).toBe(false));
  }));

  it('keeps the name of an existing entry read-only', async(() => {
    component.nameReadOnly = true;
    fixture.detectChanges();

    fixture.whenStable().then(() => expect(nameInput().readOnly).toBe(true));
  }));

  it('splits the tags of a loaded entry', () => {
    fixture.detectChanges();

    store.changeMetaData({ ...new MetaData(), tags: 'vnf, name,' });

    expect(Array.from(component.tags)).toEqual(['vnf', 'name']);
  });
});
