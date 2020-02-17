import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {PackageCreationStore} from '../../package-creation.store';
import {Template} from '../../mapping-models/CBAPacakge.model';

@Component({
    selector: 'app-templ-mapp-listing',
    templateUrl: './templ-mapp-listing.component.html',
    styleUrls: ['./templ-mapp-listing.component.css']
})
export class TemplMappListingComponent implements OnInit {
    @Output() showCreationViewParentNotification = new EventEmitter<any>();
    private templates: Template;

    constructor(private packageCreationStore: PackageCreationStore) {
        this.packageCreationStore.state$.subscribe(cba => {
            if (cba.templates) {
                this.templates = cba.templates;
            }
        });
    }

    ngOnInit() {
    }

    openCreationView() {
        this.showCreationViewParentNotification.emit('tell parent to open create views');
    }

}
