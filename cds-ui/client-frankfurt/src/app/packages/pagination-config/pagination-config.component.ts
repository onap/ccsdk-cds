import { Component, OnInit } from '@angular/core';
import {NgbPaginationConfig} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-pagination-config',
  templateUrl: './pagination-config.component.html',
  styleUrls: ['./pagination-config.component.css'],
  providers: [NgbPaginationConfig] // add NgbPaginationConfig to the component providers

})
export class PaginationConfigComponent implements OnInit {

  constructor(config: NgbPaginationConfig) {
    config.size = 'sm';
    config.boundaryLinks = true;
   }

  ngOnInit() {
  }

}


