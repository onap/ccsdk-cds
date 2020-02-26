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

import { Injectable } from '@angular/core';
import { Store } from '../../../../common/core/stores/Store';
import { ConfigurationDashboardService } from './configuration-dashboard.service';
import { PackageDashboardState } from '../model/package-dashboard.state';
import { BlueprintURLs } from '../../../../common/constants/app-constants';
import * as JSZip from 'jszip';

@Injectable({
    providedIn: 'root'
})
export class PackageStore extends Store<PackageDashboardState> {

    private zipFile: JSZip = new JSZip();

    constructor(private configurationDashboardService: ConfigurationDashboardService) {
        super(new PackageDashboardState());
    }

    getPagedPackages(id: string) {
        return this.configurationDashboardService.getBluePrintModel(id);
    }

    public downloadResource(path: string) {
        console.log('download resource xx');
        this.configurationDashboardService.downloadResource(BlueprintURLs.download + path).subscribe(response => {
            console.log('try to download ');
            const blob = new Blob([response], { type: 'application/octet-stream' });
            this.zipFile.loadAsync(blob).then((zip) => {
                Object.keys(zip.files).forEach((filename) => {
                    console.log(filename);
                    zip.files[filename].async('string').then((fileData) => {
                        if (fileData) {
                            if (filename.includes('Scripts/')) {
                                this.setScripts(filename, fileData);
                            } else if (filename.includes('templates/')) {
                                this.setTemplates(filename, fileData);
                            } else if (filename.includes('definitions/')) {
                                this.setImports(filename, fileData);
                            }
                        }
                    });
                });
            });
        });
    }

    setConfiguration(bluePrintDetailModels) {
        this.setState({
            ...this.state,
            configuration: bluePrintDetailModels[0]
        });
    }

    private setScripts(filename: string, fileData: any) {
        this.setState({
            ...this.state,
            scripts: this.state.scripts.setScripts(filename, fileData)
        });
    }

    private setImports(filename: string, fileData: any) {

    }

    private setTemplates(filename: string, fileData: any) {

    }
}
