/*
============LICENSE_START==========================================
===================================================================
Copyright (C) 2019 Orange. All rights reserved.
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

import * as joint from 'jointjs';
import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root'
})
export class GraphUtil {

    actionIdCounter = 0;
    // to generate Ids for dragged function elements
    private fuctionIdCounter = 0;

    createCustomAction(boardGraph: joint.dia.Graph) {
        const actionName = this.generateNewActionName();
        const actionId = this.generateNewActionId();
        const element = new joint.shapes.app.ActionElement({
            id: actionId
        });
        element.attr('#label/text', actionName);
        boardGraph.addCell(element);
        return element;
    }

    generateNewActionName() {
        this.actionIdCounter++;
        const actionName = 'Action' + this.actionIdCounter;
        return actionName;
    }

    private generateNewActionId() {
        const actionName =
                (Date.now().toString(36) + Math.random().toString(36).substr(2, 5))
                .toUpperCase();
        return actionName;
    }

    createCustomActionWithName(actionName: string, boardGraph: joint.dia.Graph) {
        const actionId = this.generateNewActionId();
        const element = new joint.shapes.app.ActionElement({
            id: actionId
        });
        element.attr('#label/text', actionName);
        boardGraph.addCell(element);
        return element;
    }

    buildPaletteGraphFromList(list: any) {
        const elements = [];
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

    createFuctionElementForBoard( label: string, type: string) {
        this.fuctionIdCounter++;
        const id = 'fucntion_' + this.fuctionIdCounter;
        const boardElement = new joint.shapes.board.FunctionElement({
            id
        });
        boardElement.attr('#label/text', label);
        boardElement.attr('#type/text', type);
        return boardElement;
    }

    getParent(functionElementForBoard: joint.shapes.board.FunctionElement, boardPaper: joint.dia.Paper) {
        const cellViewsBelow = boardPaper.findViewsFromPoint(functionElementForBoard.getBBox().center());
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
    dropFunctionOverActionWithPosition(
        label: string, type: string,
        mouseupX: number, mouseupY: number,
        target: JQuery.Coordinates, offset: { x: number; y: number; },
        boardGraph: joint.dia.Graph) {

        const functionElementForBoard = this.dropFunctionOverAction(label, type, boardGraph);
        functionElementForBoard.position(mouseupX - target.left - offset.x, mouseupY - target.top - offset.y);

        return functionElementForBoard;
    }


    dropFunctionOverActionRelativeToParent(
        parent: joint.shapes.app.ActionElement,
        label: string, type: string,
        boardGraph: joint.dia.Graph) {

        const functionElementForBoard = this.dropFunctionOverAction(label, type, boardGraph);
        parent.embed(functionElementForBoard);
        functionElementForBoard.position({ parentRelative: true });

        return functionElementForBoard;
    }


    dropFunctionOverAction(
        label: string, type: string,
        boardGraph: joint.dia.Graph) {

        // function name is the same as function type
        // actually functionName here refers step name in CDS tosca model
        // and function type is the nodeTempalteName
        const functionElementForBoard =
            this.createFuctionElementForBoard(label, type);
        boardGraph.addCell(functionElementForBoard);
        return functionElementForBoard;
    }

    getFunctionTypeFromPaletteFunction(cell: joint.shapes.palette.FunctionElement) {
        return cell.attributes.attrs.type;
    }

    getFunctionTypeFromBoardFunction(cell: joint.shapes.board.FunctionElement) {
        return cell.attributes.attrs['#type'].text;
    }

    getFunctionNameFromBoardFunction(cell: joint.shapes.board.FunctionElement) {
        return cell.attributes.attrs['#label'].text;
    }

    canEmpedMoreChildern(parentCell: joint.shapes.app.ActionElement, boardGraph: joint.dia.Graph): boolean {
        if (!parentCell.get('embeds')) {
            return true;
        }
        const types = this.getChildernTypes(parentCell, boardGraph);
        return parentCell.get('embeds').length < 1 ||
            types.includes('dg-generic');
    }


    getChildernTypes(parentCell: joint.shapes.app.ActionElement,
                     boardGraph: joint.dia.Graph): string[] {
        if (parentCell.get('embeds')) {
            return parentCell.get('embeds').map((cellName) => {
                const child = boardGraph.getCell(cellName) as joint.shapes.board.FunctionElement;
                const functionType = this.getFunctionTypeFromBoardFunction(child);
                console.log('functionType', functionType);
                return functionType;
            });
        } else {
            return [];
        }
    }

    getDgGenericChild(parentCell: joint.shapes.app.ActionElement,
                      boardGraph: joint.dia.Graph):
        joint.shapes.board.FunctionElement[] {
        if (parentCell.get('embeds')) {
            return parentCell.get('embeds')
                .filter((cellName) => {
                    const child = boardGraph.getCell(cellName) as joint.shapes.board.FunctionElement;
                    const functionType = this.getFunctionTypeFromBoardFunction(child);
                    return functionType === 'dg-generic';
                })
                .map((cellName) => {
                    const child = boardGraph.getCell(cellName) as joint.shapes.board.FunctionElement;
                    return child;
                });
        } else {
            return [];
        }
    }

    isEmptyParent(parentCell: joint.shapes.app.ActionElement): boolean {
        return !parentCell.get('embeds') || parentCell.get('embeds').length < 1;
    }

}
