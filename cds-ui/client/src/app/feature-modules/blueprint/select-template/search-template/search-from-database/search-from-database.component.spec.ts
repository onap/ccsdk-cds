import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SearchFromDatabaseComponent } from './search-from-database.component';

describe('SearchFromDatabaseComponent', () => {
  let component: SearchFromDatabaseComponent;
  let fixture: ComponentFixture<SearchFromDatabaseComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SearchFromDatabaseComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchFromDatabaseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
