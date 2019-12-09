import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PackagesSearchComponent } from './search-by-packages.component';

describe('PackagesSearchComponent', () => {
  let component: PackagesSearchComponent;
  let fixture: ComponentFixture<PackagesSearchComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [PackagesSearchComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PackagesSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
