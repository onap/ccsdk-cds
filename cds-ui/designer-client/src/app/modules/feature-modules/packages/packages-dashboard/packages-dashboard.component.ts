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
import { steps } from './guideSteps';
declare var $: any;

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
                content: 'Package list is where you get access to your all and most recent CBA packages.',
                title: 'Managing your CBA packages',
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
                anchorId: 'create',
                content: 'Start creating a full CBA packages from built-in forms without programming.',
                title: 'Create new package',
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
            if (res.value && res.value.anchorId && res.value.anchorId.includes('tm-')) {
                $('#nav-template-tab').trigger('click');
            }
            if (res.value && res.value.anchorId && res.value.anchorId === 'tm-mappingContent') {
                $('#mappingTab').trigger('click');
            }
            if (res.value && res.value.anchorId && res.value.anchorId === 'dslTab') {
                $('#nav-authentication-tab').trigger('click');
            }
            if (res.value && res.value.anchorId && res.value.anchorId.includes('st-')) {
                $('#nav-scripts-tab').trigger('click');
            }
        });
    }

    stopTour() {
        localStorage.setItem('tour-guide', 'false');
    }

    ngOnDestroy(): void {
        this.tourService.pause();
    }
}
