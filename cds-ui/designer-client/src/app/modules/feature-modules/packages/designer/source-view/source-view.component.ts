import { Component, OnInit, OnDestroy } from '@angular/core';
import { DesignerStore } from '../designer.store';
import { PackageCreationUtils } from '../../package-creation/package-creation.utils';
import { RouterLink, Router, ActivatedRoute } from '@angular/router';
import { Subject } from 'rxjs';
import { BlueprintDetailModel } from '../../model/Blueprint.detail.model';
import { viewClassName } from '@angular/compiler';
import { SourceViewService } from './source-view.service';

@Component({
    selector: 'app-designer-source-view',
    templateUrl: './source-view.component.html',
    // styleUrls: ['./source-view.component.css']
    styleUrls: ['../designer.component.css']
})
export class DesignerSourceViewComponent implements OnInit, OnDestroy {

    content = '';
    lang = 'json';
    controllerSideBar: boolean;
    ngUnsubscribe = new Subject();
    viewedPackage: BlueprintDetailModel = new BlueprintDetailModel();
    public customActionName = '';
    cl = 'editBar';
    packageId: string;

    constructor(
        private store: DesignerStore,
        private packageCreationUtils: PackageCreationUtils,
        private router: Router,
        private route: ActivatedRoute,
        private sourceViewService: SourceViewService) {
        this.controllerSideBar = true;
    }
    _toggleSidebar1() {
        this.controllerSideBar = !this.controllerSideBar;
        if (this.controllerSideBar === false) {
            this.cl = 'editBar2';
        }
        if (this.controllerSideBar === true) {
            this.cl = 'editBar';
        }
    }

    ngOnInit() {
        this.store.state$.subscribe(
            state => {
                console.log(state);
                this.content = this.packageCreationUtils.transformToJson(state.template);
            });

        const id = this.route.snapshot.paramMap.get('id');
        this.sourceViewService.getPagedPackages(id).subscribe(
            (bluePrintDetailModels) => {
                if (bluePrintDetailModels) {
                    this.viewedPackage = bluePrintDetailModels[0];
                }
            });

        this.route.paramMap.subscribe(res => {
            this.packageId = res.get('id');
        });
    }

    convertAndOpenInDesingerView(id) {
        // TODO validate json against scheme
        console.log('convertAndOpenInDesingerView ...', this.content);
        this.store.saveSourceContent(this.content);
        this.router.navigate(['/packages/designer', this.packageId, { actionName: this.customActionName }]);
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
