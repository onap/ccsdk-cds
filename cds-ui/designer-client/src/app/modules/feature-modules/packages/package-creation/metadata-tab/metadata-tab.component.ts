import {Component, OnInit} from '@angular/core';
import {PackageCreationService} from '../package-creation.service';
import {MetaDataTabModel} from '../mapping-models/metadata/MetaDataTab.model';
import {PackageCreationStore} from '../package-creation.store';
import {PackageStore} from '../../configuration-dashboard/package.store';


@Component({
    selector: 'app-metadata-tab',
    templateUrl: './metadata-tab.component.html',
    styleUrls: ['./metadata-tab.component.css']
})
export class MetadataTabComponent implements OnInit {

    counter = 0;
    tags = new Set<string>();
    customKeysMap = new Map();
    modes: object[] = [
        {name: 'Designer Mode', style: 'mode-icon icon-designer-mode'},
        {name: 'Scripting Mode', style: 'mode-icon icon-scripting-mode'},
        {name: 'Generic Script Mode', style: 'mode-icon icon-generic-script-mode'}];
    private metaDataTab: MetaDataTabModel = new MetaDataTabModel();
    private errorMessage: string;

    constructor(private packageCreationService: PackageCreationService, private packageCreationStore: PackageCreationStore,
                private packageStore: PackageStore) {

    }

    ngOnInit() {
        this.metaDataTab.templateTags = this.tags;
        this.metaDataTab.mapOfCustomKey = this.customKeysMap;
        this.packageCreationStore.changeMetaData(this.metaDataTab);

        this.packageStore.state$.subscribe(element => {
            if (element && element.configuration) {
                this.metaDataTab.name = element.configuration.artifactName;
                this.metaDataTab.version = element.configuration.artifactVersion;
                this.metaDataTab.tags = element.configuration.tags;
                this.metaDataTab.description = element.configuration.artifactDescription;

            }
        });
    }

    removeTag(value) {
        // console.log(event);
        this.tags.delete(value);
    }

    addTag(event) {
        const value = event.target.value;
        console.log(value);
        if (value && value.trim().length > 0) {
            event.target.value = '';
            this.tags.add(value);
        }
    }

    removeKey(event, key) {
        console.log(event);
        this.customKeysMap.delete(key);
    }

    addCustomKey() {
        // tslint:disable-next-line: no-string-literal
        const key = document.getElementsByClassName('mapKey')[0];
        // tslint:disable-next-line: no-string-literal
        const value = document.getElementsByClassName('mapValue')[0];

        // tslint:disable-next-line: no-string-literal
        if (key['value'] && value['value']) {
            // tslint:disable-next-line: no-string-literal
            this.customKeysMap.set(key['value'], value['value']);
            // tslint:disable-next-line: no-string-literal
            key['value'] = '';
            // tslint:disable-next-line: no-string-literal
            value['value'] = '';
        }
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
