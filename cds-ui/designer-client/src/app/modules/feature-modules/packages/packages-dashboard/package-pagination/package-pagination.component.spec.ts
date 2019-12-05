import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PackagePaginationComponent } from './package-pagination.component';

describe('PackagePaginationComponent', () => {
  let component: PackagePaginationComponent;
  let fixture: ComponentFixture<PackagePaginationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PackagePaginationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PackagePaginationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
