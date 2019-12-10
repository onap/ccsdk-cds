import { Component, OnInit } from '@angular/core';
import { PackagesStore } from '../../packages.store';

@Component({
  selector: 'app-packages-header',
  templateUrl: './packages-header.component.html',
  styleUrls: ['./packages-header.component.css']
})
export class PackagesHeaderComponent implements OnInit {

  numberOfPackages: number;

  constructor(private packagesStore: PackagesStore) {
    this.packagesStore.state$
      .subscribe(state => {
        this.numberOfPackages = state.totalPackagesWithoutSearchorFilters;
      });
   }

  ngOnInit() {
  }

}
