import { Component, OnInit, ViewEncapsulation } from '@angular/core';

@Component({
  selector: 'app-designer',
  templateUrl: './designer.component.html',
  styleUrls: ['./designer.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class DesignerComponent implements OnInit {

  private controllerSideBar: boolean;
  private attributesSideBar: boolean;
  constructor() {
    this.controllerSideBar = true;
    this.attributesSideBar = false;
  }
  private _toggleSidebar1() {
    this.controllerSideBar = !this.controllerSideBar;
  }
  private _toggleSidebar2() {
    this.attributesSideBar = !this.attributesSideBar;
  }


  ngOnInit() {
  }
}
