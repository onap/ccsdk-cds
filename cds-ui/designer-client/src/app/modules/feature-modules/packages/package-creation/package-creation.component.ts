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

import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FilesContent, FolderNodeElement, MetaDataTabModel } from './mapping-models/metadata/MetaDataTab.model';

import * as JSZip from 'jszip';
import { PackageCreationStore } from './package-creation.store';
import { CBAPackage, Definition } from './mapping-models/CBAPacakge.model';
import { PackageCreationModes } from './creationModes/PackageCreationModes';
import { PackageCreationBuilder } from './creationModes/PackageCreationBuilder';
import { PackageCreationUtils } from './package-creation.utils';
import { MetadataTabComponent } from './metadata-tab/metadata-tab.component';
import { Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { TourService } from 'ngx-tour-md-menu';
import { PackageCreationService } from './package-creation.service';
import { ComponentCanDeactivate } from '../../../../common/core/canDactivate/ComponentCanDeactivate';
import { DesignerStore } from '../designer/designer.store';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { mapBlueprintError } from '../../../../common/core/services/blueprint-error-handler';
import { NgxFileDropEntry, FileSystemFileEntry } from 'ngx-file-drop';
declare var $: any;


@Component({
    selector: 'app-package-creation',
    templateUrl: './package-creation.component.html',
    styleUrls: ['./package-creation.component.css']
})
export class PackageCreationComponent extends ComponentCanDeactivate implements OnInit, OnDestroy {


    // adding initial referencing to designer mode


    constructor(
        private packageCreationStore: PackageCreationStore,
        private packageCreationService: PackageCreationService,
        private packageCreationUtils: PackageCreationUtils,
        private router: Router,
        private tourService: TourService,
        private toastService: ToastrService,
        private ngxService: NgxUiLoaderService,
        private designerStore: DesignerStore) {

        super();
    }

    counter = 0;
    modes: object[] = [
        { name: 'Designer Mode', style: 'mode-icon icon-designer-mode' },
        { name: 'Scripting Mode', style: 'mode-icon icon-scripting-mode' }];
    metaDataTab: MetaDataTabModel = new MetaDataTabModel();
    folder: FolderNodeElement = new FolderNodeElement();
    zipFile: JSZip = new JSZip();
    filesData: any = [];
    definition: Definition = new Definition();
    isSaveEnabled = false;

    @ViewChild(MetadataTabComponent, { static: false })
    metadataTabComponent: MetadataTabComponent;

    @ViewChild('nameit', { static: true })
    elementRef: ElementRef;
    versionPattern = '^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$';
    metadataClasses = 'nav-item nav-link active complete';
    lifecycle = { ddImported: false, enriched: false, published: false, enrichError: '' };
    ddImportFile: File = null;
    private enrichedBlob: Blob = null;
    private cbaPackage: CBAPackage;

    ngOnInit() {
        this.elementRef.nativeElement.focus();
        const regexp = RegExp(this.versionPattern);
        this.packageCreationStore.state$.subscribe(cbaPackage => {
            console.log(cbaPackage);
            console.log('abbaaaas' + cbaPackage.metaData.name);
            this.cbaPackage = cbaPackage;
            if (cbaPackage && cbaPackage.metaData && cbaPackage.metaData.name
                && cbaPackage.metaData.version &&
                regexp.test(cbaPackage.metaData.version)) {
                this.isSaveEnabled = true;
                if (!this.metadataClasses.includes('complete')) {
                    console.log('added');
                    this.metadataClasses += 'complete';
                }
                console.log('perhaps it is been added');
            } else {
                this.isSaveEnabled = false;
                this.metadataClasses = this.metadataClasses.replace('complete', '');
            }
        });
    }

    openTourGuide(step: string) {
        if (this.tourService.currentStep && this.tourService.currentStep.anchorId !== step) {
            this.tourService.goto(step);
        }
    }

    saveBluePrint() {
        this.ngxService.start();
        console.log(this.cbaPackage);
        FilesContent.clear();
        let packageCreationModes: PackageCreationModes;
        this.cbaPackage = PackageCreationModes.mapModeType(this.cbaPackage);
        this.cbaPackage.metaData = PackageCreationModes.setEntryPoint(this.cbaPackage.metaData);
        packageCreationModes = PackageCreationBuilder.getCreationMode(this.cbaPackage);

        // this.cbaPackage.templateTopology.content = this.designerStore.state.sourceContent;
        packageCreationModes.execute(this.cbaPackage, this.packageCreationUtils);
        this.filesData.push(this.folder.TREE_DATA);
        this.saveBluePrintToDataBase();


    }


    saveBluePrintToDataBase() {
        this.create();
        this.zipFile.generateAsync({ type: 'blob' })
            .then(blob => {
                this.packageCreationService.savePackage(blob).subscribe(
                    bluePrintDetailModels => {
                        if (bluePrintDetailModels) {
                            const id = bluePrintDetailModels.toString().split('id')[1].split(':')[1].split('"')[1];
                            this.toastService.success('Package Updated Successfully ');
                            this.isSaveEnabled = false;
                            this.router.navigate(['/packages/package/' + id]);
                        }
                    }, error => {
                        const mapped = mapBlueprintError(error);
                        this.toastService.error(mapped.suggestion, mapped.title, { timeOut: 8000, closeButton: true });
                        console.log('Error -' + error.message);
                        this.ngxService.stop();
                    }, () => {
                        this.ngxService.stop();
                    });
            });
    }


    create() {
        FilesContent.getMapOfFilesNamesAndContent().forEach((value, key) => {
            this.zipFile.folder(key.split('/')[0]);
            this.zipFile.file(key, value);
        });

    }


    openDDImportModal() {
        this.ddImportFile = null;
        ($('#ddImportModal') as any).modal('show');
    }

    onDDFileDrop(files: NgxFileDropEntry[]) {
        if (files.length > 0 && files[0].fileEntry.isFile) {
            (files[0].fileEntry as FileSystemFileEntry).file((f: File) => { this.ddImportFile = f; });
        }
    }

    importDataDictionary() {
        if (!this.ddImportFile) { return; }
        const reader = new FileReader();
        reader.onload = (e: any) => {
            try {
                const entries = JSON.parse(e.target.result);
                this.packageCreationService.importDataDictionary(entries).subscribe(
                    () => {
                        this.lifecycle.ddImported = true;
                        this.toastService.success('Data dictionary imported — ' + entries.length + ' entries registered.');
                    },
                    err => this.toastService.error(
                        'DD import failed: check the JSON format and try again.', 'Import failed', { timeOut: 8000 })
                );
            } catch (err) {
                this.toastService.error('Invalid JSON file. Please select a valid dd.json.', 'Import failed', { timeOut: 8000 });
            }
        };
        reader.readAsText(this.ddImportFile);
    }

    enrichCurrentPackage() {
        this.ngxService.start();
        this.lifecycle.enrichError = '';
        FilesContent.clear();
        this.cbaPackage = PackageCreationModes.mapModeType(this.cbaPackage);
        this.cbaPackage.metaData = PackageCreationModes.setEntryPoint(this.cbaPackage.metaData);
        PackageCreationBuilder.getCreationMode(this.cbaPackage).execute(this.cbaPackage, this.packageCreationUtils);
        this.filesData.push(this.folder.TREE_DATA);
        this.packageCreationService.enrichCurrentPackage().then(obs$ => {
            obs$.subscribe(
                (blob: Blob) => {
                    this.enrichedBlob = blob;
                    this.lifecycle.enriched = true;
                    this.toastService.success('Blueprint enriched successfully. Ready to publish.');
                    this.ngxService.stop();
                },
                err => {
                    const mapped = mapBlueprintError(err);
                    this.lifecycle.enrichError = mapped.suggestion;
                    this.toastService.error(mapped.suggestion, mapped.title, { timeOut: 10000, closeButton: true });
                    this.ngxService.stop();
                }
            );
        });
    }

    publishCurrentPackage() {
        if (!this.enrichedBlob) { return; }
        this.ngxService.start();
        this.packageCreationService.deploy(this.enrichedBlob).subscribe(
            () => {
                this.lifecycle.published = true;
                this.toastService.success('Blueprint published and ready for use in SO workflows.');
                this.ngxService.stop();
            },
            err => {
                this.toastService.error(
                    'Publish failed. The blueprint processor rejected the enriched CBA.', 'Publish failed', { timeOut: 8000 });
                this.ngxService.stop();
            }
        );
    }

    goBackToDashBorad() {
        this.router.navigate(['/packages']);
    }

    saveMetaData() {
        console.log('executed change');
        this.metadataTabComponent.saveMetaDataToStore();

    }

    canDeactivate(): boolean {
        return this.isSaveEnabled;
    }

    ngOnDestroy(): void {

    }
}
