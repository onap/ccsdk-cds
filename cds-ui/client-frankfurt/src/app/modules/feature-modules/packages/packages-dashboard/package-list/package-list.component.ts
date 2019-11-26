import { Component, OnInit } from '@angular/core';
import { BlueprintModel } from '../../model/BluePrint.model';
import { PackagesStore } from '../../packages.store';

@Component({
  selector: 'app-packages-list',
  templateUrl: './package-list.component.html',
  styleUrls: ['./package-list.component.css']
})
export class PackageListComponent implements OnInit {

  viewedPackages: BlueprintModel[] = [];
  numberOfPackages: number;


  constructor(private packagesStore: PackagesStore) {
    console.log('PackageListComponent');
    this.packagesStore.state$.subscribe(page => {
      console.log(page);
      this.viewedPackages = page.content;
    });
  }

  ngOnInit() {
    // this.packagesStore.getPagedPackages(0, this.packagesStore.pageSize);
  }

}
