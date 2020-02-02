import {Component, OnInit} from '@angular/core';
import {DslDefinition} from '../mapping-models/CBAPacakge.model';
import {PackageCreationStore} from '../package-creation.store';

@Component({
    selector: 'app-dsl-definitions-tab',
    templateUrl: './dsl-definitions-tab.component.html',
    styleUrls: ['./dsl-definitions-tab.component.css']
})
export class DslDefinitionsTabComponent implements OnInit {

    dslDefinition: DslDefinition = new DslDefinition();
    lang = 'json';

    constructor(private packageCreationStore: PackageCreationStore) {
    }

    ngOnInit() {
        this.packageCreationStore.changeDslDefinition(this.dslDefinition);

    }
}
