import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SourceEditorComponent } from './source-editor.component';

describe('SourceEditorComponent', () => {
  let component: SourceEditorComponent;
  let fixture: ComponentFixture<SourceEditorComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SourceEditorComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SourceEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
