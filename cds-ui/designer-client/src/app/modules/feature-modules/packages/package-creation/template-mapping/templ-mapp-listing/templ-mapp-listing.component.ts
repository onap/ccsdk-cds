import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {PackageCreationStore} from '../../package-creation.store';
import {Template} from '../../mapping-models/CBAPacakge.model';
import {TemplateInfo, TemplateStore} from '../../template.store';

@Component({
    selector: 'app-templ-mapp-listing',
    templateUrl: './templ-mapp-listing.component.html',
    styleUrls: ['./templ-mapp-listing.component.css']
})
export class TemplMappListingComponent implements OnInit {
    @Output() showCreationViewParentNotification = new EventEmitter<any>();
    private templates: Template;
    private sourceCodeEditorContnet: string;

    constructor(private packageCreationStore: PackageCreationStore, private templateStore: TemplateStore) {
    }

    ngOnInit() {
        this.packageCreationStore.state$.subscribe(cba => {
            if (cba.templates) {
                this.templates = cba.templates;
            }
        });
    }

    openCreationView() {
        this.showCreationViewParentNotification.emit('tell parent to open create views');
    }

    setSourceCodeEditor(key: string) {
        this.packageCreationStore.state$.subscribe(cba => {
            if (cba.templates) {
                const fileContent = cba.templates.getValue(key);
                const templateInfo = new TemplateInfo();
                templateInfo.fileContent = fileContent;
                templateInfo.fileName = key;
                this.templateStore.changeTemplateInfo(templateInfo);
            }
        });
    }
}
