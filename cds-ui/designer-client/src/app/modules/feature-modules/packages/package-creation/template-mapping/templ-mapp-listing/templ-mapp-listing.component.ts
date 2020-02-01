import { Component, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-templ-mapp-listing',
  templateUrl: './templ-mapp-listing.component.html',
  styleUrls: ['./templ-mapp-listing.component.css']
})
export class TemplMappListingComponent implements OnInit {
  @Output() showCreationViewParentNotification = new EventEmitter<any>();

  constructor() { }

  ngOnInit() {
  }

  openCreationView() {
    this.showCreationViewParentNotification.emit('tell parent to open create views');
  }

}
