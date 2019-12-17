import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigurationDashboardComponent } from './configuration-dashboard.component';

describe('PackageViewingComponent', () => {
  let component: ConfigurationDashboardComponent;
  let fixture: ComponentFixture<ConfigurationDashboardComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfigurationDashboardComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigurationDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
