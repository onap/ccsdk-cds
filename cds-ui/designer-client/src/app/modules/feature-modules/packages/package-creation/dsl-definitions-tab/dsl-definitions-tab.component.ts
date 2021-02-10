import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {DslDefinition} from '../mapping-models/CBAPacakge.model';
import {PackageCreationStore} from '../package-creation.store';

@Component({
    selector: 'app-dsl-definitions-tab',
    templateUrl: './dsl-definitions-tab.component.html',
    styleUrls: ['./dsl-definitions-tab.component.css']
})
export class DslDefinitionsTabComponent implements OnInit {

    dslDefinition: DslDefinition = new DslDefinition();
    @Output() changeEvent = new EventEmitter<string>();
    lang = 'json';

    constructor(private packageCreationStore: PackageCreationStore) {
    }

    ngOnInit() {
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            if (cbaPackage && cbaPackage.definitions && cbaPackage.definitions.dslDefinition) {
                this.dslDefinition.content = cbaPackage.definitions.dslDefinition.content;
            }
        });

    }

    textChanged(event) {
        this.packageCreationStore.changeDslDefinition(this.dslDefinition);
    }

    callParent(): void {
        this.changeEvent.next('some changes to enable save ');
    }

    onPaste($event: ClipboardEvent) {
        this.callParent();
    }
}
