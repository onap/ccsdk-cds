import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PaginationConfigComponent } from './pagination-config.component';

describe('PaginationConfigComponent', () => {
  let component: PaginationConfigComponent;
  let fixture: ComponentFixture<PaginationConfigComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PaginationConfigComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PaginationConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
