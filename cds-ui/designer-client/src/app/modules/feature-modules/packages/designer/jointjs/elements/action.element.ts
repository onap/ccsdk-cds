import * as joint from 'jointjs';
import { ActionElementTypeName } from 'src/app/common/constants/app-constants';
/**
 * please refer to documentation in file palette.function.element.ts to get more details
 * about how to create new element type and define it in typescript
 */

declare module 'jointjs' {
    namespace shapes {
        // add new module called "app" under the already existing "shapes" modeule inside jointjs
        export namespace app {
            class ActionElement extends joint.shapes.standard.Rectangle {
            }
        }
    }
}
const rectWidth = 616;
const rectHeight = 381;
// custom element implementation
// https://resources.jointjs.com/tutorials/joint/tutorials/custom-elements.html#markup
const ActionElement = joint.shapes.standard.Rectangle.define(ActionElementTypeName, {
    size: {width: rectWidth, height: rectHeight}
},
    {
    markup:
    `<defs>
        <rect id="custom-action" x="0" y="30" width="${rectWidth}" height="${rectHeight}"></rect>
        <filter x="-1.7%" y="-2.2%" width="100%" height="100%" filterUnits="objectBoundingBox" id="filter-2">
            <feMorphology radius="0.5" operator="dilate" in="SourceAlpha" result="shadowSpreadOuter1"></feMorphology>
            <feOffset dx="0" dy="2" in="shadowSpreadOuter1" result="shadowOffsetOuter1"></feOffset>
            <feGaussianBlur stdDeviation="3" in="shadowOffsetOuter1" result="shadowBlurOuter1"></feGaussianBlur>
            <feComposite in="shadowBlurOuter1" in2="SourceAlpha" operator="out" result="shadowBlurOuter1"></feComposite>
            <feColorMatrix
            values="0 0 0 0 0.0705882353   0 0 0 0 0.450980392   0 0 0 0 0.921568627  0 0 0 0.1 0"
             type="matrix" in="shadowBlurOuter1"></feColorMatrix>
        </filter>
    </defs>
    <g id="Page-1" stroke="none" stroke-width="1" fill="none" fill-rule="evenodd">
        <g id="7.2-Designer---Insert-Action" transform="translate(-380, 5)">
            <g id="workflow-container" transform="translate(401, 120)">
                <g id="Card">
                    <use fill="black" fill-opacity="1" filter="url(#filter-2)" xlink:href="#custom-action"></use>
                    <use stroke="#1273EB" stroke-width="1" fill="#FFFFFF" fill-rule="evenodd" xlink:href="#custom-action"></use>
                    </g>
                    <g id="name">
                        <path id="Rectangle"
                        fill="#C3CDDB"></path>
                        <text id="Action-1" font-family="HelveticaNeue-Bold, Helvetica Neue"
                        font-size="13" font-weight="bold" fill="#1273EB">
                            <tspan id="label" x="0" y="20">Action 1</tspan>
                        </text>
                </g>
            </g>
        </g>
    </g>`
});

Object.assign(joint.shapes, {
    app: {
        ActionElement
    }
});
