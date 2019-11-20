import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PackageListComponent } from './package-list.component';
import { PackagesStore } from '../../packages.store';
import { getBluePrintPageMock } from '../../blueprint.page.mock';
import { of } from 'rxjs';

describe('PackageListComponent', () => {
  let component: PackageListComponent;
  let fixture: ComponentFixture<PackageListComponent>;
  let store: Partial<PackagesStore>;

  beforeEach(async(() => {

    store = { state$: of(getBluePrintPageMock()) };

    TestBed.configureTestingModule({
      declarations: [ PackageListComponent ],
      providers: [{ provide: PackagesStore, useValue: store }]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PackageListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  // TODO create another test with store in mind
});
