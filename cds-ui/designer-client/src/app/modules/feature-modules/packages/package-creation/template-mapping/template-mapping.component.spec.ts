import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplateMappingComponent } from './template-mapping.component';

describe('TemplateMappingComponent', () => {
  let component: TemplateMappingComponent;
  let fixture: ComponentFixture<TemplateMappingComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplateMappingComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplateMappingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
