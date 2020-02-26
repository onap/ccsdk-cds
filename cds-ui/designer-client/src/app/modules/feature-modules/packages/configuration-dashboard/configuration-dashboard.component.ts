import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {PackageStore} from './package.store';
import {BluePrintDetailModel} from '../model/BluePrint.detail.model';
import {CBAPackage} from '../package-creation/mapping-models/CBAPacakge.model';


@Component({
    selector: 'app-configuration-dashboard',
    templateUrl: './configuration-dashboard.component.html',
    styleUrls: ['./configuration-dashboard.component.css']
})
export class ConfigurationDashboardComponent implements OnInit {
    viewedPackage: BluePrintDetailModel = new BluePrintDetailModel();

    constructor(private route: ActivatedRoute, private configurationStore: PackageStore) {

        const id = this.route.snapshot.paramMap.get('id');
        this.configurationStore.getPagedPackages(id);


    }

    ngOnInit() {
        this.configurationStore.state$.subscribe(
            el => {
                if (el && el.configuration) {
                    this.viewedPackage = el.configuration;
                    console.log(this.configurationStore.downloadTest(
                        this.viewedPackage.artifactName + '/' + this.viewedPackage.artifactVersion));
                }
            }
        );
    }

}
