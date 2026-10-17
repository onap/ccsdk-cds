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
import { of } from 'rxjs';

import { SourcesTemplateComponent } from './sources-template.component';
import { SourcesStore } from './sources.store';
import { DictionaryCreationService } from '../dictionary-creation.service';
import { DictionaryCreationStore } from '../dictionary-creation.store';
import { MetaData } from '../../model/metaData.model';

describe('SourcesTemplateComponent', () => {
  let component: SourcesTemplateComponent;
  let store: DictionaryCreationStore;

  beforeEach(() => {
    const service = jasmine.createSpyObj<DictionaryCreationService>('DictionaryCreationService', ['getSources']);
    service.getSources.and.returnValue(of([{ input: 'source-input', default: 'source-default' }]));
    const sourcesStore = new SourcesStore(service);
    store = new DictionaryCreationStore(service, sourcesStore);
    component = new SourcesTemplateComponent(sourcesStore, service, store);
    component.ngOnInit();
  });

  function loadSources(sources: any) {
    store.changeMetaData({ ...new MetaData(), sources });
  }

  it('lists the sources of a loaded entry', () => {
    loadSources({ input: { type: 'source-input' }, default: { type: 'source-default', properties: {} } });

    expect(Array.from(component.sourcesOptions.keys())).toEqual(['input', 'default']);
    expect(component.sourcesOptions.get('input')).toEqual({ input: { type: 'source-input' } });
  });

  it('keeps the loaded sources when another source is added', () => {
    loadSources({ input: { type: 'source-input' } });

    component.tempSources.set('default', { name: 'default', value: 'source-default' });
    component.addSources();

    expect(store.state.metaData.sources).toEqual({
      input: { type: 'source-input' },
      default: { type: 'source-default', properties: {} }
    });
  });

  it('removes a deleted source from the entry', () => {
    loadSources({ input: { type: 'source-input' }, default: { type: 'source-default' } });

    component.toDeleteSource.set('input', {});
    component.deleteSource();

    expect(store.state.metaData.sources).toEqual({ default: { type: 'source-default' } });
  });
});
