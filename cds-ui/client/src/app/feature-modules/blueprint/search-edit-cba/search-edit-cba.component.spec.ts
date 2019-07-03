import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchEditCBAComponent } from './search-edit-cba.component';

describe('SearchEditCBAComponent', () => {
  let component: SearchEditCBAComponent;
  let fixture: ComponentFixture<SearchEditCBAComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchEditCBAComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchEditCBAComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
