import {Component, OnInit} from '@angular/core';
import {DesignerStore} from '../designer.store';
import {ModelType} from '../../model/ModelType.model';


@Component({
    selector: 'app-functions',
    templateUrl: './functions.component.html',
    styleUrls: ['./functions.component.css']
})
export class FunctionsComponent implements OnInit {
    viewedFunctions: ModelType[] = [];

    constructor(private designerStore: DesignerStore) {

        this.designerStore.state$.subscribe(state => {
            console.log(state);
            if (state.functions) {
                this.viewedFunctions = state.functions;
            }
        });
    }

    ngOnInit() {
        this.designerStore.getFuntions();
    }

}
