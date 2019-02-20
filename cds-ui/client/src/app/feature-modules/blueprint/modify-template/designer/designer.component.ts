/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2018-19 IBM Intellectual Property. All rights reserved.
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

import { Component, OnInit, ViewChild, ElementRef, Output, EventEmitter } from '@angular/core';
import * as d3 from 'd3';
import { text } from 'd3';

@Component({
  selector: 'app-designer',
  templateUrl: './designer.component.html',
  styleUrls: ['./designer.component.scss']
})
export class DesignerComponent implements OnInit {
  @Output() onNodeSelect = new EventEmitter();
  @ViewChild('svgArea') graphContainer: ElementRef;
  dataForsimulation;
  svg;
  svgWidth;
  svgHeight;


  simulation;

  constructor() {    
    this.dataForsimulation = {"nodes" :[],
                              "links": []
                            }
    
   
    d3.json("../../../../../assets/activation-blueprint.json")
    .then((data)=>{
      console.log(data);
      this.buildD3DataNodes(data);
      //this.buildD3DataLinks(data);
      this.drawNode();
    });
   }

  ngOnInit() {
  }

  ngAfterContentInit()  {
    
  }

  drawNode() {
    this.svg = d3.select('#svgArea')
              .style('back-ground-color', 'white');
    
    this.svgWidth = this.svg._groups[0][0].width.baseVal.value;
    this.svgHeight = this.svg._groups[0][0].height.baseVal.value;

    console.log('width', this.svgWidth);

    let xbyMath;
    let ybyMath;
    let X= 10;
    let Y=10;

    let transformString = "translate(" + X + "," + Y + ")";
    this.dataForsimulation.nodes.forEach((d, i)=> {
      let id= 'g'+i;

      // xbyMath =  Math.random() * ( this.svgWidth - 50 -  105  ) + ( 105/2 + 10 );
      // ybyMath =  Math.random() * ( this.svgWidth - 20 -  100  ) + ( 100/2 + 10 );

      xbyMath = Math.floor(Math.random() * ((this.svgWidth-110) - 100 + 1)) + 100;
      ybyMath = Math.floor(Math.random() * ((this.svgHeight-110) - 100 + 1)) + 100;

      transformString =  "translate(" + xbyMath + "," + ybyMath + ")"; 
      
      let gEleId = 'g'+i;
      let nodeTemprectId = gEleId+name
      let requirement = gEleId+name+'requirement';
        this.svg.append('g')
          .attr('id', gEleId);
        
        let firstg = d3.select('#g'+i)
                    .attr('transform', transformString);

        firstg.append('rect')
              .attr('id', d.name)
              .attr("x", 0)
               .attr("y", 0)
               .attr("rx", 20)
                .attr("ry", 20)
                .attr('width', 100)
                .attr('height', 100)
                .attr('fill', 'white')
                 .attr('stroke', 'black')
                 .attr('opacity', 0.6)
                 .on('mouseover', () => this.handleMouseOver());

          d.x = xbyMath;
          d.y = ybyMath;
                
        firstg.append('circle')
                    .attr('cx', 97)
                    .attr('cy', 20)
                    .attr('r', 5)
                    .attr('fill', 'orange')

        if(d.requirementsArray) {
          d.requirementsArray.forEach(requirement =>{
            firstg.append('circle')
                      .attr('id', d.name+requirement.name)
                      .attr('cx', 97)
                      .attr('cy', 60)
                      .attr('r', 5)
                      .attr('fill', 'blue')
            requirement.x = xbyMath + 95;
            requirement.y = ybyMath + 60;
          });
        }

        if(d.capabilitiesArray) {
          d.capabilitiesArray.forEach(capability =>{
            firstg.append('circle')
                        .attr('id', d.name+capability.name)
                        .attr('cx', 97)
                        .attr('cy', 40)
                        .attr('r', 5)
                        .attr('fill', 'green');
            capability.x = xbyMath + 97;
            capability.y = ybyMath + 40;
          }); 
        }
          
        
        firstg.append('text')
                    .attr('x', 0)
                    .attr('y', 115)
                    .text(d.name);
          
       // X = X +120;
       // Y = 10;
    });
    this.buildD3DataLinks();
  }

  buildD3DataNodes(data) {
    let d3data;
    d3data = data.topology_template.node_templates;
    console.log('d3data:', d3data);
    let finalData = [];
    for (var property1 in d3data) {
      d3data[property1].name = property1;
      this.dataForsimulation.nodes.push(d3data[property1]);
      finalData.push(d3data[property1]);
    }

    this.dataForsimulation.nodes.forEach(node => {
      for( var nodeProperty in node) {
        if(nodeProperty == 'requirements' || nodeProperty == 'capabilities') {
            let arrayName = nodeProperty + 'Array';
            node[arrayName] = [];
            for(var reqProperty in node[nodeProperty]) {
              node[nodeProperty][reqProperty].name = reqProperty;
              node[arrayName].push(node[nodeProperty][reqProperty])
            }
            
        console.log('node array:', + node[arrayName]);
        }
      }
    });
    console.log( this.dataForsimulation);
  
  
  }

 buildD3DataLinks() {
  this.dataForsimulation.nodes.forEach((node) => {
    if(node.requirementsArray && node.requirementsArray.length > 0) {
      node.requirementsArray.forEach(requirement => {
        let linkObject = {};
        linkObject['sourceName'] = node.name + requirement.name;
        linkObject['sourceid'] = node.name + requirement.name;
        linkObject['sourceX'] = requirement.x;
        linkObject['sourceY'] = requirement.y;
        linkObject['targetNode'] = requirement.node;
        linkObject['targetCapabilility'] = requirement.capability;
        linkObject['ele'] = d3.select('#'+ linkObject['sourceid']);
        this.dataForsimulation.links.push(linkObject);
      });      
    }
  });

  this.capabilityTargets();
 }

 capabilityTargets() {
   this.dataForsimulation.links.forEach(link=>{
    this.dataForsimulation.nodes.forEach(node=>{
          if(node.name == link.targetNode && node.capabilitiesArray) {
            node.capabilitiesArray.forEach(capability=>{
              if(capability.name == link.targetCapabilility) {
                link['targetX'] = capability.x;
                link['targetY'] = capability.y;
              }
            })
          }
      });
   });

   this.drawlink();
 }

 drawlink() {
   this.dataForsimulation.links.forEach(link=>{
      this.svg.append('line')
      .attr('x1', link.sourceX)
      .attr('y1', link.sourceY)
      .attr('x2', link.targetX)
      .attr('y2', link.targetY)
      .attr('stroke','gray')
      .attr('stroke-width', 2);
   });
 }

 handleMouseOver() {
  console.log('mouse over');
 }

 trigerNodeSelectEvent(d) {
  this.onNodeSelect.emit(d);
}

}
