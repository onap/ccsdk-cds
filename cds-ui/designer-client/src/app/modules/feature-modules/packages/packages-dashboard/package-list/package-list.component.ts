import {Component, OnInit} from '@angular/core';
import {BlueprintModel} from '../../model/Blueprint.model';
import {PackagesStore} from '../../packages.store';
import {Router} from '@angular/router';
import {ConfigurationDashboardService} from '../../configuration-dashboard/configuration-dashboard.service';
import {saveAs} from 'file-saver';
import {NgxUiLoaderService} from 'ngx-ui-loader';
import {TourService} from 'ngx-tour-md-menu';
import {ToastrService} from 'ngx-toastr';

@Component({
    selector: 'app-packages-list',
    templateUrl: './package-list.component.html',
    styleUrls: ['./package-list.component.css']
})
export class PackageListComponent implements OnInit {

    viewedPackages: BlueprintModel[] = [];


    constructor(
        private packagesStore: PackagesStore,
        private router: Router,
        private configurationDashboardService: ConfigurationDashboardService,
        private ngxLoader: NgxUiLoaderService,
        private tourService: TourService,
        private toastService: ToastrService
    ) {
        console.log('PackageListComponent');


        this.packagesStore.state$.subscribe(state => {
            console.log(state);
            if (state.filteredPackages) {
                this.viewedPackages = state.filteredPackages.content;
            }
        });
    }


    ngOnInit() {
        this.ngxLoader.start();
        this.packagesStore.getAll();
    }

    view(id) {
        this.router.navigate(['/packages/package', id]);
    }

    testDispatch(bluePrint: BlueprintModel) {
        console.log(bluePrint.id);
    }

    downloadPackage(artifactName: string, artifactVersion: string) {
        this.configurationDashboardService.downloadResource(artifactName + '/' + artifactVersion).subscribe(response => {
            const blob = new Blob([response], {type: 'application/octet-stream'});
            saveAs(blob, artifactName + '-' + artifactVersion + '-CBA.zip');
        });
    }

    viewDesigner(id: string) {
        this.router.navigate(['/packages/designer', id, {actionName: ''}]);
    }

    deletePackage(id: string) {
        this.configurationDashboardService.deletePackage(id).subscribe(res => {
            this.toastService.success('Package Deleted Successfully ');
            this.router.navigate(['/packages']);
            this.packagesStore.getAll();
        }, err => {
            this.toastService.error('Error has occured during deleting package ' + err.message);
        });

    }
}
