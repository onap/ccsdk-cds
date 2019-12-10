import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PackagesHeaderComponent } from './packages-header.component';

describe('PackagesHeaderComponent', () => {
  let component: PackagesHeaderComponent;
  let fixture: ComponentFixture<PackagesHeaderComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PackagesHeaderComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PackagesHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
