declare var require: any
import { Component, OnInit } from '@angular/core';
import * as $ from 'jquery';
import * as _ from 'lodash';
const joint = require('../../node_modules/jointjs/dist/joint.js');


@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = 'jointJS-designer';

  public graph: any;
  public paper: any;
  public rect: any;
  public rect2: any;
  public link: any;

  public stencilGraph: any;
  public stencilPaper: any;

  public selectedElement = {
    attributes: {
      attrs: {
        label: {
          text: 'abc'
        }
      }
    }
  };

  grapJson: any;
  sourceMode = false;

  constructor() {

  }

  ngOnInit() {
    // this.creatElementWithPorts();
    // this.dragCopyPlainElements();
    this.sourceMode = false;
    this.dragCopyElementsWithPort();
    // this.newDragCopy();

    this.createContainerElements();
  }

  creatElementWithPorts() {
    // working code

    // create graph
    let elementList: any[] = [];
    // this.graph = new joint.dia.Graph;

    // // create paper
    // this.paper = new joint.dia.Paper({
    //   el: document.getElementById('paper'),
    //   width: 1000,
    //   height: 1000,
    //   model: this.graph,
    //   gridSize: 2,
    //   drawGrid: true
    // });

    // this.paper.setGrid({
    //   name: 'dot',
    //   args:
    //     { color: 'black', thickness: 2, scaleFactor: 8 }

    // }).drawGrid();

    // create element
    this.rect = new joint.shapes.basic.Rect({
      position: { x: 100, y: 30 },
      size: { width: 100, height: 100 },
      attrs: { rect: { fill: 'white' }, text: { text: 'my box', fill: 'white' } }
    });
    this.rect.translate(100, 50);
    elementList.push(this.rect);

    this.rect.position(10, 10);
    // clone element
    // this.rect2 = this.rect.clone();
    // this.rect2.translate(180);
    // elementList.push(this.rect2);

    // Create link
    // this.link = new joint.dia.Link({
    //   source: { id: this.rect.id },
    //   target: { id: this.rect2.id }
    // });


    // create circle
    var circle = new joint.shapes.standard.Circle();
    circle.resize(100, 100);
    circle.position(180, 10);
    circle.attr('root/title', 'joint.shapes.standard.Circle');
    circle.attr('label/text', 'Circle');
    circle.attr('body/fill', 'lightblue');
    elementList.push(circle);

    // create link
    var ellipse = new joint.shapes.standard.Ellipse();
    ellipse.resize(150, 100);
    ellipse.position(180, 150);
    ellipse.attr('root/title', 'joint.shapes.standard.Ellipse');
    ellipse.attr('label/text', 'Ellipse');
    ellipse.attr('body/fill', 'lightblue');
    elementList.push(ellipse);

    // rectangle with header
    var headeredRectangle = new joint.shapes.standard.HeaderedRectangle();
    headeredRectangle.resize(150, 100);
    headeredRectangle.position(10, 280);
    headeredRectangle.attr('root/title', 'joint.shapes.standard.HeaderedRectangle');
    headeredRectangle.attr('header/fill', 'lightgray');
    headeredRectangle.attr('headerText/text', 'Header');
    headeredRectangle.attr('bodyText/text', 'Headered\nRectangle');
    elementList.push(headeredRectangle);

    let m1 = new joint.shapes.devs.Model({
      position: { x: 200, y: 280 },
      size: { width: 90, height: 90 },
      inPorts: ['in1', 'in2'],
      outPorts: ['out', 'out2'],
      ports: {
        groups: {
          'in': {
            attrs: {
              '.port-body': {
                fill: '#16A085'
              }
            }
          },
          'out': {
            attrs: {
              '.port-body': {
                fill: '#E74C3C'
              }
            }
          }
        }
      },
      attrs: {
        '.label': { text: 'Model', 'ref-x': .5, 'ref-y': .2 },
        rect: { fill: 'white' }
      }
    });
    elementList.push(m1);

    //container element
    let c1 = new joint.shapes.devs.Coupled({
      position: {
          x: 150,
          y: 470
      },
      size: {
          width: 200,
          height: 200
      }
    });

    c1.set('inPorts', ['in']);
  c1.set('outPorts', ['out 1', 'out 2']);
  c1.attr({
    '.body': {
        'rx': 6,
        'ry': 6
    }
});
  elementList.push(c1);
  // circle.position(10, 150);
  var a1 = new joint.shapes.devs.Atomic({
    position: {
        x: 10,
        y: 150
    },
    inPorts: ['xy'],
    outPorts: ['x', 'y']
  });

  a1.attr({
    '.body': {
        'rx': 6,
        'ry': 6
    }
  });

  elementList.push(a1);

    
    return elementList;
  }

  dragCopyPlainElements() {
    // Canvas where sape are dropped
    this.graph = new joint.dia.Graph,
      this.paper = new joint.dia.Paper({
        el: $('#paper'),
        model: this.graph
      });

    // Canvas from which you take shapes
    this.stencilGraph = new joint.dia.Graph,
      this.stencilPaper = new joint.dia.Paper({
        el: $('#stencil'),
        height: 60,
        model: this.stencilGraph,
        interactive: false
      });

    var r1 = new joint.shapes.basic.Rect({
      position: {
        x: 10,
        y: 10
      },
      size: {
        width: 100,
        height: 40
      },
      attrs: {
        text: {
          text: 'Rect1'
        }
      }
    });
    var r2 = new joint.shapes.basic.Rect({
      position: {
        x: 120,
        y: 10
      },
      size: {
        width: 100,
        height: 40
      },
      attrs: {
        text: {
          text: 'Rect2'
        }
      }
    });
    this.stencilGraph.addCells([r1, r2]);

    let _this = this;

    this.stencilPaper.on('cell:pointerdown', function (cellView, e, x, y) {
      $('body').append('<div id="flyPaper" style="position:fixed;z-index:100;opacity:.7;pointer-event:none;"></div>');
      var flyGraph = new joint.dia.Graph,
        flyPaper = new joint.dia.Paper({
          el: $('#flyPaper'),
          model: flyGraph,
          interactive: false
        }),
        flyShape = cellView.model.clone(),
        pos = cellView.model.position(),
        offset = {
          x: x - pos.x,
          y: y - pos.y
        };

      flyShape.position(0, 0);
      flyGraph.addCell(flyShape);
      $("#flyPaper").offset({
        left: e.pageX - offset.x,
        top: e.pageY - offset.y
      });
      $('body').on('mousemove.fly', function (e) {
        $("#flyPaper").offset({
          left: e.pageX - offset.x,
          top: e.pageY - offset.y
        });
      });
      $('body').on('mouseup.fly', function (e) {
        var x = e.pageX,
          y = e.pageY,
          target = _this.paper.$el.offset();

        // Dropped over paper ?
        if (x > target.left && x < target.left + _this.paper.$el.width() && y > target.top && y < target.top + _this.paper.$el.height()) {
          var s = flyShape.clone();
          s.position(x - target.left - offset.x, y - target.top - offset.y);
          _this.graph.addCell(s);
        }
        $('body').off('mousemove.fly').off('mouseup.fly');
        flyShape.remove();
        $('#flyPaper').remove();
      });
    })
  }

  dragCopyElementsWithPort() {
    // Canvas where sape are dropped
    
    this.graph = new joint.dia.Graph,
      this.paper = new joint.dia.Paper({
        el: $('#paper'),
        model: this.graph,
        height: 700,
        width: 1000,
        gridSize: 2,
        drawGrid: true
      });

    // create paper
    // this.paper = new joint.dia.Paper({
    //   el: document.getElementById('paper'),
    //   width: 1000,
    //   height: 1000,
    //   model: this.graph,
    //   gridSize: 2,
    //   drawGrid: true
    // });

    this.paper.setGrid({
      name: 'dot',
      args:
        { color: 'black', thickness: 2, scaleFactor: 8 }

    }).drawGrid();

    // Canvas from which you take shapes
    this.stencilGraph = new joint.dia.Graph,
      this.stencilPaper = new joint.dia.Paper({
        el: $('#stencil'),
        height: 700,
        width: 382,
        model: this.stencilGraph,
        interactive: false
      });      
    
      let elementWithPort = this.creatElementWithPorts();
      // let elementWithPort = this.createCustomElement();
      // let elementWithPort = this.myCustomElementGenerator();

      
    
    elementWithPort.forEach(element => {
      this.stencilGraph.addCell(element);
    });
    
    let _this = this;
    this.stencilPaperEventListeners(_this);
    this.drawAreapaperEventListerners();
  }

  resetAll(paper) {
    this.paper.drawBackground({
      color: 'white'
    })

    var elements = this.paper.model.getElements();
    for (var i = 0, ii = elements.length; i < ii; i++) {
      var currentElement = elements[i];
      currentElement.attr('body/stroke', 'black');
    }

    var links = this.paper.model.getLinks();
    for (var j = 0, jj = links.length; j < jj; j++) {
      var currentLink = links[j];
      currentLink.attr('line/stroke', 'black');
      currentLink.label(0, {
        attrs: {
          body: {
            stroke: 'black'
          }
        }
      })
    }
  }

  onDrag(evt) {
    // transform client to paper coordinates
    var p = evt.data.paper.snapToGrid({
      x: evt.clientX,
      y: evt.clientY
    });
    // manually execute the linkView mousemove handler
    evt.data.view.pointermove(evt, p.x, p.y);
  }

  onDragEnd(evt) {
    // manually execute the linkView mouseup handler
    evt.data.view.pointerup(evt);
    $(document).off('.example');
  }

  stencilPaperEventListeners(_this) {
    this.stencilPaper.on('cell:pointerdown', function (cellView, e, x, y) {
      $('body').append('<div id="flyPaper" style="position:fixed;z-index:100;opacity:.7;pointer-event:none;"></div>');
      var flyGraph = new joint.dia.Graph,
        flyPaper = new joint.dia.Paper({
          el: $('#flyPaper'),
          model: flyGraph,
          interactive: true
        }),
        flyShape = cellView.model.clone(),
        pos = cellView.model.position(),
        offset = {
          x: x - pos.x,
          y: y - pos.y
        };

      flyShape.position(0, 0);
      flyGraph.addCell(flyShape);
      $("#flyPaper").offset({
        left: e.pageX - offset.x,
        top: e.pageY - offset.y
      });
      $('body').on('mousemove.fly', function (e) {
        $("#flyPaper").offset({
          left: e.pageX - offset.x,
          top: e.pageY - offset.y
        });
      });
      let elementabove, elementBelow;
      $('body').on('mouseup.fly', function (e) {
        console.log(this);        
        var x = e.pageX,
          y = e.pageY,
          target = _this.paper.$el.offset();

        // Dropped over paper ?
        if (x > target.left && x < target.left + _this.paper.$el.width() && y > target.top && y < target.top + _this.paper.$el.height()) {
          var s = flyShape.clone();

          // var coordinates = new g.Point(x, y);
          // elementabove = s;
          // elementBelow =  _this.paper.model.findModelsFromPoint(coordinates).find(function(el) {
          //   return (el.id !== elementabove.id);
          // });
          // elementBelow =_this.paper.findModelsFromPoint(coordinates).find(function(el) {
          //     return (el.id !== elementabove.id);
          //   });
          // elementBelow.embed(elementabove);

          s.position(x - target.left - offset.x, y - target.top - offset.y);
          _this.graph.addCell(s);
          // let elementssss = (_this.graph.getElements());
          // console.log("elementsss", elementssss);
          // let elementBelow = elementssss[0];
          // let elementAbove;
          // if(elementssss[1]) {
          //   elementAbove = elementssss[1];
          //   elementBelow.embed(elementabove);
          // }
        }
        $('body').off('mousemove.fly').off('mouseup.fly');
        flyShape.remove();
        $('#flyPaper').remove();
      });
      _this.paper.on('mouse')
    })
  }

  drawAreapaperEventListerners() {
    // create event listerners
    let _this = this;
    this.paper.on('element:pointerdblclick', function (elementView) {
      _this.resetAll(this);
      _this.selectedElement = elementView.model;
      var currentElement = elementView.model;
      currentElement.attr('body/stroke', 'orange');
      // currentElement.attr('label/text', "abc");
    });

    this.paper.on('blank:pointerdblclick', function () {
      _this.resetAll(this);

      this.drawBackground({
        color: 'orange'
      });
    });

    this.paper.on('link:pointerclick', function (linkView) {
      _this.resetAll(this);
      let currentElement = linkView.model;
      currentElement.appendLabel({
        attrs: {
          text: {
            text: "Hello to new link!"
          }
        }
      });

    });


    this.paper.on('blank:pointerdown', function (evt, x, y) {
      let linkView = this.getDefaultLink()
        .set({
          'source': { x: x, y: y },
          'target': { x: x, y: y }
        })
        .addTo(this.model)
        .findView(this);
      linkView.startArrowheadMove('target');

      $(document).on({
        'mousemove.example': _this.onDrag,
        'mouseup.example': _this.onDragEnd
      }, {
          // shared data between listeners
          view: linkView,
          paper: this
        });
    });

    this.paper.on({
      // 'element:pointerdown': function(elementView, evt) {

      //   evt.data = elementView.model.position();
      // },
      'element:pointerup': function(elementView, evt, x, y) {
        var coordinates = new g.Point(x, y);
        var elementAbove = elementView.model;
        var elementBelow = this.model.findModelsFromPoint(coordinates).find(function(el) {
                return (el.id !== elementAbove.id);
            });
        if(elementBelow) elementBelow.embed(elementAbove);
      }

    }); //end of my event
  }

  createCustomElement() {
    joint.shapes.html = {};
    joint.shapes.html.Element = joint.shapes.basic.Rect.extend({
      defaults: joint.util.deepSupplement({
          type: 'html.Element',
          attrs: {
              rect: { stroke: 'none', 'fill-opacity': 0 }
          }
      }, joint.shapes.basic.Rect.prototype.defaults)
    });

    // / Create a custom view for that element that displays an HTML div above it.
    // -------------------------------------------------------------------------

    joint.shapes.html.ElementView = joint.dia.ElementView.extend({

          template: [
            '<div class="html-element">',
            '<button class="delete">x</button>',
            '<label></label>',
            '<span></span>', '<br/>',
            '<select><option>--</option><option>one</option><option>two</option></select>',
            '<input type="text" value="I\'m HTML input" />',
            '</div>'
        ].join(''),

        initialize: function() {
          _.bindAll(this, 'updateBox');
          joint.dia.ElementView.prototype.initialize.apply(this, arguments);

          this.$box = $(_.template(this.template)());
          // Prevent paper from handling pointerdown.
          this.$box.find('input,select').on('mousedown click', function(evt) {
              evt.stopPropagation();
          });
          // This is an example of reacting on the input change and storing the input data in the cell model.
          this.$box.find('input').on('change', _.bind(function(evt) {
              this.model.set('input', $(evt.target).val());
          }, this));
          this.$box.find('select').on('change', _.bind(function(evt) {
              this.model.set('select', $(evt.target).val());
          }, this));
          this.$box.find('select').val(this.model.get('select'));
          this.$box.find('.delete').on('click', _.bind(this.model.remove, this.model));
          // Update the box position whenever the underlying model changes.
          this.model.on('change', this.updateBox, this);
          // Remove the box when the model gets removed from the graph.
          this.model.on('remove', this.removeBox, this);

          this.updateBox();
        },

        render: function() {
          joint.dia.ElementView.prototype.render.apply(this, arguments);
          this.paper.$el.prepend(this.$box);
          this.updateBox();
          return this;
        },

      updateBox: function() {
        // Set the position and dimension of the box so that it covers the JointJS element.
        var bbox = this.model.getBBox();
        // Example of updating the HTML with a data stored in the cell model.
        this.$box.find('label').text(this.model.get('label'));
        this.$box.find('span').text(this.model.get('select'));
        this.$box.css({
            width: bbox.width,
            height: bbox.height,
            left: bbox.x,
            top: bbox.y,
            transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)'
        });
      },

      removeBox: function(evt) {
        this.$box.remove();
      }
    });

    // Create JointJS elements and add them to the graph as usual.
        // -----------------------------------------------------------

        var el1 = new joint.shapes.html.Element({
          position: { x: 10, y: 10 },
          size: { width: 170, height: 100 },
          label: 'I am HTML',
          select: 'one'
      });

      var el2 = new joint.shapes.html.Element({
        position: { x: 370, y: 160 },
        size: { width: 170, height: 100 },
        label: 'Me too',
        select: 'two'
    });
    var l = new joint.dia.Link({
        source: { id: el1.id },
        target: { id: el2.id },
        attrs: { '.connection': { 'stroke-width': 5, stroke: '#34495E' }}
    });

    let elementArray : any[] = [];
    elementArray.push(el1, el2);
    return elementArray;
  }

  myCustomElementGenerator() {
    var Ellipse = joint.dia.Element.define('examples.Ellipse', {
      // default attributes
      markup: [{
          tagName: 'ellipse',
          selector: 'ellipse' // not necessary but faster
      }],
      attrs: {
          ellipse: {
              fill: 'white',
              stroke: 'black',
              strokeWidth: 4,
              refRx: .5,
              refRy: .5,
              refCx: .5,
              refCy: .5
          }
      }
  });

    var Rectangle = joint.shapes.standard.Rectangle.define('examples.CustomRectangle', {
      markup: [{
        tagName: 'body',
        selector: 'body' // not necessary but faster
      },
      {
        tagName: 'label',
        selector: 'label'
      }],
      attrs: {
        body: {
          rx: 10, // add a corner radius
          ry: 10,
          strokeWidth: 1,
          fill: 'cornflowerblue'
        },
        label: {
          textAnchor: 'left', // align text to left
          refX: 10, // offset text from right edge of model bbox
          fill: 'white',
          fontSize: 18,
          text: 'mad mad mad'
        }
      }
    })
  

  var customElement = (new joint.dia.Element.examples.Ellipse()).position(100, 100).size(120, 50);
    let elementsArray = [];
    elementsArray.push(customElement);

  var customRect = new Rectangle().position(100, 200).size(120, 120);
  elementsArray.push(customRect);
    return elementsArray;
  }

  convertGraphToJson() {
    this.grapJson = JSON.stringify(this.graph.toJSON());
    // this.grapJson = this.graph.toJSON();
    console.log(this.graph.toJSON());
    console.log(this.grapJson);
    this.sourceMode = true;
  }

  setNewValue(event) {
    // this.selectedElement.attr('label/text', event.currentTarget.value);
  }

  convertJsonToGraph() {
    this.sourceMode = false;
  }

  zoomIn() {
    var graphScale = 1;
    graphScale += 0.1;
    this.paper.scale(graphScale, graphScale);
  }

  zoomOut() {
    var graphScale = 1;
    graphScale -= 0.1;
    this.paper.scale(graphScale, graphScale);
  }

  createContainerElements() {
    this.graph = new joint.dia.Graph;

    this.paper = new joint.dia.Paper({

      el: document.getElementById('paper'),
      width: 800,
      height: 400,
      gridSize: 1,
      model: this.graph,
      snapLinks: true,
      linkPinning: false,
      embeddingMode: true,
      clickThreshold: 5,
      defaultConnectionPoint: { name: 'boundary' },
      highlighting: {
          'default': {
              name: 'stroke',
              options: {
                  padding: 6
              }
          },
          'embedding': {
              name: 'addClass',
              options: {
                  className: 'highlighted-parent'
              }
          }
      },
  
      validateEmbedding: function(childView, parentView) {
          return parentView.model instanceof joint.shapes.devs.Coupled;
      },
  
      validateConnection: function(sourceView, sourceMagnet, targetView, targetMagnet) {
          return sourceMagnet != targetMagnet;
      }
  });

  let c1 = new joint.shapes.devs.Coupled({
    position: {
        x: 230,
        y: 50
    },
    size: {
        width: 300,
        height: 300
    }
});

c1.set('inPorts', ['in']);
c1.set('outPorts', ['out 1', 'out 2']);

var a1 = new joint.shapes.devs.Atomic({
  position: {
      x: 360,
      y: 260
  },
  inPorts: ['xy'],
  outPorts: ['x', 'y']
});

var a2 = new joint.shapes.devs.Atomic({
  position: {
      x: 50,
      y: 160
  },
  outPorts: ['out']
});

var a3 = new joint.shapes.devs.Atomic({
  position: {
      x: 650,
      y: 50
  },
  size: {
      width: 100,
      height: 300
  },
  inPorts: ['a', 'b']
});

[c1, a1, a2, a3].forEach(function(element) {
  element.attr({
      '.body': {
          'rx': 6,
          'ry': 6
      }
  });
});



this.graph.addCells([c1, a1, a2, a3]);

c1.embed(a1);

this.connect(a2, 'out', c1, 'in');
this.connect(c1, 'in', a1, 'xy');
this.connect(a1, 'x', c1, 'out 1');
this.connect(a1, 'y', c1, 'out 2');
this.connect(c1, 'out 1', a3, 'a');
this.connect(c1, 'out 2', a3, 'b');

var strokeDasharrayPath = '.body/strokeDasharray';
let _this = this;

this.paper.on('element:pointerdblclick', function(elementView) {
    var element = elementView.model;
    if (element.get('type') === 'devs.Atomic') {
      _this.toggleDelegation(element);
    }
});

this.paper.setInteractivity(function(elementView) {
    return {
        stopDelegation: !elementView.model.attr(strokeDasharrayPath)
    };
});

  
} // function end

  // function
  connect(source, sourcePort, target, targetPort) {

    var link = new joint.shapes.devs.Link({
        source: {
            id: source.id,
            port: sourcePort
        },
        target: {
            id: target.id,
            port: targetPort
        }
    });

    link.addTo(this.graph).reparent();
  }

  toggleDelegation(element) {
    var strokeDasharrayPath = '.body/strokeDasharray';
    element.attr(strokeDasharrayPath, element.attr(strokeDasharrayPath) ? '' : '15,1');
}

createContainerElemnetsByDragDrop() {
  

}
}
