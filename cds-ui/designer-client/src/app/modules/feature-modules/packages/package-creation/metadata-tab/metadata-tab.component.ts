import {Component, OnInit} from '@angular/core';
import {PackageCreationService} from '../package-creation.service';
import {MetaDataTabModel} from '../mapping-models/metadata/MetaDataTab.model';
import {PackageCreationStore} from '../package-creation.store';
import {ActivatedRoute} from '@angular/router';


@Component({
    selector: 'app-metadata-tab',
    templateUrl: './metadata-tab.component.html',
    styleUrls: ['./metadata-tab.component.css']
})
export class MetadataTabComponent implements OnInit {

    counter = 0;
    tags = new Set<string>();
    customKeysMap = new Map();
    modes: any[] = [
        {name: 'Designer Mode', style: 'mode-icon icon-topologyView-active'}];
    /*  {name: 'Scripting Mode', style: 'mode-icon icon-topologySource'},
      {name: 'Generic Script Mode', style: 'mode-icon icon-topologySource'}];*/
    modeType = this.modes[0].name;
    metaDataTab: MetaDataTabModel = new MetaDataTabModel();
    errorMessage: string;
    versionPattern = '^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$';
    isNameEditable = false;

    constructor(
        private route: ActivatedRoute,
        private packageCreationService: PackageCreationService,
        private packageCreationStore: PackageCreationStore
    ) {

    }

    ngOnInit() {
        this.metaDataTab.templateTags = this.tags;
        this.metaDataTab.mapOfCustomKey = this.customKeysMap;
        this.metaDataTab.mode = this.modeType;
        this.isNameEditable = this.route.snapshot.paramMap.get('id') == null;
        this.packageCreationStore.state$.subscribe(element => {

            if (element && element.metaData) {

                this.metaDataTab.name = element.metaData.name;
                this.metaDataTab.version = element.metaData.version;
                this.metaDataTab.description = element.metaData.description;
                this.tags = element.metaData.templateTags;
                this.tags.delete('');
                this.metaDataTab.templateTags = this.tags;
                if (element.metaData.mode && element.metaData.mode.includes('DEFAULT')) {
                    this.metaDataTab.mode = 'Designer Mode';
                    this.modeType = this.metaDataTab.mode;
                }

                this.customKeysMap = element.metaData.mapOfCustomKey;
                this.metaDataTab.mapOfCustomKey = this.customKeysMap;
                /* if (this.isNameEditable) {
                     this.validatePackageNameAndVersion();
                 }*/
                // this.tags = element.metaData.templateTags;


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
            this.tags.add(value.trim());
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
        console.log('in validate');
        console.log('in this.metaDataTab.name' + this.metaDataTab.name);
        if (this.metaDataTab.name && this.metaDataTab.version) {
            this.packageCreationService.checkBlueprintNameAndVersion(this.metaDataTab.name, this.metaDataTab.version).then(element => {
                if (element) {
                    this.errorMessage = 'Package name already exists with this version. Use different name or different version number.';
                } else if (!this.metaDataTab.version.match(this.versionPattern)) {
                    this.errorMessage = 'version should be as example 1.0.0';
                } else {
                    this.errorMessage = '';
                }
            });
        }

    }

    saveMetaDataToStore() {
        this.packageCreationStore.changeMetaData(this.metaDataTab);
    }

    checkRequiredElements() {
        const newMetaData = new MetaDataTabModel();
        newMetaData.description = this.metaDataTab.description;
        newMetaData.name = this.metaDataTab.name;
        newMetaData.version = this.metaDataTab.version;
        newMetaData.templateTags = this.metaDataTab.templateTags;
        newMetaData.mapOfCustomKey = this.metaDataTab.mapOfCustomKey;
        newMetaData.mode = this.metaDataTab.mode;
        this.packageCreationStore.changeMetaData(newMetaData);
    }


}
