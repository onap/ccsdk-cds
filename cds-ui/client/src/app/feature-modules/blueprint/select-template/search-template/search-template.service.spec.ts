import { TestBed } from '@angular/core/testing';

import { SearchTemplateService } from './search-template.service';

describe('SearchTemplateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SearchTemplateService = TestBed.get(SearchTemplateService);
    expect(service).toBeTruthy();
  });
});
