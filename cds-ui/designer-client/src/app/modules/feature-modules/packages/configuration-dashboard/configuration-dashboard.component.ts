import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PackageStore } from './package.store';
import { BluePrintDetailModel } from '../model/BluePrint.detail.model';


@Component({
    selector: 'app-configuration-dashboard',
    templateUrl: './configuration-dashboard.component.html',
    styleUrls: ['./configuration-dashboard.component.css']
})
export class ConfigurationDashboardComponent implements OnInit {
    viewedPackage: BluePrintDetailModel = new BluePrintDetailModel();

    constructor(private route: ActivatedRoute, private configurationStore: PackageStore) {
    }
    // test
    ngOnInit() {
        const id = this.route.snapshot.paramMap.get('id');
        this.configurationStore.getPagedPackages(id).subscribe(
            (bluePrintDetailModels) => {
                console.log('-------------xxxxxxxxxxx----------------');
                console.log(bluePrintDetailModels);
                this.configurationStore.setConfiguration(bluePrintDetailModels);

                console.log('----------------- id ' + id);
                if (bluePrintDetailModels) {
                    this.configurationStore.downloadResource(
                        bluePrintDetailModels[0].artifactName + '/' + bluePrintDetailModels[0].artifactVersion);
                }
            });
    }

}
