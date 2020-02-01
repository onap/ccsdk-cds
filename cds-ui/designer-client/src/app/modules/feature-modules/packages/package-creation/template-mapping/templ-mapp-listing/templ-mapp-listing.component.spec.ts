import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplMappListingComponent } from './templ-mapp-listing.component';

describe('TemplMappListingComponent', () => {
  let component: TemplMappListingComponent;
  let fixture: ComponentFixture<TemplMappListingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplMappListingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplMappListingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
