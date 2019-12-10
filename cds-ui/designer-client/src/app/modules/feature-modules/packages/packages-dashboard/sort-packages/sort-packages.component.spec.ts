import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SortPackagesComponent } from './sort-packages.component';

describe('SortPackagesComponent', () => {
  let component: SortPackagesComponent;
  let fixture: ComponentFixture<SortPackagesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SortPackagesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SortPackagesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
