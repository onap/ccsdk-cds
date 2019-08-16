import { TestBed } from '@angular/core/testing';

import { SelectTemplateService } from './select-template.service';

describe('SelectTemplateService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: SelectTemplateService = TestBed.get(SelectTemplateService);
    expect(service).toBeTruthy();
  });
});
