import {Component, OnInit} from '@angular/core';
import {PackageCreationService} from '../package-creation.service';
import {MetaDataTabModel} from '../mapping-models/metadata/MetaDataTab.model';
import {PackageCreationStore} from '../package-creation.store';


@Component({
    selector: 'app-metadata-tab',
    templateUrl: './metadata-tab.component.html',
    styleUrls: ['./metadata-tab.component.css']
})
export class MetadataTabComponent implements OnInit {

    counter = 0;
    modes: object[] = [
        {name: 'Designer Mode', style: 'mode-icon icon-designer-mode'},
        {name: 'Scripting Mode', style: 'mode-icon icon-scripting-mode'}];
    private metaDataTab: MetaDataTabModel = new MetaDataTabModel();
    private errorMessage: string;

    constructor(private packageCreationService: PackageCreationService, private packageCreationStore: PackageCreationStore) {

    }

    ngOnInit() {
        this.packageCreationStore.changeMetaData(this.metaDataTab);
    }

    validatePackageNameAndVersion() {
        if (this.metaDataTab.name && this.metaDataTab.version) {
            this.packageCreationService.checkBluePrintNameAndVersion(this.metaDataTab.name, this.metaDataTab.version).then(element => {
                if (element) {
                    this.errorMessage = 'the package with name and version is exists';
                } else {
                    this.errorMessage = ' ';
                }
            });
        }

    }
}
