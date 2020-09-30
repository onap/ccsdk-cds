import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FunctionsAttributeComponent } from './functions-attribute.component';

describe('FunctionsAttributeComponent', () => {
  let component: FunctionsAttributeComponent;
  let fixture: ComponentFixture<FunctionsAttributeComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FunctionsAttributeComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FunctionsAttributeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
