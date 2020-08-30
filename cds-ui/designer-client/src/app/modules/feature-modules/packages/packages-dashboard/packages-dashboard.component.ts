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
import { Component, OnInit, OnDestroy } from '@angular/core';
import { PackagesStore } from '../packages.store';
import { TourService } from 'ngx-tour-md-menu';


@Component({
    selector: 'app-packages-dashboard',
    templateUrl: './packages-dashboard.component.html',
    styleUrls: ['./packages-dashboard.component.css']
})
export class PackagesDashboardComponent implements OnInit, OnDestroy {

    startTour = false;
    constructor(
        private tourService: TourService,
    ) { }

    ngOnInit() {

        console.log('PackagesDashboardComponent');

        this.tourService.initialize([
            {
                anchorId: 'allTab',
                content: 'This Tab contain all packages you created before',
                title: 'All Package',
            },
            {
                anchorId: 'search',
                content: 'Search for Package by name, version, tags and type',
                title: 'Search',
            },
            {
                anchorId: 'tagFilter',
                content: 'Filter Packages by tags',
                title: 'Tag Filter',
            },
            {
                anchorId: 'import',
                content: 'Import a package to CDS',
                title: 'Import',
            },
            {
                anchorId: 'create',
                content: 'Create a new Package',
                title: 'Create',
            },
            {
                anchorId: 'metadataTab',
                content: 'Set your package basic information',
                title: 'Metadata Tab',
                route: 'packages/createPackage'
            },
        ]);
        this.checkTour();
    }

    checkTour() {
        if (localStorage.getItem('tour-guide') && localStorage.getItem('tour-guide') === 'false') {
            this.startTour = false;
        } else {
            this.startTour = true;
        }
    }
    start() {
        console.log('start .................');
        this.tourService.start();
        this.tourService.events$.subscribe(res => {
            console.log(res);
        });
    }

    stopTour() {
        localStorage.setItem('tour-guide', 'false');
    }

    ngOnDestroy(): void {
        this.tourService.pause();
    }
}
