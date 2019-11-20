import {TestBed} from '@angular/core/testing';

import {SearchByTagsService} from './search-by-tags.service';

describe('SearchByTagsService', () => {
    beforeEach(() => TestBed.configureTestingModule({}));

    it('should be created', () => {
        const service: SearchByTagsService = TestBed.get(SearchByTagsService);
        expect(service).toBeTruthy();
    });
});
