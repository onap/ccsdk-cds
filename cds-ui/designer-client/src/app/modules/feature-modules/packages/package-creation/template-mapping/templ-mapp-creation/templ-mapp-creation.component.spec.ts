import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplMappCreationComponent } from './templ-mapp-creation.component';

describe('TemplateMappingCreationComponent', () => {
  let component: TemplMappCreationComponent;
  let fixture: ComponentFixture<TemplMappCreationComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ TemplMappCreationComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplMappCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
