import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PackageCreationComponent } from './package-creation.component';

describe('PackageCreationComponent', () => {
  let component: PackageCreationComponent;
  let fixture: ComponentFixture<PackageCreationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ PackageCreationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PackageCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
