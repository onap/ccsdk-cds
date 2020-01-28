import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DslDefinitionsTabComponent } from './dsl-definitions-tab.component';

describe('DslDefinitionsTabComponent', () => {
  let component: DslDefinitionsTabComponent;
  let fixture: ComponentFixture<DslDefinitionsTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DslDefinitionsTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DslDefinitionsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
