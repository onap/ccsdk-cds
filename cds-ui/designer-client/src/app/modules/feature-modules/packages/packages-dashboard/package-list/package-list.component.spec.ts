import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PackageListComponent } from './package-list.component';
import { PackagesStore } from '../../packages.store';
import { getBlueprintPageMock } from '../../blueprint.page.mock';
import { of } from 'rxjs';
import {PackagesDashboardState} from '../../model/packages-dashboard.state';

describe('PackageListComponent', () => {
  let component: PackageListComponent;
  let fixture: ComponentFixture<PackageListComponent>;
  let store: Partial<PackagesStore>;

  beforeEach(async(() => {

    const dashBoard = new PackagesDashboardState();
    dashBoard.page = getBlueprintPageMock();
    store = { state$: of(dashBoard) };

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
