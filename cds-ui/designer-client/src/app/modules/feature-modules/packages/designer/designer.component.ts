import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import * as joint from 'jointjs';
import './jointjs/elements/palette.function.element';
import './jointjs/elements/action.element';
import './jointjs/elements/board.function.element';
import { DesignerStore } from './designer.store';
import { ActionElementTypeName } from 'src/app/common/constants/app-constants';



@Component({
  selector: 'app-designer',
  templateUrl: './designer.component.html',
  styleUrls: ['./designer.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class DesignerComponent implements OnInit {

  private controllerSideBar: boolean;
  private attributesSideBar: boolean;
  //to generate Ids for dragged function elements
  private fuctionIdCounter=0;
  private actionIdCounter=0;

  boardGraph: joint.dia.Graph;
  boardPaper: joint.dia.Paper;

  paletteGraph: joint.dia.Graph;
  palettePaper: joint.dia.Paper;

  constructor(private designerStore: DesignerStore) {
    this.controllerSideBar = true;
    this.attributesSideBar = false;
  }
  private _toggleSidebar1() {
    this.controllerSideBar = !this.controllerSideBar;
  }
  private _toggleSidebar2() {
    this.attributesSideBar = !this.attributesSideBar;
  }


  /**
   * - There is a board (main paper) that will the action and function selected from the palette
   * itmes in this board will be used to create tosca workflow and node templates
   * - There is also palette , whis contains all the possible functions and actions
   * that can be dragged into the board
   * - There is also a fly paper , which is temporarliy paper created on the fly
   * when items is dragged from the palette- and it's deleted when the item is dropped over
   * the board.
   * for more info about the drag and drop algorithem used please visit the following link:
   * https://stackoverflow.com/a/36932973/1340034
   */

  ngOnInit() {
    this.initializeBoard();
    this.initializePalette();
    // this.createEditBarOverThePaper();

    //functions list is contants for now
    const list = [
      { modelName: 'component-netconf-executor'},
      { modelName: 'component-remote-ansible-executor' },
      { modelName: 'dg-generic' },
      { modelName: 'component-resource-resolution' }];
      const cells = this.buildPaletteGraphFromList(list);
      this.paletteGraph.resetCells(cells);

      let idx = 0;
      cells.forEach(cell => {
        console.log(cell);
        cell.translate(5, (cell.attributes.size.height + 5) * idx++);

      });
      this.stencilPaperEventListeners();
      /**
       * the code to retrieve from server is commented
       */
        // this.designerStore.state$.subscribe(state => {
        //   console.log(state);
        //   if (state.functions) {
        //     console.log('functions-->' , state.functions);
        //     // this.viewedFunctions = state.functions;
        //     const list = state.functions;
        //   }
        // });
        //action triggering
        // this.designerStore.getFuntions();
    
  }

  initializePalette() {
    if (!this.paletteGraph) {
      this.paletteGraph = new joint.dia.Graph();
      this.palettePaper = new joint.dia.Paper({
        el: $('#palette-paper'),
        model: this.paletteGraph,
        height: 300,
        width: 300,
        gridSize: 1,
        interactive: false
      });
    }
  }

  initializeBoard() {
    if (!this.boardGraph) {
      this.boardGraph = new joint.dia.Graph();
      this.boardPaper = new joint.dia.Paper({
          el: $('#board-paper'),
          model: this.boardGraph,
          height: 720,
          width: 1200,
          gridSize: 10,
          drawGrid: true,
          // background: {
          //   color: 'rgba(0, 255, 0, 0.3)'
          // },
          cellViewNamespace: joint.shapes
        });

      this.boardPaper.on('all', element => {
        // console.log(element);
      });

      this.boardPaper.on('link:pointerdown', link => {
        console.log(link);
      });

      this.boardPaper.on('element:pointerdown', element => {
        // this.modelSelected.emit(element.model.get('model'));
      });

      this.boardPaper.on('blank:pointerclick', () => {
        // this.selectedModel = undefined;
      });

      this.boardGraph.on('change:position', (cell) => {

        var parentId = cell.get('parent');
        if (!parentId) return;

        var parent = this.boardGraph.getCell(parentId);
        
        var parentBbox = parent.getBBox();
        var cellBbox = cell.getBBox();
        if (parentBbox.containsPoint(cellBbox.origin()) &&
          parentBbox.containsPoint(cellBbox.topRight()) &&
          parentBbox.containsPoint(cellBbox.corner()) &&
          parentBbox.containsPoint(cellBbox.bottomLeft())) {

          
          // All the four corners of the child are inside
          // the parent area.
          return;
        }

        // Revert the child position.
        cell.set('position', cell.previous('position'));
      });
    }
  }

  insertCustomActionIntoBoard() {
    this.actionIdCounter++;
    const actionId = "action_" + this.actionIdCounter;
    const actionName = 'Action' + this.actionIdCounter;
    const element = this.createCustomAction(actionId , actionName);
    this.boardGraph.addCell(element);
    console.log('saving action to store action workflow....');
    this.designerStore.addDeclarativeWorkFlow(actionName);
  }

  createCustomAction(id: string, label: string) {
    const element = new joint.shapes.app.ActionElement({
      id: id
    });
    element.attr('#label/text', label);
    return element;
  }

  buildPaletteGraphFromList(list: any) {
    const elements = [];

    console.log(list);
    list.forEach(element => {
      elements.push(this.createFuctionElementForPalette(element.modelName));
    });

    return elements;
  }

  createFuctionElementForPalette(label: string) {
      const element = new joint.shapes.palette.FunctionElement({
        id: label
      });
      element.attr('#label/text', label);
      element.attr('type', label);
      return element;
    }

  createFuctionElementForBoard(id :String, label :string, type :string) {
    const boardElement = new joint.shapes.board.FunctionElement({
      id: id
    });
    boardElement.attr('#label/text', label);
    boardElement.attr('#type/text', type);
    return boardElement;
  }

  stencilPaperEventListeners() {
    this.palettePaper.on('cell:pointerdown', (draggedCell, pointerDownEvent, x, y) => {

      $('body').append(`
        <div id="flyPaper"
            style="position:fixed;z-index:100;opacity:.7;pointer-event:none;background-color: transparent !important;"></div>`
        );
      const flyGraph = new joint.dia.Graph();
      const flyPaper = new joint.dia.Paper({
          el: $('#flyPaper'),
          model: flyGraph,
          interactive: true
        });
      const flyShape = draggedCell.model.clone();
      const pos = draggedCell.model.position();
      const offset = {
        x: x - pos.x,
        y: y - pos.y
      };

      flyShape.position(0, 0);
      flyGraph.addCell(flyShape);
      $('#flyPaper').offset({
        left: pointerDownEvent.pageX - offset.x,
        top: pointerDownEvent.pageY - offset.y
      });
      $('body').on('mousemove.fly', mouseMoveEvent => {
        $('#flyPaper').offset({
          left: mouseMoveEvent.pageX - offset.x,
          top: mouseMoveEvent.pageY - offset.y
        });
      });

      $('body').on('mouseup.fly', mouseupEvent => {
        const mouseupX = mouseupEvent.pageX;
        const mouseupY = mouseupEvent.pageY;
        const target = this.boardPaper.$el.offset();
        // Dropped over paper ?
        if (mouseupX > target.left &&
          mouseupX < target.left + this.boardPaper.$el.width() &&
          mouseupY > target.top && y < target.top + this.boardPaper.$el.height()) {
          const functionType = flyShape.attributes.attrs.type;
          console.log(functionType);
          const functionElementForBoard = this.dropFunctionOverAction(functionType, mouseupX, target, offset, mouseupY);

          let parentCell = this.getParent(functionElementForBoard);

          console.log("parentCell -->", parentCell);

          if (parentCell && 
              parentCell.model.attributes.type === ActionElementTypeName){
                
            const actionName = parentCell.model.attributes.attrs['#label'].text;
            this.designerStore.addStepToDeclarativeWorkFlow(actionName, functionType);
            this.designerStore.addNodeTemplate(functionType);

            // Prevent recursive embedding.
            if (parentCell &&
              parentCell.model.get('parent') !== functionElementForBoard.id) {
              parentCell.model.embed(functionElementForBoard);
            }
          }else{
            console.log('function dropped outside action, rolling back...');
            functionElementForBoard.remove();
          }
        }
        $('body').off('mousemove.fly').off('mouseup.fly');
        // flyShape.remove();
        $('#flyPaper').remove();
      });
    });
  }

  private getParent(functionElementForBoard: joint.shapes.board.FunctionElement) {
    const cellViewsBelow = this.boardPaper.findViewsFromPoint(functionElementForBoard.getBBox().center());
    let cellViewBelow;
    if (cellViewsBelow.length) {
      cellViewsBelow.forEach(cellItem => {
        if (cellItem.model.id !== functionElementForBoard.id) {
          cellViewBelow = cellItem;
        }
      });
    }
    return cellViewBelow;
  }

  /**
   * trigger actions related to Function dropped over the board:
   * - create board function element of the same type of palette function 
   * as board function element is different from the palette function element
   * - save function to parent action in store
  */
  private dropFunctionOverAction(functionType: any, mouseupX: number, target: JQuery.Coordinates, offset: { x: number; y: number; }, mouseupY: number) {
    this.fuctionIdCounter++;
    const functionElementForBoard = this.createFuctionElementForBoard("fucntion_" + this.fuctionIdCounter, 'execute', functionType);
    functionElementForBoard.position(mouseupX - target.left - offset.x, mouseupY - target.top - offset.y);
    this.boardGraph.addCell(functionElementForBoard);
    return functionElementForBoard;
  }
  /**
   * this is a way to add the button like zoom in , zoom out , and source over jointjs paper
   * may be used if no other way is found
   */
  // createEditBarOverThePaper() {
  //   joint.shapes["html"] = {};
  //   joint.shapes["html"].Element = joint.shapes.basic.Rect.extend({
  //     defaults: joint.util.deepSupplement({
  //       type: 'html.Element'
  //     }, joint.shapes.basic.Rect.prototype.defaults)
  //   });
  //   joint.shapes["html"].ElementView = joint.dia.ElementView.extend({

  //     template: [
  //       '<div>',
  //       '<div id="editbar" class="editBar text-center">',
  //       '<div class="btn-group mr-2" role="group" aria-label="First group">',
  //       '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Undo">',
  //       '<img src="/assets/img/icon-undoActive.svg">',
  //       '</button>',
  //       '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Redo">',
  //       '<img src="/assets/img/icon-redo.svg">',
  //       '</button>',
  //       '</div>',
  //       '<div class="btn-group mr-2" role="group" aria-label="Second group">',
  //       '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Zoom Out">',
  //       '<img src="/assets/img/icon-zoomOut.svg">',
  //       '</button>',
  //       '<button type="button" class="btn btn-secondary pl-0 pr-0">100%</button>',
  //       '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Zoom In">',
  //       '<img src="/assets/img/icon-zoomIn.svg">',
  //       '</button>',
  //       '</div>',
  //       '<div class="btn-group viewBtns" role="group" aria-label="Third group">',
  //       '<button type="button" class="btn btn-secondary topologySource active">View</button>',
  //       '<button type="button" class="btn btn-secondary topologyView">Source</button>',
  //       '</div>',
  //       '</div>',
  //       '</div>'
  //     ].join(''),
  //     initialize: function () {
  //       _.bindAll(this, 'updateBox');
  //       joint.dia.ElementView.prototype.initialize.apply(this, arguments);

  //       this.$box = $(_.template(this.template)());
  //       // Prevent paper from handling pointerdown.
  //       this.$box.find('input,select').on('mousedown click', function (evt) {
  //         evt.stopPropagation();
  //       });
  //       this.model.on('change', this.updateBox, this);
  //       this.updateBox();
  //     },
  //     render: function () {
  //       joint.dia.ElementView.prototype.render.apply(this, arguments);
  //       this.paper.$el.prepend(this.$box);
  //       this.updateBox();
  //       return this;
  //     },
  //     updateBox: function () {
  //       // Set the position and dimension of the box so that it covers the JointJS element.
  //       var bbox = this.model.getBBox();
  //       this.$box.css({
  //         width: bbox.width,
  //         height: bbox.height,
  //         left: bbox.x,
  //         top: bbox.y,
  //         transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)'
  //       });
  //     }
  //   });

  // }
}
