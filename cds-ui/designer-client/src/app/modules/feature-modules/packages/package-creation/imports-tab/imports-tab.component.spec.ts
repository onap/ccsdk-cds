import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ImportsTabComponent } from './imports-tab.component';

describe('ImportsTabComponent', () => {
  let component: ImportsTabComponent;
  let fixture: ComponentFixture<ImportsTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ImportsTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ImportsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
