import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { PackagesStore } from '../../packages.store';

@Component({
  selector: 'app-package-pagination',
  templateUrl: './package-pagination.component.html',
  styleUrls: ['./package-pagination.component.css'],
})
export class PackagePaginationComponent implements OnInit {
  pageNumber = 0;
  totalCount = 4;
  pageSize: number;

  constructor(private packagesStore: PackagesStore) {
    this.pageSize = packagesStore.pageSize;
   }

  ngOnInit() {
  }

  public getPageFromService(page) {
    console.log('getPageFromService', page);
    if (isNaN(page)) {
      page = 1;
      console.log('page change to first...', page);
    }
    this.packagesStore.getPagedPackages(page - 1, this.packagesStore.pageSize);
  }

}
