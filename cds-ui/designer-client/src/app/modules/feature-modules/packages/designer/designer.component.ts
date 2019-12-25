import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import * as $ from 'jquery';
import * as _ from 'lodash';
import * as joint from '../../../../../../node_modules/jointjs/dist/joint.js';

@Component({
    selector: 'app-designer',
    templateUrl: './designer.component.html',
    styleUrls: ['./designer.component.css'],
    encapsulation: ViewEncapsulation.None
})
export class DesignerComponent implements OnInit {

    private controllerSideBar: boolean;
    private attributesSideBar: boolean;
    public graph: any;
    public paper: any;

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

        this.attachEditorBarToCanvas();
    }

    attachEditorBarToCanvas() {
        this.graph = new joint.dia.Graph,
            this.paper = new joint.dia.Paper({
                el: $('#paper'),
                model: this.graph,
                height: 720,
                width: 1200,
                gridSize: 2,
                drawGrid: true,
                cellViewNamespace: joint.shapes
            });

        this.paper.setGrid({
            name: 'dot',
            args:
                {color: 'black', thickness: 2, scaleFactor: 8}

        }).drawGrid();


        joint.shapes['html'] = {};
        joint.shapes['html'].Element = joint.shapes.basic.Rect.extend({
            defaults: joint.util.deepSupplement({
                type: 'html.Element'
            }, joint.shapes.basic.Rect.prototype.defaults)
        });

        joint.shapes['html'].ElementView = joint.dia.ElementView.extend({

            template: [
                '<div>',
                '<div id="editbar" class="editBar text-center">',
                '<div class="btn-group mr-2" role="group" aria-label="First group">',
                '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Undo">',
                '<img src="/assets/img/icon-undoActive.svg">',
                '</button>',
                '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Redo">',
                '<img src="/assets/img/icon-redo.svg">',
                '</button>',
                '</div>',
                '<div class="btn-group mr-2" role="group" aria-label="Second group">',
                '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Zoom Out">',
                '<img src="/assets/img/icon-zoomOut.svg">',
                '</button>',
                '<button type="button" class="btn btn-secondary pl-0 pr-0">100%</button>',
                '<button type="button" class="btn btn-secondary tooltip-bottom" data-tooltip="Zoom In">',
                '<img src="/assets/img/icon-zoomIn.svg">',
                '</button>',
                '</div>',
                '<div class="btn-group viewBtns" role="group" aria-label="Third group">',
                '<button type="button" class="btn btn-secondary topologySource active">View</button>',
                '<button type="button" class="btn btn-secondary topologyView">Source</button>',
                '</div>',
                '</div>',
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
                this.model.on('change', this.updateBox, this);

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
                this.$box.css({
                    width: bbox.width,
                    height: bbox.height,
                    left: bbox.x,
                    top: bbox.y,
                    transform: 'rotate(' + (this.model.get('angle') || 0) + 'deg)'
                });
            }
        });

        var el1 = new joint.shapes['html'].Element({});
        this.graph.addCells([el1]);
    }
}
