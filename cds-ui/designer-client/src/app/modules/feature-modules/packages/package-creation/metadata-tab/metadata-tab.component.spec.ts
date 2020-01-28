import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataTabComponent } from './metadata-tab.component';

describe('MetadataTabComponent', () => {
  let component: MetadataTabComponent;
  let fixture: ComponentFixture<MetadataTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MetadataTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MetadataTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
