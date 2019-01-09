/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018 IBM Intellectual Property. All rights reserved.
===================================================================

Unless otherwise specified, all software contained herein is licensed
under the Apache License, Version 2.0 (the License);
you may not use this software except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
============LICENSE_END============================================
*/
import { Component, OnInit, Input, Output, EventEmitter, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { MatStepper } from '@angular/material';

import { GlobalContants } from '../../../constants/app-constants';

@Component({
  selector: 'app-cbawizard',
  templateUrl: './cbawizard.component.html',
  styleUrls: ['./cbawizard.component.scss']
})
export class CBAWizardComponent implements OnInit {
  @Input() stepsRequired: any[];
  @ViewChild('stepper') stepper: MatStepper;
  @Output() stepChanged = new EventEmitter();
  public stepDetails = GlobalContants.cbawizard.stepsRequired.steps;
  private routeLinks : any[];
  activeLinkIndex = -1;


  constructor(private router: Router) {
    this.routeLinks = [
      {
          label: 'CBA Metadata',
          link: '/blueprint/selectTemplate',
          index: 0
      }, {
          label: 'Controller Blueprint Designer',
          link: '/blueprint/modifyTemplate',
          index: 1
      }, {
          label: 'Test',
          link: '/blueprint/testTemplate',
          index: 2
      }, {
          label: 'Deploy',
          link: '/blueprint/deployTemplate',
          index: 3
      }
  ];

  }

  ngOnInit() {
  //   this.router.events.subscribe((res) => {
  //     this.activeLinkIndex = this.routeLinks.indexOf(this.routeLinks.find(tab => tab.link === this.router.url));
  //     this.stepper.selectedIndex = this.activeLinkIndex; 
  // });
   this.stepsRequired.forEach((step, index)=>{
    if(step.link == this.router.url) {
      this.stepper.selectedIndex = step.index
    }
   });
  }

  changeRoute(event){
    this.stepsRequired.forEach((step, index)=>{
      if(index == event.selectedIndex) {
        this.router.navigate([step.link]);
      }
    });
  }

}
