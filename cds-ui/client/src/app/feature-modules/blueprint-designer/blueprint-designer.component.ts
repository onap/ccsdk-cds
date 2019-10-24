/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 IBM Intellectual Property. All rights reserved.
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

import { Component, OnInit } from '@angular/core';
import * as $ from 'jquery';
import * as _ from 'lodash';
import * as joint from '../../../../node_modules/jointjs/dist/joint.js';

@Component({
  selector: 'app-blueprint-designer',
  templateUrl: './blueprint-designer.component.html',
  styleUrls: ['./blueprint-designer.component.scss']
})
export class BlueprintDesignerComponent implements OnInit {

  public graph: any;
  public paper: any;

  constructor() { }

  ngOnInit() {
    // this.createGraph();
  }

  createGraph() {
    this.graph = new joint.dia.Graph,
      this.paper = new joint.dia.Paper({
        el: $('#paper'),
        model: this.graph,
        height: 700,
        width: 1000,
        gridSize: 2,
        drawGrid: true
      });

      this.paper = new joint.dia.Paper({
      el: document.getElementById('paper'),
      width: 1000,
      height: 1000,
      model: this.graph,
      gridSize: 2,
      drawGrid: true
    });

    this.paper.setGrid({
      name: 'dot',
      args:
        { color: 'black', thickness: 2, scaleFactor: 8 }

    }).drawGrid();
  }

}
